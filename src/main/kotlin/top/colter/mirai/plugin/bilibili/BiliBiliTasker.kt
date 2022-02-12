package top.colter.mirai.plugin.bilibili

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.permission.PermissionService.Companion.getPermittedPermissions
import net.mamoe.mirai.console.permission.PermitteeId.Companion.permitteeId
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.AtAll
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.error
import top.colter.mirai.plugin.bilibili.PluginMain.contactMap
import top.colter.mirai.plugin.bilibili.PluginMain.save
import top.colter.mirai.plugin.bilibili.data.*
import top.colter.mirai.plugin.bilibili.data.BiliPluginConfig.lowSpeed
import top.colter.mirai.plugin.bilibili.utils.HttpUtils
import top.colter.mirai.plugin.bilibili.utils.decode
import java.net.URI
import java.time.Instant
import java.time.LocalTime
import java.util.stream.Collectors
import kotlin.coroutines.CoroutineContext

internal val logger by PluginMain::logger

object DynamicTasker : CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.IO + CoroutineName("DynamicTasker")

    private var listener: MutableList<Job>? = null

    private val seleniumMutex = Mutex()

    private val httpUtils = HttpUtils()

    val mutex = Mutex()
    val dynamic: MutableMap<Long, SubData> by BiliSubscribeData::dynamic

    private val startTime = Instant.now().epochSecond
    private var lastDynamic = startTime
    private var lastLive = startTime
    private val historyMutex = Mutex()
    private val historyDynamic: MutableMap<Long, MutableList<Long>> = mutableMapOf()
    private val history: MutableList<Long> = mutableListOf()

    private val interval = BiliPluginConfig.interval * 1000
    private val liveInterval = BiliPluginConfig.liveInterval * 1000

    private var lsl = listOf(0,0)
    private var isLowSpeed = false

    private var errorMessage = "SUCCESS"
    private var liveErrorMessage = "SUCCESS"

    fun start() {
        runCatching {
            lsl = lowSpeed.split("-","x").map { it.toInt() }
            isLowSpeed = lsl[0] != lsl[1]
        }.onFailure {
            logger.error("低频检测参数错误 ${it.message}")
        }

        listener = listen()
    }

    fun stop() {
        listener?.forEach {
            it.cancel()
        }
    }

    suspend fun listenAll(subject: String) = mutex.withLock {
        dynamic.forEach { (uid, sub) ->
            if (subject in sub.contacts) {
                sub.contacts.remove(subject)
            }
        }
        val user = dynamic[0]
        user?.contacts?.set(subject, "11")
    }

    suspend fun cancelListen(subject: String) = mutex.withLock {
        dynamic[0]?.contacts?.remove(subject)
    }

    private fun followUser(uid: Long): String {
        if (uid == PluginMain.mid) {
            return "不能关注自己哦"
        }
        val attr = httpUtils.getAndDecode<IsFollow>(IS_FOLLOW(uid)).attribute
        if (attr == 0) {
            if (!BiliPluginConfig.autoFollow) {
                return "未关注此用户"
            } else {
                val postBody = "fid=$uid&act=1&re_src=11&csrf=${PluginMain.biliJct}"
                val res = httpUtils.post(FOLLOW, postBody).decode<ResultData>()
                if (res.code != 0) {
                    return "关注失败: ${res.message}"
                }
                if (BiliPluginConfig.followGroup.isNotEmpty()) {
                    val pb = "fids=${uid}&tagids=${PluginMain.tagid}&csrf=${PluginMain.biliJct}"
                    val res1 = httpUtils.post(ADD_USER, pb).decode<ResultData>()
                    if (res1.code != 0) {
                        logger.error("移动分组失败: ${res1.message}")
                    }
                }
            }
        } else if (attr == 128) {
            return "此账号已被拉黑"
        }
        return ""
    }

    suspend fun setColor(uid: Long, color: String): String {
        if (color.first() != '#' || color.length != 7) {
            return "格式错误，请输入16进制颜色，如: #d3edfa"
        }
        mutex.withLock {
            dynamic[uid]?.color = color
        }
        return "设置完成"
    }

    suspend fun addSubscribe(uid: Long, subject: String) = mutex.withLock {
        if (dynamic[0]?.contacts?.contains(subject) == true) {
            dynamic[0]?.contacts?.remove(subject)
        }
        val m = followUser(uid)
        if (m != "") {
            return@withLock m
        }
        val user = dynamic[uid]
        if (user == null) {
            val subData = SubData(httpUtils.getAndDecode<User>(USER_INFO(uid)).name)
            subData.contacts[subject] = "11"
            dynamic[uid] = subData
            "订阅 ${dynamic[uid]?.name} 成功! \n默认检测 动态+视频+直播 如果需要调整请发送/bili set $uid\n如要设置主题色请发送/bili color <16进制颜色>"
        } else {
            if (user.contacts.contains(subject)) {
                "之前订阅过这个人哦"
            } else {
                user.contacts[subject] = "11"
                "订阅 ${dynamic[uid]?.name} 成功! \n默认检测 动态+视频+直播 如果需要调整请发送/bili set $uid"
            }

        }
    }

    suspend fun addFilter(regex: String, uid: Long, subject: String, mode: Boolean = true) = mutex.withLock {
        if (dynamic.containsKey(uid)){
            val filter = if (mode) dynamic[uid]?.filter else dynamic[uid]?.containFilter
            if (filter?.containsKey(subject) == true){
                filter[subject]?.add(regex)
            }else{
                filter?.set(subject, mutableListOf(regex))
            }
            "设置成功"
        }else{
            "还未关注此人哦"
        }
    }

    suspend fun listFilter(uid: Long, subject: String) = mutex.withLock {
        if (dynamic.containsKey(uid)){
            return@withLock buildString {
                appendLine("过滤 ")
                if (dynamic[uid]?.filter?.containsKey(subject) == true && dynamic[uid]?.filter?.get(subject)?.size!! > 0){
                    dynamic[uid]?.filter?.get(subject)?.forEachIndexed { index, s ->
                        appendLine("f$index: $s")
                    }
                }else{
                    appendLine("还没有设置过滤哦")
                }
                appendLine("包含 ")
                if (dynamic[uid]?.containFilter?.containsKey(subject) == true && dynamic[uid]?.containFilter?.get(subject)?.size!! > 0){
                    dynamic[uid]?.containFilter?.get(subject)?.forEachIndexed { index, s ->
                        appendLine("c$index: $s")
                    }
                }else{
                    appendLine("还没有设置包含哦")
                }
            }
        }else{
            "还未关注此人哦"
        }
    }

    suspend fun delFilter(uid: Long, subject: String, index: String) = mutex.withLock {
        if (dynamic.containsKey(uid)){
            var i = 0
            runCatching {
                i = index.substring(1).toInt()
            }.onFailure {
                return@withLock "索引错误"
            }
            val filter = if (index[0] == 'f'){
                 dynamic[uid]?.filter
            }else if (index[0] == 'c'){
                dynamic[uid]?.containFilter
            }else{
                return@withLock "索引值错误"
            }
            if (filter?.containsKey(subject) == true){
                if (filter[subject]?.size!! < i) return@withLock "索引超出范围"
                val ft = filter[subject]?.get(i)
                filter[subject]?.removeAt(i)
                "已删除 $ft 过滤"
            }else{
                "还没有设置过滤哦"
            }
        }else{
            "还未关注此人哦"
        }
    }

    suspend fun removeSubscribe(uid: Long, subject: String) = mutex.withLock {
        val user = dynamic[uid]
        user?.contacts?.remove(subject)
        user
    }

    suspend fun removeAllSubscribe(subject: String) = mutex.withLock {
        dynamic.count { (uid, sub) ->
            if (sub.contacts.contains(subject)) {
                sub.contacts.remove(subject)
                true
            } else false
        }
    }

    suspend fun list(subject: String) = mutex.withLock {
        var count = 0
        buildString {
            dynamic.forEach { (uid, sub) ->
                if (subject in sub.contacts) {
                    appendLine("${sub.name}@$uid")
                    count++
                }
            }
            append("共 $count 个订阅")
        }
    }

    suspend fun login(contact: Contact){
        val loginData = httpUtils.getAndDecode<LoginData>(LOGIN_URL)
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(loginData.url, BarcodeFormat.QR_CODE, 250, 250)
        val file = PluginMain.resolveDataFile("qrcode.png")
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", file.toPath())
        contact.sendMessage(file.uploadAsImage(contact) + "请使用BiliBili手机APP扫码登录 3分钟有效")
        runCatching {
            withTimeout(180000){
                while (true){
                    val loginInfo = httpUtils.post(LOGIN_INFO,"oauthKey=${loginData.oauthKey}").decode<ResultData>()
                    if (loginInfo.status == true){
                        val url = loginInfo.data?.decode<LoginData>()?.url
                        val querys = URI(url).query.split("&")
                        val cookie = buildString {
                            querys.forEach {
                                if (it.contains("SESSDATA") || it.contains("bili_jct")) append("$it; ")
                            }
                        }
                        BiliPluginConfig.cookie = cookie
                        BiliPluginConfig.save()
                        logger.info("Cookie: ${BiliPluginConfig.cookie}")
                        initCookie()
                        initTagid()
                        getHistoryDynamic()
                        contact.sendMessage("登录成功!")
                        break
                    }
                    delay(5000)
                }
            }
        }.onFailure {
            contact.sendMessage("登录失败 ${it.message}")
        }
        file.delete()
    }


    private fun listen(): MutableList<Job> {
        val jobList = mutableListOf<Job>()
        jobList.add(normalJob())
        jobList.add(liveJob())
        return jobList
    }

    private fun getHistoryDynamic() = runCatching {
        val nd = httpUtils.getAndDecode<DynamicList>(NEW_DYNAMIC)
        val ul = dynamic.map { it.key }
        val ndList =
            nd.dynamics?.stream()?.filter { v -> ul.contains(v.uid) }?.map { v -> v.did }?.collect(Collectors.toList())
        ndList?.let { history.addAll(it) }
    }.onFailure {
        logger.error(it.message)
    }

    private fun normalJob() = launch {
        getHistoryDynamic()
        var intervalTime = interval

        delay(10000L)
        while (isActive) {
            if (PluginMain.sessData == ""){
                logger.debug("未登录")
                delay(100000)
                continue
            }
            runCatching {
                logger.debug("DynamicCheck")
                intervalTime = calcTime(interval)

                val newDynamic = httpUtils.getAndDecode<DynamicList>(NEW_DYNAMIC)
                val dynamics = newDynamic.dynamics ?: return@runCatching
                val uidList = dynamic.filter { it.value.contacts.isNotEmpty() }.map { it.key }
                val newDynamicList = dynamics.stream().filter { it.timestamp > lastDynamic }
                    .filter { uidList.contains(it.uid) }
                    .filter { !history.contains(it.did) }
                    .collect(Collectors.toList())

                newDynamicList.forEach { di ->
                    val describe = di.describe
                    logger.info("新动态: ${di.uname}@${di.uid}=${di.did}")
                    var list = getDynamicContactList(describe.uid, di.describe.type == 8)
                    if (list != null && list.size > 0) {
                        di.dynamicContent = di.card.dynamicContent(di.type)
                        list = list.filterContent(describe.uid, di.dynamicContent!!.textContent(describe.type))
                        if (list.size > 0){
                            val color = dynamic[describe.uid]?.color ?: "#d3edfa"
                            seleniumMutex.withLock {
                                history.add(describe.dynamicId)
                                withTimeout(30000) {
                                    list.sendMessage(di.type) { di.build(it, color) }
                                }
                            }
                        }
                    }
                }
            }.onSuccess {
                errorMessage = "SUCCESS"
                delay((intervalTime..(intervalTime + 5000L)).random())
            }.onFailure {
                logger.error("ERROR $it")
                if (it.message != errorMessage){
                    (findContact(BiliPluginConfig.admin)?:findContact("-${BiliPluginConfig.admin}"))?.sendMessage("动态检测失败\n" + it.message)
                }
                if (errorMessage == "SUCCESS"){
                    errorMessage = it.message.toString()
                }
                delay((60000L..120000L).random())
            }
        }
    }

    private fun liveJob() = launch {
        var liveIntervalTime = interval

        delay(20000L)
        while (isActive) {
            if (PluginMain.sessData == ""){
                delay(100000)
                continue
            }
            runCatching {
                logger.debug("LiveCheck")
                liveIntervalTime = calcTime(liveInterval)

                val liveList = httpUtils.getAndDecode<Live>(LIVE_LIST).liveList
                val newLiveList = liveList.stream().filter { it.liveTime > lastLive }
                    .filter { v -> dynamic.map { it.key }.contains(v.uid) }
                    .collect(Collectors.toList())
                newLiveList.forEach { ll ->
                    val list = getLiveContactList(ll.uid)
                    if (list != null && list.size != 0) {
                        seleniumMutex.withLock {
                            withTimeout(30000) {
                                list.sendMessage(DynamicType.LIVE){ ll.build(it) }
                            }
                        }
                        lastLive = ll.liveTime
                    }
                }
            }.onSuccess {
                liveErrorMessage = "SUCCESS"
                delay((liveIntervalTime..(liveIntervalTime + 5000L)).random())
            }.onFailure {
                logger.error("ERROR $it")
                if (it.message != liveErrorMessage){
                    (findContact(BiliPluginConfig.admin)?:findContact("-${BiliPluginConfig.admin}"))?.sendMessage("直播检测失败\n" + it.message)
                }
                if (liveErrorMessage == "SUCCESS"){
                    liveErrorMessage = it.message.toString()
                }
                delay((60000L..120000L).random())
            }
        }
    }

    private fun calcTime(time: Int): Int{
        return if (isLowSpeed){
            val hour = LocalTime.now().hour
            return if (lsl[0] > lsl[1]){
                if (lsl[0] <= hour || hour <= lsl[1]) time * lsl[2] else time
            }else{
                if (lsl[0] <= hour && hour <= lsl[1]) time * lsl[2] else time
            }
        }else time
    }

    private fun missJob() = launch {
        delay(10000L)
        while (isActive) {
            runCatching {
                logger.info("missJobStart")
                val subData: Map<Long, SubData>
                mutex.withLock {
                    subData = dynamic.toMap()
                }
                val time: Long = Instant.now().epochSecond
                subData.forEach { (uid, sub) ->
                    logger.info("missJob: $uid")
                    if (uid != 0L) {
                        val dynamics = httpUtils.getAndDecode<DynamicList>(DYNAMIC_LIST(uid)).dynamics ?: return@forEach
                        for (i in 0..dynamics.size) {
                            val describe = dynamics[i].describe
                            if (describe.timestamp <= lastDynamic) break
                            if (historyMutex.withLock { historyDynamic[uid]?.contains(describe.dynamicId) == true }) {
                                historyMutex.withLock { historyDynamic[uid]?.remove(describe.dynamicId) }
                                continue
                            }
                            val list = sub.contacts.keys
                            if (list.size > 0) {
                                seleniumMutex.withLock {
                                    if (!history.contains(describe.dynamicId)) {
                                        history.add(describe.dynamicId)
                                        list.sendMessage { dynamics[i].build(it, sub.color) }
                                    }
                                }
                            }
                        }
                        delay((interval..interval + 5000L).random())
                    }
                }
                lastDynamic = time
            }.onSuccess {
                delay((interval..interval + 5000L).random())
            }.onFailure {
                logger.error("ERROR $it")
                findContact(BiliPluginConfig.admin)?.sendMessage("动态检测失败\n" + it.message)
                delay((60000L..120000L).random())
            }
        }
    }


    private suspend fun getDynamicContactList(uid: Long, isVideo: Boolean): MutableSet<String>? = mutex.withLock {
        return try {
            val all = dynamic[0] ?: return null
            val list: MutableSet<String> = mutableSetOf()
            list.addAll(all.contacts.keys)
            val subData = dynamic[uid] ?: return list
            if (isVideo) list.addAll(subData.contacts.filter { it.value[0] == '1' || it.value[0] == '2' }.keys)
            else list.addAll(subData.contacts.filter { it.value[0] == '1' }.keys)
            list.removeAll(subData.banList.keys)
            list
        } catch (e: Throwable) {
            null
        }
    }

    private suspend fun getLiveContactList(uid: Long): MutableSet<String>? = mutex.withLock {
        return try {
            val all = dynamic[0] ?: return null
            val list: MutableSet<String> = mutableSetOf()
            list.addAll(all.contacts.keys)
            val subData = dynamic[uid] ?: return list
            list.addAll(subData.contacts.filter { it.value[1] == '1' }.keys)
            list.removeAll(subData.banList.keys)
            list
        } catch (e: Throwable) {
            null
        }
    }

    private suspend fun MutableSet<String>.filterContent(uid: Long, content: String): MutableSet<String> = mutex.withLock {
        val allContainList = mutableListOf<String>()
        dynamic[0]?.containFilter?.get("0")?.let { allContainList.addAll(it) }
        dynamic[uid]?.containFilter?.get("0")?.let { allContainList.addAll(it) }

        val allFilterList = mutableListOf<String>()
        dynamic[0]?.filter?.get("0")?.let { allFilterList.addAll(it) }
        dynamic[uid]?.filter?.get("0")?.let { allFilterList.addAll(it) }

        return filter { contact ->
            val containList = mutableListOf<String>()
            containList.addAll(allContainList)
            dynamic[0]?.containFilter?.get(contact)?.let { containList.addAll(it) }
            dynamic[uid]?.containFilter?.get(contact)?.let { containList.addAll(it) }
            containList.forEach {
                val b = Regex(it).containsMatchIn(content)
                if (b) return@filter true
            }
            if (containList.size > 0) return@filter false

            val filterList = mutableListOf<String>()
            filterList.addAll(allFilterList)
            dynamic[0]?.filter?.get(contact)?.let { filterList.addAll(it) }
            dynamic[uid]?.filter?.get(contact)?.let { filterList.addAll(it) }
            filterList.forEach {
                val b = Regex(it).containsMatchIn(content)
                if (b) return@filter false
            }
            true
        }.toMutableSet()
    }

    private suspend inline fun MutableSet<String>.sendMessage(
        dynamicType: Int = DynamicType.DELETE,
        message: (contact: Contact) -> Message
    ) {
        val me = findContact(this.first())?.let { message(it) }
        if (me != null) {
            this.map { delegate ->
                runCatching {
                    requireNotNull(findContact(delegate)) { "找不到联系人" }.let { contact ->
                        var msg = me
                        if (contact is Group) {
                             val gwp = when (dynamicType){
                                DynamicType.LIVE -> PluginMain.gwp
                                DynamicType.VIDEO -> PluginMain.videoGwp
                                else -> null
                            }
                            val hasPerm = contact.permitteeId.getPermittedPermissions().any { it.id == gwp }
                            if (hasPerm) msg = msg + "\n" + AtAll
                        }
                        runCatching {
                            contact.sendMessage(msg)
                        }.onFailure {
                            logger.error(it.message)
                            contact.sendMessage(me)
                        }
                    }
                }.onFailure {
                    logger.error({ "对${this}构建消息失败" }, it)
                }
            }
        }
    }

}

/**
 * 查找Contact
 */
fun findContact(del: String, ): Contact? = synchronized(contactMap) {
    if (del.isBlank()) return@synchronized null
    val delegate = del.toLong()
    contactMap.compute(delegate) { _, _ ->
        for (bot in Bot.instances) {
            if (delegate < 0) {
                for (group in bot.groups) {
                    if (group.id == delegate * -1) return@compute group
                }
            } else {
                for (friend in bot.friends) {
                    if (friend.id == delegate) return@compute friend
                }
                for (stranger in bot.strangers) {
                    if (stranger.id == delegate) return@compute stranger
                }
                for (group in bot.groups) {
                    for (member in group.members) {
                        if (member.id == delegate) return@compute member
                    }
                }
            }
        }
        null
    }
}

/**
 * 通过正负号区分群和用户
 * @author cssxsh
 */
val Contact.delegate get() = (if (this is Group) id * -1 else id).toString()


