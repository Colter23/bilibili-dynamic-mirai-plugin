package top.colter.mirai.plugin.bilibili.command

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CommandSenderOnMessage
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.descriptor.CommandArgumentParserException
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.contact.Contact
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic.crossContact
import top.colter.mirai.plugin.bilibili.FilterMode
import top.colter.mirai.plugin.bilibili.FilterType
import top.colter.mirai.plugin.bilibili.api.getDynamicDetail
import top.colter.mirai.plugin.bilibili.api.getLive
import top.colter.mirai.plugin.bilibili.data.DynamicDetail
import top.colter.mirai.plugin.bilibili.data.LiveDetail
import top.colter.mirai.plugin.bilibili.tasker.BiliDataTasker
import top.colter.mirai.plugin.bilibili.utils.biliClient
import top.colter.mirai.plugin.bilibili.utils.delegate

object DynamicCommand : CompositeCommand(
    owner = BiliBiliDynamic,
    "bili",
    description = "动态指令"
) {

    @SubCommand("color", "颜色")
    suspend fun CommandSender.color(uid: Long, color: String) = sendMessage(
        BiliDataTasker.setColor(uid, color)
    )

    @SubCommand("add", "添加")
    suspend fun CommandSender.add(uid: Long, contact: Contact = Contact()) {
        if(!hasPermission(crossContact) && contact.delegate != Contact().delegate) return
        sendMessage(BiliDataTasker.addSubscribe(uid, contact.delegate))
    }


    @SubCommand("del", "删除")
    suspend fun CommandSender.del(uid: Long, contact: Contact = Contact()) {
        if(!hasPermission(crossContact) && contact.delegate != Contact().delegate) return
        sendMessage(BiliDataTasker.removeSubscribe(uid, contact.delegate).let { "对 ${it?.name} 取消订阅成功!" })
    }

    @SubCommand("delAll", "删除全部订阅")
    suspend fun CommandSender.delAll(contact: Contact = Contact()) {
        if(!hasPermission(crossContact) && contact.delegate != Contact().delegate) return
        sendMessage(BiliDataTasker.removeAllSubscribe(contact.delegate).let { "删除订阅成功! 共删除 $it 个订阅" })
    }

    @SubCommand("list", "列表")
    suspend fun CommandSender.list(contact: Contact = Contact()) {
        if(!hasPermission(crossContact) && contact.delegate != Contact().delegate) return
        sendMessage(BiliDataTasker.list(contact.delegate))
    }

    @SubCommand("listAll", "la", "全部订阅列表")
    suspend fun CommandSender.listAll() = sendMessage(
        BiliDataTasker.listAll()
    )

    @SubCommand("listUser", "lu", "用户列表")
    suspend fun CommandSender.listUser() = sendMessage(
        BiliDataTasker.listUser()
    )

    @SubCommand("filterMode", "fm", "过滤模式")
    suspend fun CommandSender.filterMode(type: String, mode: String, uid: Long = 0L, contact: Contact = Contact()) {
        if(!hasPermission(crossContact) && contact.delegate != Contact().delegate) return
        sendMessage(
            BiliDataTasker.addFilter(
                if (type == "t") FilterType.TYPE else FilterType.REGULAR,
                if (mode == "w") FilterMode.WHITE_LIST else FilterMode.BLACK_LIST,
                type, uid, contact.delegate
            )
        )
    }


    @SubCommand("filterType", "ft", "类型过滤")
    suspend fun CommandSender.filterType(type: String, uid: Long = 0L, contact: Contact = Contact()) {
        if(!hasPermission(crossContact) && contact.delegate != Contact().delegate) return
        sendMessage(BiliDataTasker.addFilter(FilterType.TYPE, null, type, uid, contact.delegate))
    }

    @SubCommand("filterReg", "fr", "正则过滤")
    suspend fun CommandSender.filterReg(reg: String, uid: Long = 0L, contact: Contact = Contact()) {
        if(!hasPermission(crossContact) && contact.delegate != Contact().delegate) return
        sendMessage(BiliDataTasker.addFilter(FilterType.REGULAR, null, reg, uid, contact.delegate))
    }

    @SubCommand("filterList", "fl", "过滤列表")
    suspend fun CommandSender.filterList(uid: Long = 0L, contact: Contact = Contact()) {
        if(!hasPermission(crossContact) && contact.delegate != Contact().delegate) return
        sendMessage(BiliDataTasker.listFilter(uid, contact.delegate))
    }

    @SubCommand("filterDel", "fd", "过滤删除")
    suspend fun CommandSender.filterDel(index: String, uid: Long = 0L, contact: Contact = Contact()) {
        if(!hasPermission(crossContact) && contact.delegate != Contact().delegate) return
        sendMessage(BiliDataTasker.delFilter(index, uid, contact.delegate))
    }

    @SubCommand("templateList", "tl", "模板列表")
    suspend fun CommandSenderOnMessage<*>.templateList() {
        BiliDataTasker.listTemplate("d", Contact())
        BiliDataTasker.listTemplate("l", Contact())
    }

    @SubCommand("template", "t", "模板")
    suspend fun CommandSender.template(type: String, template: String, contact: Contact = Contact()) {
        if(!hasPermission(crossContact) && contact.delegate != Contact().delegate) return
        sendMessage(BiliDataTasker.setTemplate(type, template, contact))
    }

    @SubCommand("login", "登录")
    suspend fun CommandSender.login() {
        BiliDataTasker.login(Contact())
    }

    @SubCommand("config", "配置")
    suspend fun CommandSenderOnMessage<*>.config(uid: Long = 0L, contact: Contact = Contact()) {
        if(!hasPermission(crossContact) && contact.delegate != Contact().delegate) return
        BiliDataTasker.config(fromEvent, uid)
    }

    @SubCommand("search", "s", "搜索")
    suspend fun CommandSenderOnMessage<*>.search(did: String) {
        val subject = Contact()
        val detail = biliClient.getDynamicDetail(did)
        if (detail != null) subject.sendMessage("请稍等") else subject.sendMessage("未找到动态")
        detail?.let { d -> BiliBiliDynamic.dynamicChannel.send(DynamicDetail(d, subject.delegate)) }
    }

    @SubCommand("live", "直播")
    suspend fun CommandSenderOnMessage<*>.live() {
        val subject = Contact()
        val detail = biliClient.getLive(1, 1)
        if (detail != null) subject.sendMessage("请稍等") else subject.sendMessage("当前没有人在直播")
        detail?.let { d -> BiliBiliDynamic.liveChannel.send(LiveDetail(d.rooms.first(), subject.delegate)) }
    }

}

fun CommandSender.Contact(): Contact = subject ?: throw CommandArgumentParserException("无法从当前环境获取联系人")