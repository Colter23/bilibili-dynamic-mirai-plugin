package top.colter.mirai.plugin.bilibili.command

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CommandSenderOnMessage
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.descriptor.CommandArgumentParserException
import net.mamoe.mirai.contact.Contact
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.old.BiliPluginConfig
import top.colter.mirai.plugin.bilibili.tasker.BiliDataTasker
import top.colter.mirai.plugin.bilibili.utils.delegate

object DynamicCommand : CompositeCommand(
    owner = BiliBiliDynamic,
    "bili",
    description = "动态指令"
) {

    @SubCommand("set", "设置")
    suspend fun CommandSenderOnMessage<*>.set(uid: Long, contact: Contact = Contact()) {
        //MyEvent(uid, contact.delegate, fromEvent).broadcast()
    }

    @SubCommand("color", "颜色")
    suspend fun CommandSender.color(uid: Long, color: String) = sendMessage(
        BiliDataTasker.setColor(uid, color)
    )

    @SubCommand("add", "添加")
    suspend fun CommandSender.add(uid: Long, contact: Contact = Contact()) =
        BiliDataTasker.addSubscribe(uid, contact)



    @SubCommand("del", "删除")
    suspend fun CommandSender.del(uid: Long, contact: Contact = Contact()) = sendMessage(
        BiliDataTasker.removeSubscribe(uid, contact.delegate).let { "对 ${it?.name} 取消订阅成功!" }
    )

    @SubCommand("delAll", "删除全部订阅")
    suspend fun CommandSender.delAll(contact: Contact = Contact()) = sendMessage(
        BiliDataTasker.removeAllSubscribe(contact.delegate).let { "删除订阅成功! 共删除 $it 个订阅" }
    )

    @SubCommand("list", "列表")
    suspend fun CommandSender.list(contact: Contact = Contact()) = sendMessage(
        BiliDataTasker.list(contact.delegate)
    )

    @SubCommand("listAll", "la" , "全部订阅列表")
    suspend fun CommandSender.listAll() = sendMessage(
        if (subject?.isAdmin() == true){
            BiliDataTasker.listAll()
        }else{
            "权限不足"
        }
    )

    @SubCommand("listUser", "lu" , "用户列表")
    suspend fun CommandSender.listUser() = sendMessage(
        if (subject?.isAdmin() == true){
            BiliDataTasker.listUser()
        }else{
            "权限不足"
        }
    )

    @SubCommand("filter", "f", "过滤")
    suspend fun CommandSender.filter(regex: String, uid: Long, contact: Contact = Contact()){
        //BiliDataTasker.addFilter(regex, uid, contact)
    }



    @SubCommand("contain", "c", "包含")
    suspend fun CommandSender.contain(regex: String, uid: Long, contact: Contact = Contact()) {
        //BiliDataTasker.addFilter(regex, uid, contact.delegate, false)
    }

    @SubCommand("filterList", "fl", "过滤列表")
    suspend fun CommandSender.filterList(uid: Long, contact: Contact = Contact()) {
        //BiliDataTasker.listFilter(uid, contact.delegate)
    }

    @SubCommand("filterDel", "fd", "过滤删除")
    suspend fun CommandSender.filterDel(index: String, uid: Long, contact: Contact = Contact()) = sendMessage(
        BiliDataTasker.delFilter(uid, contact.delegate, index)
    )

    @SubCommand("login", "登录")
    suspend fun CommandSender.login() {
        if (subject?.isAdmin() == true){
            BiliDataTasker.login(Contact())
        }else{
            sendMessage("仅Bot管理员可进行登录")
        }
    }

    @SubCommand("config", "配置")
    suspend fun CommandSenderOnMessage<*>.config(uid: Long = 0L) {
        BiliDataTasker.config(fromEvent, uid)
    }

}

fun Contact.isAdmin(): Boolean = BiliPluginConfig.admin == id.toString()

fun CommandSender.Contact(): Contact = subject ?: throw CommandArgumentParserException("无法从当前环境获取联系人")