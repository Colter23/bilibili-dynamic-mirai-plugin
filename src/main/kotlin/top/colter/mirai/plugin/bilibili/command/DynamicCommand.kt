package top.colter.mirai.plugin.bilibili.command

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CommandSenderOnMessage
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.descriptor.CommandArgumentParserException
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.broadcast
import top.colter.mirai.plugin.bilibili.DynamicTasker
import top.colter.mirai.plugin.bilibili.MyEvent
import top.colter.mirai.plugin.bilibili.PluginMain
import top.colter.mirai.plugin.bilibili.data.BiliPluginConfig
import top.colter.mirai.plugin.bilibili.delegate

object DynamicCommand : CompositeCommand(
    owner = PluginMain,
    "bili",
    description = "动态指令"
) {

//    @SubCommand("listenAll", "监听账号")
//    suspend fun CommandSender.listenAll(contact: Contact = Contact()) = sendMessage(
//        if (DynamicTasker.listenAll(contact.delegate) == true){
//            "监听账号成功!"
//        }else{
//            "监听账号失败!"
//        }
//    )
//
//    @SubCommand("cancelListen", "取消监听账号")
//    suspend fun CommandSender.cancelListen(contact: Contact = Contact()) = sendMessage(
//        DynamicTasker.cancelListen(contact.delegate).let { "取消监听账号成功!" }
//    )

    @SubCommand("set", "设置")
    suspend fun CommandSenderOnMessage<*>.set(uid: Long, contact: Contact = Contact()) {
        MyEvent(uid, contact.delegate, fromEvent).broadcast()
    }

    @SubCommand("color", "颜色")
    suspend fun CommandSender.color(uid: Long, color: String) = sendMessage(
        DynamicTasker.setColor(uid, color)
    )

    @SubCommand("add", "添加")
    suspend fun CommandSender.add(uid: Long, contact: Contact = Contact()) = sendMessage(
        DynamicTasker.addSubscribe(uid, contact.delegate)
    )


    @SubCommand("del", "删除")
    suspend fun CommandSender.del(uid: Long, contact: Contact = Contact()) = sendMessage(
        DynamicTasker.removeSubscribe(uid, contact.delegate).let { "对 ${it?.name} 取消订阅成功!" }
    )

    @SubCommand("delAll", "删除全部订阅")
    suspend fun CommandSender.delAll(contact: Contact = Contact()) = sendMessage(
        DynamicTasker.removeAllSubscribe(contact.delegate).let { "删除订阅成功! 共删除 $it 个订阅" }
    )

    @SubCommand("list", "列表")
    suspend fun CommandSender.list(contact: Contact = Contact()) = sendMessage(
        DynamicTasker.list(contact.delegate)
    )

    @SubCommand("filter", "f", "过滤")
    suspend fun CommandSender.filter(regex: String, uid: Long, contact: Contact = Contact()) = sendMessage(
        DynamicTasker.addFilter(regex, uid, contact.delegate)
    )

    @SubCommand("contain", "c", "包含")
    suspend fun CommandSender.contain(regex: String, uid: Long, contact: Contact = Contact()) = sendMessage(
        DynamicTasker.addFilter(regex, uid, contact.delegate, false)
    )

    @SubCommand("filterList", "fl", "过滤列表")
    suspend fun CommandSender.filterList(uid: Long, contact: Contact = Contact()) = sendMessage(
        DynamicTasker.listFilter(uid, contact.delegate)
    )

    @SubCommand("filterDel", "fd", "过滤删除")
    suspend fun CommandSender.filterDel(index: String, uid: Long, contact: Contact = Contact()) = sendMessage(
        DynamicTasker.delFilter(uid, contact.delegate, index)
    )

    @SubCommand("login", "登录")
    suspend fun CommandSender.login() {
        if (BiliPluginConfig.admin == subject?.id.toString()){
            DynamicTasker.login(Contact())
        }else{
            sendMessage("仅Bot管理员可进行登录")
        }
    }

}

fun CommandSender.Contact(): Contact = subject ?: throw CommandArgumentParserException("无法从当前环境获取联系人")
