package top.colter.mirai.plugin.bilibili

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.util.CoroutineScopeUtils.childScope
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.AtAll
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.error
import top.colter.mirai.plugin.bilibili.PluginMain.contactMap
import top.colter.mirai.plugin.bilibili.data.*
import top.colter.mirai.plugin.bilibili.data.BiliPluginConfig.liveAtAll
import top.colter.mirai.plugin.bilibili.utils.HttpUtils
import top.colter.mirai.plugin.bilibili.utils.decode
import java.time.Instant
import java.util.stream.Collectors

internal val logger by PluginMain::logger

object DynamicTasker : CoroutineScope by PluginMain.childScope("DynamicTasker") {

    private var listener: MutableList<Job>? = null

    private val seleniumMutex = Mutex()

    //    private val httpMutex = Mutex()
    private var httpUtils = HttpUtils()

    val mutex = Mutex()
    val dynamic: MutableMap<Long, SubData> by BiliSubscribeData::dynamic

    private val startTime = Instant.now().epochSecond
    private var lastDynamic = startTime
    private var lastLive = startTime
    private val historyMutex = Mutex()
    private val historyDynamic: MutableMap<Long, MutableList<Long>> = mutableMapOf()
    private val history: MutableList<Long> = mutableListOf()

    private val interval = BiliPluginConfig.interval * 1000


    fun start() {
        listener = listen()
    }

    fun stop() {
        listener?.forEach {
            it.cancel()
        }
    }

//    suspend fun listenAll(subject: String) = mutex.withLock {
//        dynamic.forEach { (uid, sub) ->
//            if (subject in sub.contacts) {
//                sub.contacts.remove(subject)
//            }
//        }
//
//        val user = dynamic[0]
//        user?.contacts?.set(subject, "11")
//    }
//
//    suspend fun cancelListen(subject: String) = mutex.withLock {
//        dynamic[0]?.contacts?.remove(subject)
//    }

    private fun followUser(uid: Long): String {

        val attr = httpUtils.getAndDecode<IsFollow>(IS_FOLLOW(uid)).attribute
        if (attr == 0) {
            if (!BiliPluginConfig.autoFollow) {
                return "未关注此用户"
            } else {
                val postBody = "fid=$uid&act=1&re_src=11&csrf=${PluginMain.biliJct}"
                val res = httpUtils.post(FOLLOW, postBody).decode<ResultData>()
                if (res.code != 0) {
                    return "关注失败"
                }
                if (BiliPluginConfig.followGroup.isNotEmpty()) {
                    val pb = "fids=${uid}&tagids=${PluginMain.tagid}&csrf=${PluginMain.biliJct}"
                    val res = httpUtils.post(ADD_USER, pb).decode<ResultData>()
                    if (res.code != 0) {
                        logger.error("移动分组失败: ${res.message}")
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
            user.contacts[subject] = "11"
            "订阅 ${dynamic[uid]?.name} 成功! \n默认检测 动态+视频+直播 如果需要调整请发送/bili set $uid"
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
            appendLine("共 $count 个订阅")
        }
    }

    private fun listen(): MutableList<Job> {

        val jobList = mutableListOf<Job>()

        jobList.add(normalJob())
//        if (BiliPluginConfig.botMode==1) jobList.add(normalJob())
//        else jobList.add(missJob())

        jobList.add(liveJob())

        return jobList

    }

    private fun normalJob() = launch {
//        var count = 0
        val nd = httpUtils.getAndDecode<DynamicList>(NEW_DYNAMIC)
        val ul = dynamic.map { it.key }
        val ndList =
            nd.dynamics?.stream()?.filter { v -> ul.contains(v.uid) }?.map { v -> v.did }?.collect(Collectors.toList())
        ndList?.let { history.addAll(it) }

        delay(10000L)
        while (isActive) {
            runCatching {
                logger.debug("DynamicCheck")
//                count++
//                if (count < 0) {
//                    val updateNum = httpUtils.getAndDecode<NewDynamicCount>(NEW_DYNAMIC_COUNT).updateNum
//                    logger.info("新动态数: $updateNum")
//                    if (updateNum == 0) return@runCatching
//                } else {
//                    count = 0
//                }
                val newDynamic = httpUtils.getAndDecode<DynamicList>(NEW_DYNAMIC)
                val dynamics = newDynamic.dynamics ?: return@runCatching
                val uidList = dynamic.map { it.key }
                val newDynamicList = dynamics.stream().filter { it.timestamp > lastDynamic }
                    .filter { uidList.contains(it.uid) }
                    .filter { !history.contains(it.did) }
                    .collect(Collectors.toList())

                newDynamicList.forEach { di ->
                    val describe = di.describe
                    logger.info("新动态: ${di.uname}@${di.uid}=${di.did}")
                    val list = getDynamicContactList(describe.uid, di.describe.type == 8)
                    if (list != null && list.size > 0) {
                        val color = dynamic[describe.uid]?.color ?: "#d3edfa"
                        seleniumMutex.withLock {
                            list.sendMessage { di.build(it, color) }
                            history.add(describe.dynamicId)
//                            lastDynamic = di.timestamp
                        }
                    }
                }
            }.onSuccess {
                delay((interval..interval + 5000L).random())
            }.onFailure {
                logger.error("ERROR $it")
                findContact(BiliPluginConfig.admin)?.sendMessage("动态检测失败\n" + it.message)
                delay((60000L..120000L).random())
            }
        }
    }

    private fun liveJob() = launch {
        delay(20000L)
        while (isActive) {
            runCatching {
                logger.debug("LiveCheck")
                val liveList = httpUtils.getAndDecode<Live>(LIVE_LIST).liveList
                val newLiveList = liveList.stream().filter { it.liveTime > lastLive }
                    .filter { v -> dynamic.map { it.key }.contains(v.uid) }
                    .collect(Collectors.toList())
                newLiveList.forEach { ll ->
                    val list = getLiveContactList(ll.uid)
                    if (list != null && list.size != 0) {
                        seleniumMutex.withLock {
                            list.sendMessage(liveAtAll) { ll.build(it) }
                        }
                        lastLive = ll.liveTime
                    }
                }
            }.onSuccess {
                delay((20000..30000L).random())
            }.onFailure {
                logger.error("ERROR $it")
                findContact(BiliPluginConfig.admin)?.sendMessage("直播检测失败\n" + it.message)
                delay((60000L..120000L).random())
            }

        }
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

    private suspend inline fun MutableSet<String>.sendMessage(
//        info: String? = null,
        liveAtAll: Boolean = false,
        message: (contact: Contact) -> Message
    ) {
        val me = findContact(this.first())?.let { message(it) }
        if (me != null) {
            this.map { delegate ->
                runCatching {
                    requireNotNull(findContact(delegate)) { "找不到联系人" }.let {
//                        if (info == null) it.sendMessage(me)
//                        else it.sendMessage(me + "\n" + info)
                        if (liveAtAll && it is Group) {
                            it.sendMessage(me + "\n" + AtAll)
                        } else it.sendMessage(me)
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
fun findContact(del: String): Contact? = synchronized(contactMap) {
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
                for (friend in bot.friends) {
                    if (friend.id == delegate) return@compute friend
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


