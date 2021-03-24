package top.colter.mirai.plugin

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.User

object MySimpleCommand : SimpleCommand(
    PluginMain, "tell", "私聊","sss",
    description = "Tell somebody privately"
//    usage = "/tell <target> <message>"  // usage 如不设置则自动根据带有 @Handler 的方法生成
) {
    @Handler // 标记这是指令处理器  // 函数名随意
    suspend fun CommandSender.handle(target: User, message: String) { // 这两个参数会被作为指令参数要求
        target.sendMessage(message)
    }
}


//object MyCompositeCommand : CompositeCommand(
//    PluginMain, "manage", // "manage" 是主指令名
//    description = "示例指令", permission = MyCustomPermission,
//    // prefixOptional = true // 还有更多参数可填, 此处忽略
//) {
//
//    // [参数智能解析]
//    //
//    // 在控制台执行 "/manage <群号>.<群员> <持续时间>",
//    // 或在聊天群内发送 "/manage <@一个群员> <持续时间>",
//    // 或在聊天群内发送 "/manage <目标群员的群名> <持续时间>",
//    // 或在聊天群内发送 "/manage <目标群员的账号> <持续时间>"
//    // 时调用这个函数
//    @SubCommand // 表示这是一个子指令，使用函数名作为子指令名称
//    suspend fun CommandSender.mute(target: Member, duration: Int) { // 通过 /manage mute <target> <duration> 调用
//        sendMessage("/manage mute 被调用了, 参数为: $target, $duration")
//
//        val result = kotlin.runCatching {
//            target.mute(duration).toString()
//        }.getOrElse {
//            it.stackTraceToString()
//        } // 失败时返回堆栈信息
//
//        sendMessage("结果: $result")
//    }
//
//    @SubCommand
//    suspend fun ConsoleCommandSender.foo() {
//        // 使用 ConsoleCommandSender 作为接收者，表示指令只能由控制台执行。
//        // 当用户尝试在聊天环境执行时将会收到错误提示。
//    }
//
//    @SubCommand("list", "查看列表") // 可以设置多个子指令名。此时函数名会被忽略。
//    suspend fun CommandSender.ignoredFunctionName() { // 执行 "/manage list" 时调用这个函数
//        sendMessage("/manage list 被调用了")
//    }
//
//    // 支持 Image 类型, 需在聊天中执行此指令.
//    @SubCommand
//    suspend fun UserCommandSender.test(image: Image) { // 执行 "/manage test <一张图片>" 时调用这个函数
//        // 由于 Image 类型消息只可能在聊天环境，可以直接使用 UserCommandSender。
//
//        sendMessage("/manage image 被调用了, 图片是 ${image.imageId}")
//    }
//}