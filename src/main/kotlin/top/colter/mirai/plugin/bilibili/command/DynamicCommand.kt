package top.colter.mirai.plugin.bilibili.command

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CommandSenderOnMessage
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.descriptor.CommandArgumentParserException
import net.mamoe.mirai.console.command.descriptor.buildCommandArgumentContext
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImageTo
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic.crossContact
import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.FilterMode
import top.colter.mirai.plugin.bilibili.FilterType
import top.colter.mirai.plugin.bilibili.api.getDynamicDetail
import top.colter.mirai.plugin.bilibili.api.getLive
import top.colter.mirai.plugin.bilibili.api.getUserNewDynamic
import top.colter.mirai.plugin.bilibili.data.DynamicDetail
import top.colter.mirai.plugin.bilibili.data.LiveDetail
import top.colter.mirai.plugin.bilibili.tasker.BiliDataTasker
import top.colter.mirai.plugin.bilibili.utils.*

object DynamicCommand : CompositeCommand(
    owner = BiliBiliDynamic,
    "bili",
    description = "动态指令",
    overrideContext = buildCommandArgumentContext {
        GroupOrContact::class with GroupOrContactParser
    }
) {

    private val admin by BiliConfig::admin

    @SubCommand("h", "help", "帮助", "menu")
    suspend fun CommandSender.help() {
        loadResourceBytes("image/HELP.png").toExternalResource().toAutoCloseable().sendAsImageTo(Contact())
    }

    @SubCommand("color", "颜色")
    suspend fun CommandSender.color(user: String, color: String) {
        matchUser(user) {
            BiliDataTasker.setColor(it, color)
        }?.let {
            sendMessage(it)
            actionNotify(this.subject?.id, name, user, "修改主题色", it)
        }
    }

    @SubCommand("add", "添加", "订阅")
    suspend fun CommandSender.add(uid: Long, target: GroupOrContact = GroupOrContact(Contact())) {
        if (checkPerm(target)) {
            BiliDataTasker.addSubscribe(uid, target.subject).let {
                sendMessage(it)
                actionNotify(this.subject?.id, name, target.name, "订阅", it)
            }
        }
    }

    @SubCommand("del", "删除")
    suspend fun CommandSender.del(user: String, target: GroupOrContact = GroupOrContact(Contact())) {
        if (checkPerm(target)) {
            matchUser(user) {
                BiliDataTasker.removeSubscribe(it, target.subject)
            }?.let {
                sendMessage(it)
                actionNotify(this.subject?.id, name, target.name, "取消订阅", it)
            }
        }
    }

    @SubCommand("delAll", "删除全部订阅")
    suspend fun CommandSender.delAll(target: GroupOrContact = GroupOrContact(Contact())) {
        if (checkPerm(target)) {
            val msg = BiliDataTasker.removeAllSubscribe(target.subject).let { "删除订阅成功! 共删除 $it 个订阅" }
            sendMessage(msg)
            actionNotify(this.subject?.id, name, target.name, "取消全部订阅", msg)
        }
    }

    @SubCommand("list", "列表")
    suspend fun CommandSender.list(target: GroupOrContact = GroupOrContact(Contact())) {
        if (checkPerm(target)) {
            sendMessage(BiliDataTasker.list(target.subject))
        }
    }

    @SubCommand("listAll", "la", "全部订阅列表")
    suspend fun CommandSender.listAll() {
        if (admin == Contact().id || admin == user?.id || user == null)
            sendMessage(BiliDataTasker.listAll())
        else sendMessage("仅bot管理员可获取")
    }

    @SubCommand("listUser", "lu", "用户列表")
    suspend fun CommandSender.listUser(user: String = "") {
        if (admin == Contact().id || admin == this.user?.id || this.user == null)
            if (user.isEmpty()) {
                sendMessage(BiliDataTasker.listUser())
            }else {
                matchUser(user) {
                    BiliDataTasker.listUser(it)
                }?.let { sendMessage(it) }
            }
        else sendMessage("仅bot管理员可获取")
    }

    @SubCommand("filterMode", "fm", "过滤模式")
    suspend fun CommandSender.filterMode(type: String, mode: String, uid: Long = 0L, target: GroupOrContact = GroupOrContact(Contact())) {
        if (checkPerm(target)) {
            sendMessage(
                BiliDataTasker.addFilter(
                    if (type == "t") FilterType.TYPE else FilterType.REGULAR,
                    if (mode == "w") FilterMode.WHITE_LIST else FilterMode.BLACK_LIST,
                    null, uid, target.subject
                )
            )
        }
    }

    @SubCommand("filterType", "ft", "类型过滤")
    suspend fun CommandSender.filterType(type: String, uid: Long = 0L, target: GroupOrContact = GroupOrContact(Contact())) {
        if (checkPerm(target)) {
            sendMessage(BiliDataTasker.addFilter(FilterType.TYPE, null, type, uid, target.subject))
        }
    }

    @SubCommand("filterReg", "fr", "正则过滤")
    suspend fun CommandSender.filterReg(reg: String, uid: Long = 0L, target: GroupOrContact = GroupOrContact(Contact())) {
        if (checkPerm(target)) {
            sendMessage(BiliDataTasker.addFilter(FilterType.REGULAR, null, reg, uid, target.subject))
        }
    }

    @SubCommand("filterList", "fl", "过滤列表")
    suspend fun CommandSender.filterList(uid: Long = 0L, target: GroupOrContact = GroupOrContact(Contact())) {
        if (checkPerm(target)) {
            sendMessage(BiliDataTasker.listFilter(uid, target.subject))
        }
    }

    @SubCommand("filterDel", "fd", "过滤删除")
    suspend fun CommandSender.filterDel(index: String, uid: Long = 0L, target: GroupOrContact = GroupOrContact(Contact())) {
        if (checkPerm(target)) {
            sendMessage(BiliDataTasker.delFilter(index, uid, target.subject))
        }
    }

    @SubCommand("templateList", "tl", "模板列表")
    suspend fun CommandSenderOnMessage<*>.templateList(type: String = "d") {
        subject?.sendMessage("少女祈祷中...")
        BiliDataTasker.listTemplate("d", Contact())
        BiliDataTasker.listTemplate("l", Contact())
    }

    @SubCommand("template", "t", "模板")
    suspend fun CommandSender.template(type: String, template: String, target: GroupOrContact = GroupOrContact(Contact())) {
        if (checkPerm(target)) {
            sendMessage(BiliDataTasker.setTemplate(type, template, target.subject))
        }
    }

    @SubCommand("login", "登录")
    suspend fun CommandSenderOnMessage<*>.login() {
        val subject = Contact()
        if (BiliConfig.admin == subject.id || BiliConfig.admin == user?.id)
            BiliDataTasker.login(subject)
        else sendMessage("仅bot管理员可进行登录")
    }

    @SubCommand("atall", "aa", "at全体")
    suspend fun CommandSender.atall(type: String = "a", user: String = "0", target: GroupOrContact = GroupOrContact(Contact())) {
        if (checkPerm(target)) {
            matchUser(user) {
                BiliDataTasker.addAtAll(type, it, target)
            }?.let { sendMessage(it) }
        }
    }

    @SubCommand("delAtall", "daa", "取消at全体")
    suspend fun CommandSender.delAtall(type: String = "a", user: String = "0", target: GroupOrContact = GroupOrContact(Contact())) {
        if (checkPerm(target)) {
            matchUser(user) {
                BiliDataTasker.delAtAll(type, it, target.subject)
            }?.let { sendMessage(it) }
        }
    }

    @SubCommand("listAtall", "laa", "at全体列表")
    suspend fun CommandSender.listAtall(user: String = "0", target: GroupOrContact = GroupOrContact(Contact())) {
        if (checkPerm(target)) {
            matchUser(user) {
                BiliDataTasker.listAtAll(it, target.subject)
            }?.let { sendMessage(it) }
        }
    }

    @SubCommand("config", "配置")
    suspend fun CommandSenderOnMessage<*>.config(user: String = "0", target: GroupOrContact = GroupOrContact(Contact())) {
        if (checkPerm(target)) {
            if (user == "0") {
                BiliDataTasker.config(fromEvent, 0, target.contact!!)
            } else {
                matchUser(user) {
                    BiliDataTasker.config(fromEvent, it, target.contact!!)
                    null
                }?.let { sendMessage(it) }
            }
        }
    }

    @SubCommand("search", "s", "搜索")
    suspend fun CommandSenderOnMessage<*>.search(did: String) {
        val subject = Contact()
        val detail = biliClient.getDynamicDetail(did)
        if (detail != null) subject.sendMessage("少女祈祷中...") else subject.sendMessage("未找到动态")
        detail?.let { d -> BiliBiliDynamic.dynamicChannel.send(DynamicDetail(d, subject.delegate)) }
    }

    @SubCommand("live", "直播")
    suspend fun CommandSenderOnMessage<*>.live() {
        val subject = Contact()
        val detail = biliClient.getLive(1, 1)
        if (detail != null) subject.sendMessage("少女祈祷中...") else subject.sendMessage("当前没有人在直播")
        detail?.let { d -> BiliBiliDynamic.liveChannel.send(LiveDetail(d.rooms.first(), subject.delegate)) }
    }

    @SubCommand("new", "最新动态")
    suspend fun CommandSenderOnMessage<*>.new(user: String, count: Int = 1) {
        matchUser(user) {
            val list = biliClient.getUserNewDynamic(it)?.items?.subList(0, count)
            list?.forEach { di ->
                BiliBiliDynamic.dynamicChannel.send(DynamicDetail(di, Contact().delegate))
            }
            if (!list.isNullOrEmpty()) "少女祈祷中..." else "未找到动态"
        }?.let { sendMessage(it) }
    }

    @SubCommand("create", "创建分组")
    suspend fun CommandSender.createGroup(name: String) = sendMessage(
        BiliDataTasker.createGroup(name, subject?.id ?:0L)
    )

    @SubCommand("listGroup", "lg", "分组列表")
    suspend fun CommandSender.listGroup(name: String? = null) = sendMessage(
        BiliDataTasker.listGroup(name, subject?.id ?:0L)
    )

    @SubCommand("delGroup", "dg", "删除分组")
    suspend fun CommandSender.delGroup(name: String) = sendMessage(
        BiliDataTasker.delGroup(name, subject?.id ?:0L)
    )

    @SubCommand("addGroupAdmin", "aga", "添加分组管理员")
    suspend fun CommandSender.setGroupAdmin(name: String, contacts: String) = sendMessage(
        BiliDataTasker.setGroupAdmin(name, contacts, subject?.id ?:0L)
    )

    @SubCommand("banGroupAdmin", "bga", "删除分组管理员")
    suspend fun CommandSender.banGroupAdmin(name: String, contacts: String) = sendMessage(
        BiliDataTasker.banGroupAdmin(name, contacts, subject?.id ?:0L)
    )

    @SubCommand("push", "添加分组")
    suspend fun CommandSender.pushGroup(name: String, contacts: String) = sendMessage(
        BiliDataTasker.pushGroupContact(name, contacts, subject?.id ?:0L)
    )

    @SubCommand("ban")
    suspend fun CommandSender.delGroupContact(name: String, contacts: String) = sendMessage(
        BiliDataTasker.delGroupContact(name, contacts, subject?.id ?:0L)
    )


    suspend fun CommandSender.checkPerm(target: GroupOrContact): Boolean {
        if (target.group != null && !BiliDataTasker.checkGroupPerm(target.group.name, Contact().id)) {
            sendMessage("权限不足, 无法操作其他分组")
            return false
        }
        if (target.group == null && !hasPermission(crossContact) && (target.contact?.delegate ?: "0") != Contact().delegate) {
            sendMessage("权限不足, 无法操作其他人")
            return false
        }
        return true
    }

}

fun CommandSender.Contact(): Contact = subject ?: throw CommandArgumentParserException("无法从当前环境获取联系人")