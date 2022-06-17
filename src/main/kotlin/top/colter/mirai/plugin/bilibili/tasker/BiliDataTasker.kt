package top.colter.mirai.plugin.bilibili.tasker

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.selectMessages
import net.mamoe.mirai.event.whileSelectMessages
import net.mamoe.mirai.message.data.buildForwardMessage
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImageTo
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import top.colter.mirai.plugin.bilibili.*
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic.save
import top.colter.mirai.plugin.bilibili.BiliConfig.accountConfig
import top.colter.mirai.plugin.bilibili.api.*
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.DynamicMessage
import top.colter.mirai.plugin.bilibili.data.DynamicType
import top.colter.mirai.plugin.bilibili.draw.loginQrCode
import top.colter.mirai.plugin.bilibili.tasker.SendTasker.buildMessage
import top.colter.mirai.plugin.bilibili.utils.delegate
import top.colter.mirai.plugin.bilibili.utils.findContact
import java.net.URI
import java.time.Instant

internal val logger by BiliBiliDynamic::logger

object BiliDataTasker {

    val mutex = Mutex()

    val client = BiliClient()

    val dynamic by BiliData::dynamic

    val filter by BiliData::filter

    suspend fun listenAll(subject: String) = mutex.withLock {
        dynamic.forEach { (uid, sub) ->
            if (subject in sub.contacts) {
                sub.contacts.remove(subject)
            }
        }
        val user = dynamic[0]
        user?.contacts?.add(subject)
    }

    suspend fun cancelListen(subject: String) = mutex.withLock {
        dynamic[0]?.contacts?.remove(subject)
    }

    private suspend fun followUser(uid: Long): String? {
        if (uid == BiliBiliDynamic.mid) {
            return null
        }

        val attr = client.isFollow(uid)?.attribute
        if (attr == 0) {
            if (!accountConfig.autoFollow) {
                return "未关注此用户"
            } else {
                val res = client.follow(uid)
                if (res.code != 0) {
                    return "关注失败: ${res.message}"
                }
                if (accountConfig.followGroup.isNotEmpty()) {
                    val res1 = client.groupAddUser(uid, BiliBiliDynamic.tagid)
                    if (res1.code != 0) {
                        logger.error("移动分组失败: ${res1.message}")
                    }
                }
            }
        } else if (attr == 128) {
            return "此账号已被拉黑"
        }
        return null
    }

    fun setColor(uid: Long, color: String): String {
        color.split(";","；").forEach{
            if (it.first() != '#' || it.length != 7) {
                return "格式错误，请输入16进制颜色，如: #d3edfa"
            }
        }
        dynamic[uid]?.color = color
        return "设置完成"
    }

    suspend fun addSubscribe(uid: Long, subject: String): String{
        if (isFollow(uid, subject)){
            return "之前订阅过这个人哦"
        }

        if (dynamic[0]?.contacts?.contains(subject) == true) {
            dynamic[0]?.contacts?.remove(subject)
        }
        if (!dynamic.containsKey(uid)){
            val m = followUser(uid)
            if (m != null) return m
            val u = client.userInfo(uid)
            dynamic[uid] = SubData(u?.name!!)
        }

        dynamic[uid]!!.contacts.add(subject)
        return "订阅 ${dynamic[uid]?.name} 成功!"
    }

    suspend fun addFilter(type: FilterType, mode: FilterMode?, regex: String?, uid: Long, subject: String) : String{
        if (!isFollow(uid, subject)){
            return "还未订阅此人哦"
        }

        if (!filter.containsKey(subject)){
            filter[subject] = mutableMapOf()
        }
        if (!filter[subject]!!.containsKey(uid)){
            filter[subject]!![uid] = DynamicFilter()
        }

        val dynamicFilter = filter[subject]!![uid]!!
        when (type){
            FilterType.TYPE -> {
                if (mode != null) dynamicFilter.typeSelect.mode = mode
                if (regex != null && regex != ""){
                    val t = when (regex){
                        "动态" -> DynamicFilterType.DYNAMIC
                        "转发动态" -> DynamicFilterType.FORWARD
                        "视频" -> DynamicFilterType.VIDEO
                        "音乐" -> DynamicFilterType.MUSIC
                        "专栏" -> DynamicFilterType.ARTICLE
                        "直播" -> DynamicFilterType.LIVE
                        else -> return "没有这个类型 $regex"
                    }
                    dynamicFilter.typeSelect.list.add(t)
                }
            }
            FilterType.REGULAR -> {
                if (mode != null) dynamicFilter.regularSelect.mode = mode
                if (regex != null && regex != ""){
                    dynamicFilter.regularSelect.list.add(regex)
                }
            }
        }
        return "设置成功"
    }

    fun listFilter(uid: Long, subject: String): String {
        if (!isFollow(uid, subject)){
            return "还未订阅此人哦"
        }
        if (!(filter.containsKey(subject) && filter[subject]!!.containsKey(uid))) {
            return "当前目标没有过滤器"
        }

        return buildString {
            appendLine("当前目标过滤器: ")
            appendLine()

            val typeSelect = filter[subject]!![uid]!!.typeSelect
            if (typeSelect.list.isNotEmpty()) {
                appendLine("动态类型过滤器: ")
                appendLine("模式: ${typeSelect.mode.value}")
                typeSelect.list.forEachIndexed { index, type ->
                    appendLine(" t$index: ${type.value}")
                }
            }
            val regularSelect = filter[subject]!![uid]!!.regularSelect
            if (regularSelect.list.isNotEmpty()) {
                appendLine("正则过滤器: ")
                appendLine("模式: ${regularSelect.mode.value}")
                regularSelect.list.forEachIndexed { index, reg ->
                appendLine(" r$index: $reg")
                }
            }
        }
    }

    fun delFilter(index: String, uid: Long, subject: String) :String {
        if (!isFollow(uid, subject)){
            return "还未订阅此人哦"
        }
        if (!(filter.containsKey(subject) && filter[subject]!!.containsKey(uid))) {
            return "当前目标没有过滤器"
        }

        var i = 0
        runCatching {
            i = index.substring(1).toInt()
        }.onFailure {
            return "索引错误"
        }
        var flag = false
        val filter = if (index[0] == 't') {
            flag = true
            filter[subject]!![uid]!!.typeSelect.list
        } else if (index[0] == 'r') {
            filter[subject]!![uid]!!.regularSelect.list
        } else {
            return "索引类型错误"
        }
        if (filter.size < i) return "索引超出范围"
        val t = filter[i]
        filter.removeAt(i)

        return if (flag){
            "已删除 ${(t as DynamicFilterType).value} 类型过滤"
        }else{
            "已删除 ${(t as String)} 正则过滤"
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

    suspend fun listAll() = mutex.withLock {
        var count = 0
        buildString {
            appendLine("名称@UID#订阅人数")
            appendLine()
            dynamic.forEach { (uid, sub) ->
                appendLine("${sub.name}@$uid#${sub.contacts.size}")
                count++
            }
            appendLine()
            append("共 $count 个订阅")
        }
    }

    suspend fun listUser() = mutex.withLock {
        buildString {
            val user = mutableSetOf<String>()
            dynamic.forEach { (uid, sub) ->
                user.addAll(sub.contacts)
            }
            val group = StringBuilder()
            val friend = StringBuilder()
            user.forEach {
                findContact(it).apply {
                    when (this) {
                        is Group -> group.appendLine("${name}@${id}")
                        is Friend -> friend.appendLine("${nick}@${id}")
                    }
                }
            }
            appendLine("====群====")
            append(group.ifEmpty { "无\n" })
            appendLine("====好友====")
            append(friend.ifEmpty { "无\n" })
            appendLine()
            append("共 ${user.size} 名用户")
        }
    }

    suspend fun login(contact: Contact) {
        val loginData = client.getLoginUrl().data!!

        val image = loginQrCode(loginData.url)
        image.encodeToData()!!.bytes.toExternalResource().toAutoCloseable().sendAsImageTo(contact)
        contact.sendMessage("请使用BiliBili手机APP扫码登录 3分钟有效")

        runCatching {
            withTimeout(180000) {
                while (true) {
                    val loginInfo = client.loginInfo(loginData.oauthKey!!)

                    if (loginInfo.status == true) {
                        val url = loginInfo.data!!.url
                        val querys = URI(url).query.split("&")
                        val cookie = buildString {
                            querys.forEach {
                                if (it.contains("SESSDATA") || it.contains("bili_jct")) append("$it; ")
                            }
                        }
                        accountConfig.cookie = cookie
                        BiliConfig.save()
                        BiliBiliDynamic.cookie.parse(cookie)
                        initTagid()
                        //getHistoryDynamic()
                        contact.sendMessage("登录成功!")
                        break
                    }
                    delay(3000)
                }
            }
        }.onFailure {
            contact.sendMessage("登录失败 ${it.message}")
        }

    }

    suspend fun listTemplate(type: String, subject: Contact){
        val template = when (type){
            "d" -> BiliConfig.templateConfig.dynamicPush
            "l" -> BiliConfig.templateConfig.livePush
            else -> {
                subject.sendMessage("类型错误 d:动态 l:直播")
                return
            }
        }

        // https://t.bilibili.com/385190177693666264
        val dynamic = DynamicMessage(
            "100000000000114514",
            114514,
            "哼啊啊啊",
            DynamicType.DYNAMIC_TYPE_WORD,
            "2114年5月14日 11:45:14",
            Instant.now().epochSecond.toInt(),
            "测试内容测试内容测试内容",
            null,
            listOf(DynamicMessage.Link("", "https://t.bilibili.com/100000000000114514"))
        )

        subject.sendMessage(buildForwardMessage(subject){
            var pt = 0
            subject.bot named dynamic.uname at dynamic.timestamp says if (type=="d") "动态推送模板" else "直播推送模板"
            subject.bot named dynamic.uname at dynamic.timestamp says "下面每个转发消息都代表一个模板推送效果"
            for (t in template){
                subject.bot named dynamic.uname at dynamic.timestamp + pt says t.key
                subject.bot named dynamic.uname at dynamic.timestamp + pt says buildForwardMessage(subject){
                    dynamic.buildMessage(t.value, subject).forEach {
                        subject.bot named dynamic.uname at dynamic.timestamp + pt says it
                    }
                }
                pt += 86400
            }
            //subject.bot named dynamic.uname at dynamic.timestamp + pt says buildString {
            //    appendLine("请回复模板名: ")
            //    template.keys.forEach { appendLine(it) }
            //}
        })
    }

    fun setTemplate(type: String, template: String, subject: Contact): String{
        val pushTemplates = when (type){
            "d" -> BiliConfig.templateConfig.dynamicPush
            "l" -> BiliConfig.templateConfig.livePush
            else -> return "类型错误 d:动态 l:直播"
        }
        val push = when(type){
            "d" -> BiliData.dynamicPushTemplate
            "l" -> BiliData.livePushTemplate
            else -> return "类型错误 d:动态 l:直播"
        }
        return if (pushTemplates.containsKey(template)){
            push.forEach { (_, u) ->
                u.remove(subject.id)
            }
            if (!push.containsKey(template)) push[template] = mutableSetOf()
            push[template]!!.add(subject.id)
            "配置完成"
        }else {
            "没有这个模板哦 $template"
        }
    }

    suspend fun config(event: MessageEvent, uid: Long = 0L){

        val subject = event.subject
        val delegate = event.subject.delegate
        val sender = event.sender

        if (!(dynamic.containsKey(uid) && dynamic[uid]!!.contacts.contains(delegate))){
            subject.sendMessage("没有订阅这个人哦 [$uid]")
            return
        }

        val user = dynamic[uid]!!

        val configMap = mutableMapOf<String, String>()

        subject.sendMessage(buildString {
            append("配置: ")
            appendLine(if (uid == 0L) "群全局" else user.name)
            appendLine()
            appendLine("当前可配置项:")
            var i = 1
            if (user.color == null) {
                configMap[i.toString()] = "COLOR"
                appendLine("  ${i++}: 主题色")
            }
            if (uid == 0L) {
                configMap[i.toString()] = "PUSH"
                appendLine("  $i: 推送模板")
                appendLine("    $i.1: 动态推送模板")
                appendLine("    $i.2: 直播推送模板")
                i++
            }
            configMap[i.toString()] = "FILTER"
            appendLine("  $i: 过滤器")
            appendLine()
            append("请输入编号, 2分钟未回复自动退出")
        })

        val selectConfig = event.selectMessages {
            configMap.forEach { (t, u) ->
                t { u }
            }
            defaultReply { "没有这个选项哦" }
            timeout(120_000)
        }

        //val selectConfig = event.selectMessages {
        //    startsWith("")
        //    default { message.content }
        //    timeout(60_000)
        //}

        when (selectConfig){
            "COLOR" -> {
                subject.sendMessage("请输入16进制颜色，例如: #d3edfa")

                var count = 0
                event.whileSelectMessages {
                    "退出" {
                        event.subject.sendMessage("已退出")
                        false // 停止循环
                    }
                    default {
                        val color = message.content
                        if (color.first() != '#' || color.length != 7) {
                            subject.sendMessage("格式错误，请输入16进制颜色，例如: #d3edfa")
                        }else{
                            subject.sendMessage(setColor(uid, color))
                            count = 2
                        }
                        count++
                        count < 2
                    }
                    timeout(120_000) { false }
                }
            }
            "PUSH" -> {
                subject.sendMessage("请选择一个推送模板")

                val template = BiliConfig.templateConfig.dynamicPush

                listTemplate("d", subject)

                val selectTemplate = event.selectMessages {
                    template.forEach { (t, _) ->
                        t { t }
                    }
                    defaultReply { "没有这个模板哦" }
                    timeout(120_000)
                }

                setTemplate("dynamic", selectTemplate, subject)
            }
            "FILTER" -> {


            }
        }


    }

    fun isFollow(uid: Long, subject: String) =
        dynamic.containsKey(uid) && dynamic[uid]!!.contacts.contains(subject)


}