package top.colter.mirai.plugin.bilibili.tasker

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.MessageSelectBuilder
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.whileSelectMessages
import net.mamoe.mirai.message.data.buildForwardMessage
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.nextMessage
import net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImageTo
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import top.colter.mirai.plugin.bilibili.*
import top.colter.mirai.plugin.bilibili.AtAllType.*
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic.save
import top.colter.mirai.plugin.bilibili.BiliConfig.accountConfig
import top.colter.mirai.plugin.bilibili.api.*
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.command.GroupOrContact
import top.colter.mirai.plugin.bilibili.command.subject
import top.colter.mirai.plugin.bilibili.data.DynamicMessage
import top.colter.mirai.plugin.bilibili.data.LiveMessage
import top.colter.mirai.plugin.bilibili.data.LoginData
import top.colter.mirai.plugin.bilibili.draw.loginQrCode
import top.colter.mirai.plugin.bilibili.tasker.DynamicMessageTasker.buildMessage
import top.colter.mirai.plugin.bilibili.tasker.LiveMessageTasker.buildMessage
import top.colter.mirai.plugin.bilibili.tasker.SendTasker.buildMessage
import top.colter.mirai.plugin.bilibili.utils.*
import java.net.URI

internal val logger by BiliBiliDynamic::logger

object BiliDataTasker {

    val mutex = Mutex()

    private val client = BiliClient()

    private val dynamic by BiliData::dynamic
    private val filter by BiliData::filter
    private val group by BiliData::group
    private val atAll by BiliData::atAll

    suspend fun listenAll(subject: String) = mutex.withLock {
        dynamic.forEach { (_, sub) ->
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
        if (uid == BiliBiliDynamic.mid) return null

        val attr = client.isFollow(uid)?.attribute
        if (attr == 0) {
            if (!accountConfig.autoFollow) return "未关注此用户"
            else {
                val res = client.follow(uid)
                if (res.code != 0) return "关注失败: ${res.message}"
                if (accountConfig.followGroup.isNotEmpty()) {
                    val res1 = client.groupAddUser(uid, BiliBiliDynamic.tagid)
                    if (res1.code != 0) logger.error("移动分组失败: ${res1.message}")
                }
                actionNotify("通知: 账号关注 $uid")
            }
        } else if (attr == 128) return "此账号已被拉黑"
        return null
    }

    suspend fun setColor(uid: Long, color: String) = mutex.withLock {
        color.split(";", "；").forEach {
            if (it.first() != '#' || it.length != 7) return@withLock "格式错误，请输入16进制颜色，如: #d3edfa"
        }
        dynamic[uid]?.color = color
        "设置完成"
    }

    suspend fun addSubscribe(uid: Long, subject: String) = mutex.withLock {
        if (isFollow(uid, subject)) return@withLock "之前订阅过这个人哦"
        if (dynamic[0]?.contacts?.contains(subject) == true) dynamic[0]?.contacts?.remove(subject)

        if (!dynamic.containsKey(uid)) {
            val m = followUser(uid)
            if (m != null) return@withLock m
            val u = client.userInfo(uid)
            dynamic[uid] = SubData(u?.name!!)
        }

        dynamic[uid]?.contacts?.apply {
            try {
                subject.toLong()
            } catch (e: NumberFormatException) {
                group[subject]?.contacts?.let {
                    removeAll(it)
                }
            }
            add(subject)
        }
        "订阅 ${dynamic[uid]?.name} 成功!"
    }

    suspend fun removeSubscribe(uid: Long, subject: String) = mutex.withLock {
        if (!isFollow(uid, subject)) return@withLock "还未订阅此人哦"
        val user = dynamic[uid]!!
        if (user.contacts.remove(subject)) {
            if (user.contacts.isEmpty()) dynamic.remove(uid)
            if (filter[subject]?.run {
                remove(uid)
                isEmpty()
            } == true) filter.remove(subject)
            if (atAll[subject]?.run {
                remove(uid)
                isEmpty()
            } == true) atAll.remove(subject)
            "对 ${user.name} 取消订阅成功"
        }else "取消订阅失败"
    }

    suspend fun removeAllSubscribe(subject: String) = mutex.withLock {
        filter.remove(subject)
        atAll.remove(subject)
        group.forEach { (_, g) -> g.contacts.remove(subject) }
        dynamic.count { (_, sub) -> sub.contacts.remove(subject) }
    }

    suspend fun addFilter(type: FilterType, mode: FilterMode?, regex: String?, uid: Long, subject: String) =
        mutex.withLock {
            if (!isFollow(uid, subject)) return@withLock "还未订阅此人哦"

            if (!filter.containsKey(subject)) filter[subject] = mutableMapOf()
            if (!filter[subject]!!.containsKey(uid)) filter[subject]!![uid] = DynamicFilter()

            val dynamicFilter = filter[subject]!![uid]!!
            when (type) {
                FilterType.TYPE -> {
                    if (mode != null) dynamicFilter.typeSelect.mode = mode
                    if (regex != null && regex != "") {
                        val t = when (regex) {
                            "动态" -> DynamicFilterType.DYNAMIC
                            "转发动态" -> DynamicFilterType.FORWARD
                            "视频" -> DynamicFilterType.VIDEO
                            "音乐" -> DynamicFilterType.MUSIC
                            "专栏" -> DynamicFilterType.ARTICLE
                            "直播" -> DynamicFilterType.LIVE
                            else -> return@withLock "没有这个类型 $regex"
                        }
                        dynamicFilter.typeSelect.list.add(t)
                    }
                }
                FilterType.REGULAR -> {
                    if (mode != null) dynamicFilter.regularSelect.mode = mode
                    if (regex != null && regex != "") dynamicFilter.regularSelect.list.add(regex)
                }
            }
            "设置成功"
        }

    suspend fun listFilter(uid: Long, subject: String) = mutex.withLock {
        if (!isFollow(uid, subject)) return@withLock "还未订阅此人哦"

        if (!(filter.containsKey(subject) && filter[subject]!!.containsKey(uid))) return@withLock "目标没有过滤器"

        buildString {
            //appendLine("当前目标过滤器: ")
            //appendLine()
            val typeSelect = filter[subject]!![uid]!!.typeSelect
            if (typeSelect.list.isNotEmpty()) {
                append("动态类型过滤器: ")
                appendLine(typeSelect.mode.value)
                typeSelect.list.forEachIndexed { index, type -> appendLine("  t$index: ${type.value}") }
                appendLine()
            }
            val regularSelect = filter[subject]!![uid]!!.regularSelect
            if (regularSelect.list.isNotEmpty()) {
                append("正则过滤器: ")
                appendLine(regularSelect.mode.value)
                regularSelect.list.forEachIndexed { index, reg -> appendLine("  r$index: $reg") }
                appendLine()
            }
        }
    }

    suspend fun delFilter(index: String, uid: Long, subject: String) = mutex.withLock {
        if (!isFollow(uid, subject)) return@withLock "还未订阅此人哦"
        if (!(filter.containsKey(subject) && filter[subject]!!.containsKey(uid))) return@withLock "当前目标没有过滤器"

        var i = 0
        runCatching {
            i = index.substring(1).toInt()
        }.onFailure {
            return@withLock "索引错误"
        }
        var flag = false
        val filter = if (index[0] == 't') {
            flag = true
            filter[subject]!![uid]!!.typeSelect.list
        } else if (index[0] == 'r') {
            filter[subject]!![uid]!!.regularSelect.list
        } else return@withLock "索引类型错误"
        if (filter.size < i) return@withLock "索引超出范围"
        val t = filter[i]
        filter.removeAt(i)

        if (flag) "已删除 ${(t as DynamicFilterType).value} 类型过滤"
        else "已删除 ${(t as String)} 正则过滤"
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

    suspend fun listUser(uid: Long? = null) = mutex.withLock {
        buildString {
            val user = mutableSetOf<String>()
            if (uid == null) {
                dynamic.forEach { (_, sub) ->
                    user.addAll(sub.contacts)
                }
            }else {
                val u = dynamic[uid]?: return@withLock "没有这个用户哦 [$uid]"
                appendLine("${u.name}[$uid]")
                appendLine()
                user.addAll(u.contacts)
            }
            val group = StringBuilder()
            val friend = StringBuilder()
            val gg = StringBuilder()
            user.forEach {
                try {
                    it.toLong()
                    findContact(it).apply {
                        when (this) {
                            is Group -> group.appendLine("$name@$id")
                            is Friend -> friend.appendLine("$nick@$id")
                        }
                    }
                }catch (e: NumberFormatException) {
                    gg.appendLine(it)
                }
            }
            appendLine("====群====")
            append(group.ifEmpty { "无\n" })
            appendLine("====好友====")
            append(friend.ifEmpty { "无\n" })
            appendLine("====分组====")
            append(gg.ifEmpty { "无\n" })
            appendLine()
            append("共 ${user.size} 名用户")
        }
    }

    suspend fun login(contact: Contact) {
        val loginData = client.getLoginUrl().data!!.decode<LoginData>()

        val image = loginQrCode(loginData.url)
        image.encodeToData()!!.bytes.toExternalResource().toAutoCloseable().sendAsImageTo(contact)
        contact.sendMessage("请使用BiliBili手机APP扫码登录 3分钟有效")

        runCatching {
            withTimeout(180000) {
                while (isActive) {
                    delay(3000)
                    val loginInfo = client.loginInfo(loginData.oauthKey!!)
                    if (loginInfo.status == true) {
                        val url = loginInfo.data!!.decode<LoginData>().url
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
                }
            }
        }.onFailure {
            contact.sendMessage("登录失败 ${it.message}")
        }
    }

    suspend fun listTemplate(type: String, subject: Contact) {
        val template = when (type) {
            "d" -> BiliConfig.templateConfig.dynamicPush
            "l" -> BiliConfig.templateConfig.livePush
            else -> {
                subject.sendMessage("类型错误 d:动态 l:直播")
                return
            }
        }

        // https://t.bilibili.com/385190177693666264
        val dynamic = if (type == "d") biliClient.getDynamicDetail("385190177693666264")?.buildMessage()!!
        else biliClient.getLive(1, 1)?.rooms?.first()?.buildMessage()!!

        subject.sendMessage(buildForwardMessage(subject) {
            var pt = 0
            subject.bot named dynamic.uname at dynamic.timestamp says if (type == "d") "动态推送模板" else "直播推送模板"
            subject.bot named dynamic.uname at dynamic.timestamp says "下面每个转发消息都代表一个模板推送效果"
            for (t in template) {
                subject.bot named dynamic.uname at dynamic.timestamp + pt says t.key
                subject.bot named dynamic.uname at dynamic.timestamp + pt says buildForwardMessage(subject) {
                    when (dynamic) {
                        is DynamicMessage -> dynamic.buildMessage(t.value, listOf(subject)).forEach {
                            subject.bot named dynamic.uname at dynamic.timestamp + pt says it
                        }
                        is LiveMessage -> dynamic.buildMessage(t.value, listOf(subject)).forEach {
                            subject.bot named dynamic.uname at dynamic.timestamp + pt says it
                        }
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

    suspend fun setTemplate(type: String, template: String, subject: String) = mutex.withLock {
        val pushTemplates = when (type) {
            "d" -> BiliConfig.templateConfig.dynamicPush
            "l" -> BiliConfig.templateConfig.livePush
            else -> return@withLock "类型错误 d:动态 l:直播"
        }
        val push = when (type) {
            "d" -> BiliData.dynamicPushTemplate
            "l" -> BiliData.livePushTemplate
            else -> return@withLock "类型错误 d:动态 l:直播"
        }
        if (pushTemplates.containsKey(template)) {
            push.forEach { (_, u) -> u.remove(subject) }
            if (!push.containsKey(template)) push[template] = mutableSetOf()
            push[template]!!.add(subject)
            "配置完成"
        } else "没有这个模板哦 $template"
    }

    private fun toAtAllType(type: String) =
        when (type.lowercase()) {
            "全部", "all", "a" -> ALL
            "全部动态", "dynamic", "d" -> DYNAMIC
            "直播", "live", "l" -> LIVE
            "视频", "video", "v" -> VIDEO
            "音乐", "music", "m" -> MUSIC
            "专栏", "article" -> ARTICLE
            else -> null
        }

    suspend fun addAtAll(type: String, uid: Long = 0L, target: GroupOrContact) = mutex.withLock {
        val atAllType = toAtAllType(type) ?: return "没有这个类型哦 [$type]"
        if (target.group == null) {
            if (target.contact !is Group) return "仅在群聊中有用哦"
            if (target.contact.botPermission.level == 0) return "Bot不为管理员, 无法使用At全体"
        }
        val list = atAll.getOrPut(target.subject) { mutableMapOf() }.getOrPut(uid) { mutableSetOf() }
        if (list.isEmpty()) {
            list.add(atAllType)
            atAll[target.subject]?.set(uid, list)
        } else when (atAllType) {
            ALL -> {
                list.clear()
                list.add(atAllType)
            }
            DYNAMIC -> {
                list.removeAll(listOf(ALL, VIDEO, MUSIC, ARTICLE))
                list.add(atAllType)
            }
            LIVE -> {
                list.remove(ALL)
                list.add(atAllType)
            }
            else -> {
                list.remove(ALL)
                list.remove(DYNAMIC)
                list.add(atAllType)
            }
        }
        "添加成功"
    }

    suspend fun delAtAll(type: String, uid: Long = 0L, subject: String) = mutex.withLock {
        val atAllType = toAtAllType(type) ?: return@withLock "没有这个类型哦 [$type]"
        if (atAll[subject]?.get(uid)?.remove(atAllType) == true) "删除成功" else "删除失败"
    }

    suspend fun listAtAll(uid: Long = 0L, subject: String) = mutex.withLock {
        val list = atAll[subject]?.get(uid)
        if (list.isNullOrEmpty()) return@withLock "没有At全体项哦"
        buildString { list.forEach { appendLine(it.value) } }
    }

    suspend fun createGroup(name: String, operator: Long) = mutex.withLock {
        if (!group.containsKey(name)) {
            if (name.matches("^[0-9]*$".toRegex())) return@withLock "分组名不能全为数字"
            group[name] = Group(name, operator)
            "创建成功"
        }else "分组名称重复"
    }

    suspend fun delGroup(name: String, operator: Long) = mutex.withLock {
        if (group.containsKey(name)) {
            if (group[name]!!.creator == operator) {
                dynamic.forEach { (_, s) -> s.contacts.remove(name) }
                BiliData.dynamicPushTemplate.forEach { (_, c) -> c.remove(name) }
                BiliData.livePushTemplate.forEach { (_, c) -> c.remove(name) }
                filter.remove(name)
                atAll.remove(name)
                group.remove(name)
                "删除成功"
            }else "无权删除"
        }else "没有此分组 [$name]"
    }

    suspend fun listGroup(name: String? = null, operator: Long) = mutex.withLock {
        if (name == null) {
            group.values.filter {
                operator == BiliConfig.admin || operator == it.creator || it.admin.contains(operator)
            }.joinToString("\n") {
                "${it.name}@${findContactAll(it.creator)?.name?:it.creator}"
            }.ifEmpty { "没有创建或管理任何分组哦" }
        } else {
            group[name]?.toString() ?: "没有此分组哦"
        }
    }

    suspend fun setGroupAdmin(name: String, contacts: String, operator: Long) = mutex.withLock {
        if (group.containsKey(name)) {
            if (group[name]!!.creator == operator) {
                var failMsg = ""
                group[name]?.admin?.addAll(contacts.split(",","，").map {
                    findContactAll(it).run {
                        if (this != null && this is Friend) id else {
                            failMsg += "$it, "
                            null
                        }
                    }
                }.filterNotNull().toSet())
                if (failMsg.isEmpty()) "添加成功"
                else "[$failMsg] 添加失败"
            }else "无权添加"
        }else "没有此分组 [$name]"
    }

    suspend fun banGroupAdmin(name: String, contacts: String, operator: Long) = mutex.withLock {
        if (group.containsKey(name)) {
            if (group[name]!!.creator == operator) {
                var failMsg = ""
                val admin = group[name]!!.admin
                contacts.split(",","，").map {
                    try {
                        it.toLong()
                    }catch (e: NumberFormatException) {
                        failMsg += "$it, "
                        null
                    }
                }.filterNotNull().toSet().forEach {
                    if (!admin.remove(it)) failMsg += "$it, "
                }
                if (failMsg.isEmpty()) "删除成功"
                else "[$failMsg] 删除失败"
            }else "无权删除"
        }else "没有此分组 [$name]"
    }

    suspend fun pushGroupContact(name: String, contacts: String, operator: Long) = mutex.withLock {
        if (group.containsKey(name)) {
            if (checkGroupPerm(name, operator)) {
                var failMsg = ""
                group[name]?.contacts?.addAll(contacts.split(",","，").map {
                    findContactAll(it)?.delegate.apply {
                        if (this == null) failMsg += "$it, "
                    }
                }.filterNotNull().toSet())
                if (failMsg.isEmpty()) "添加成功"
                else "[$failMsg] 添加失败"
            }else "无权添加"
        }else "没有此分组 [$name]"
    }

    suspend fun delGroupContact(name: String, contacts: String, operator: Long) = mutex.withLock {
        if (group.containsKey(name)) {
            if (checkGroupPerm(name, operator)) {
                var failMsg = ""
                group[name]?.contacts?.removeAll(contacts.split(",","，").map {
                    findContactAll(it)?.let {
                        failMsg += "$it, "
                        it.delegate
                    } ?: ""
                }.filter { it.isNotEmpty() }.toSet())
                if (failMsg.isEmpty()) "删除成功"
                else "[$failMsg] 删除失败"
            }else "无权删除"
        }else "没有此分组 [$name]"
    }

    fun checkGroupPerm(name: String, operator: Long): Boolean =
        group[name]?.creator == operator || group[name]?.admin?.contains(operator) == true


    suspend fun config(event: MessageEvent, uid: Long = 0L, contact: Contact) {
        val subject = event.subject
        if (uid != 0L && !((dynamic.containsKey(uid) && dynamic[uid]!!.contacts.contains(contact.delegate)))) {
            subject.sendMessage("没有订阅这个人哦 [$uid]")
            return
        }
        val user = if (uid != 0L) dynamic[uid] else null
        val configMap = mutableMapOf<String, String>()

        subject.sendMessage(buildString {
            appendLine("配置: ")
            append("用户: ")
            appendLine(if (uid == 0L) "全局" else user?.name)
            append("目标: ")
            appendLine(if (subject.id == contact.id) "当前环境" else contact.name)
            appendLine()
            appendLine("当前可配置项:")
            var i = 1
            if (contact is Group) {
                configMap[i.toString()] = "ATALL"
                val aa = atAll[contact.delegate]?.get(uid)?.isNotEmpty()
                appendLine("  $i: At全体 [${aa ?: false}]")
                appendLine("      $i.1: 当前At全体项")
                appendLine("      $i.2: 添加At全体")
                appendLine("      $i.2: 删除At全体")
                i++
            }
            if (uid != 0L) {
                configMap[i.toString()] = "COLOR"
                appendLine("  ${i++}: 主题色 [${user?.color ?: BiliConfig.imageConfig.defaultColor}]")
            }
            if (uid == 0L) {
                val cdl = BiliData.dynamicPushTemplate.filter { it.value.contains(contact.delegate) }.map { it.key }
                val currDynamic = if (cdl.isNotEmpty()) cdl.first() else BiliConfig.templateConfig.defaultDynamicPush
                val cll = BiliData.livePushTemplate.filter { it.value.contains(contact.delegate) }.map { it.key }
                val currLive = if (cll.isNotEmpty()) cll.first() else BiliConfig.templateConfig.defaultLivePush
                configMap[i.toString()] = "PUSH"
                appendLine("  $i: 推送模板")
                appendLine("      $i.1: 动态推送模板 [$currDynamic]")
                appendLine("      $i.2: 直播推送模板 [$currLive]")
                i++
            }

            val filter = BiliData.filter[contact.delegate]?.get(uid)
            val mode = if (filter == null) "无过滤器" else
                "类型: ${filter.typeSelect.mode.value} | 正则: ${filter.regularSelect.mode.value}"

            configMap[i.toString()] = "FILTER"
            appendLine("  $i: 过滤器")
            appendLine("      $i.1: 过滤器列表")
            appendLine("      $i.2: 添加类型过滤器")
            appendLine("      $i.3: 添加正则过滤器")
            appendLine("      $i.4: 切换过滤模式 [$mode]")
            appendLine("      $i.5: 删除过滤器")
            appendLine()
            append("[中括号]内为当前值\n请输入编号, 2分钟未回复自动退出\n或回复 退出 来主动退出")
        })

        while (true) {
            var cc = 0
            var rres: String? = null
            var selectContent = ""
            var selectConfig = ""
            event.whileSelectMessages {
                "退出" {
                    event.subject.sendMessage("已退出")
                    false
                }
                configMap.forEach { (t, u) ->
                    startsWith(t) {
                        selectContent = message.content
                        selectConfig = u
                        rres = ""
                        false
                    }
                }
                default {
                    cc++
                    subject.sendMessage("没有这个选项哦${if (cc < 2) ", 请重新输入" else ", 超出重试次数, 退出"}")
                    cc < 2
                }
                timeout(120_000) { false }
            }
            if (rres == null) return

            when (selectConfig) {
                "ATALL" -> {
                    val b = selectContent.split(".").last()
                    when (b) {
                        "1" -> subject.sendMessage(listAtAll(uid, contact.delegate))
                        "2" -> {
                            subject.sendMessage(buildString {
                                appendLine("请选择要At全体的内容: ")
                                appendLine("  全部")
                                appendLine("  ├─ 全部动态")
                                appendLine("  │   ├─ 视频")
                                appendLine("  │   ├─ 音乐")
                                appendLine("  │   └─ 专栏")
                                appendLine("  └─ 直播")
                            })

                            var c = 0
                            var res: String? = null
                            var selectType = ""
                            event.whileSelectMessages {
                                "退出" {
                                    event.subject.sendMessage("已退出")
                                    false // 停止循环
                                }
                                AtAllType.values().forEach { t ->
                                    t.value {
                                        selectType = t.value
                                        res = ""
                                        false
                                    }
                                }
                                default {
                                    c++
                                    subject.sendMessage("没有这个选项哦${if (c < 2) ", 请重新输入" else ", 超出重试次数, 退出"}")
                                    c < 2
                                }
                                timeout(120_000) { false }
                            }
                            if (res == null) return
                            subject.sendMessage(addAtAll(selectType, uid, GroupOrContact(contact)))
                        }
                        "3" -> {
                            val list = atAll[contact.delegate]?.get(uid)
                            if (list == null || list.isEmpty()) subject.sendMessage("没有At全体哦")
                            subject.sendMessage("At全体项:\n" + listAtAll(uid, contact.delegate) + "\n请回复要删除的项")
                            val type = event.nextMessage().content
                            subject.sendMessage(delAtAll(type, uid, contact.delegate))
                        }
                    }
                }

                "COLOR" -> {
                    subject.sendMessage("请输入16进制颜色，例如: #d3edfa")
                    var res: String? = null
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
                            } else {
                                subject.sendMessage(setColor(uid, color))
                                count = 2
                                res = ""
                            }
                            ++count < 2
                        }
                        timeout(120_000) { false }
                    }
                    if (res == null) return
                }

                "PUSH" -> {
                    val b = selectContent.split(".").last()
                    val template = when (b) {
                        "1" -> BiliConfig.templateConfig.dynamicPush
                        "2" -> BiliConfig.templateConfig.livePush
                        else -> {
                            subject.sendMessage("没有这个选项哦")
                            null
                        }
                    }
                    if (template != null) {
                        subject.sendMessage("请选择一个推送模板, 回复模板名\n生成模板需要一定时间...")
                        listTemplate(if (b == "1") "d" else "l", subject)
                        var c = 0
                        var res: String? = null
                        var selectTemplate = ""
                        event.whileSelectMessages {
                            "退出" {
                                event.subject.sendMessage("已退出")
                                false // 停止循环
                            }
                            template.forEach { (t, _) ->
                                t {
                                    selectTemplate = t
                                    res = ""
                                    false
                                }
                            }
                            default {
                                c++
                                subject.sendMessage("没有这个选项哦${if (c < 2) ", 请重新输入" else ", 超出重试次数, 退出"}")
                                c < 2
                            }
                            timeout(120_000) { false }
                        }
                        if (res == null) return
                        subject.sendMessage(setTemplate(if (b == "1") "d" else "l", selectTemplate, subject.delegate))
                    }
                }

                "FILTER" -> {
                    val b = selectContent.split(".").last()
                    val filter = BiliData.filter[contact.delegate]?.get(uid)
                    when (b) {
                        "1" -> subject.sendMessage(listFilter(uid, contact.delegate))
                        "2" -> {
                            val mode = filter?.typeSelect?.mode?.value ?: "黑名单"
                            val type = DynamicFilterType.values().joinToString("\n    ") { it.value }
                            subject.sendMessage("当前过滤器类型: $mode\n支持的类型: \n    $type\n请回复要过滤的类型")

                            var c = 0
                            var res: String? = null
                            var selectType: String? = null
                            event.whileSelectMessages {
                                "退出" {
                                    event.subject.sendMessage("已退出")
                                    false // 停止循环
                                }
                                DynamicFilterType.values().forEach { t ->
                                    t.value {
                                        selectType = t.value
                                        res = ""
                                        false
                                    }
                                }
                                default {
                                    c++
                                    subject.sendMessage("没有这个选项哦${if (c < 2) ", 请重新输入" else ", 超出重试次数, 退出"}")
                                    c < 2
                                }
                                timeout(120_000) { false }
                            }
                            if (res == null) return
                            subject.sendMessage(addFilter(FilterType.TYPE, null, selectType, uid, contact.delegate))
                        }
                        "3" -> {
                            try {
                                val mode = filter?.regularSelect?.mode?.value ?: "黑名单"
                                subject.sendMessage("当前过滤器类型: $mode\n请回复过滤文本或正则")
                                val reg = event.nextMessage(120_000).content
                                if (reg != "")
                                    subject.sendMessage(addFilter(FilterType.REGULAR, null, reg, uid, contact.delegate))
                            } catch (e: Exception) {
                                return
                            }
                        }
                        "4" -> {
                            val typeMode = filter?.typeSelect?.mode?.value ?: "黑名单"
                            val regMode = filter?.regularSelect?.mode?.value ?: "黑名单"
                            subject.sendMessage("类型过滤器: $typeMode\n正则过滤器: $regMode\n请选择要切换的过滤的类型\nt: 类型过滤器\nr: 正则过滤器")

                            var c = 0
                            var res: String? = null
                            var selectType: FilterType? = null
                            var selectMode: FilterMode? = null
                            event.whileSelectMessages {
                                "退出" {
                                    event.subject.sendMessage("已退出")
                                    false // 停止循环
                                }
                                "t" {
                                    selectType = FilterType.TYPE
                                    selectMode = filter?.typeSelect?.mode ?: FilterMode.BLACK_LIST
                                    res = ""
                                    false
                                }
                                "r" {
                                    selectType = FilterType.REGULAR
                                    selectMode = filter?.regularSelect?.mode ?: FilterMode.BLACK_LIST
                                    res = ""
                                    false
                                }
                                default {
                                    c++
                                    subject.sendMessage("没有这个选项哦${if (c < 2) ", 请重新输入" else ", 超出重试次数, 退出"}")
                                    c < 2
                                }
                                timeout(120_000) { false }
                            }
                            if (res == null) return
                            if (selectType != null) {
                                selectMode =
                                    if (selectMode == FilterMode.BLACK_LIST) FilterMode.WHITE_LIST else FilterMode.BLACK_LIST
                                subject.sendMessage(addFilter(selectType!!, selectMode, null, uid, contact.delegate))
                            }
                        }
                        "5" -> {
                            try {
                                subject.sendMessage(listFilter(uid, contact.delegate))
                                val reg = event.nextMessage(120_000).content
                                subject.sendMessage(delFilter(reg, uid, contact.delegate))
                            } catch (e: Exception) {
                                return
                            }
                        }
                    }
                }
            }
            subject.sendMessage("配置结束\n输入上方编号以继续\n不回复或回复 退出 来退出")
        }
    }

    suspend inline fun <reified T : MessageEvent> T.whileSelect(
        count: Int = 2,
        timeout: Long = 120_000,
        defaultReply: String = "没有这个选项哦",
        crossinline selectBuilder: MessageSelectBuilder<T, Boolean>.() -> Unit
    ): String? {
        var c = 0
        var res: String? = null
        whileSelectMessages {
            "退出" {
                subject.sendMessage("已退出")
                res = "退出"
                false
            }
            apply(selectBuilder)
            default {
                c++
                subject.sendMessage("$defaultReply${if (c < count) ", 请重新输入" else ", 超出重试次数, 退出"}")
                if (c >= count) res = "超次"
                c < count
            }
            timeout(timeout) {
                res = "超时"
                false
            }
        }
        return res
    }

    private fun isFollow(uid: Long, subject: String) =
        uid == 0L || (dynamic.containsKey(uid) && dynamic[uid]!!.contacts.contains(subject))

}