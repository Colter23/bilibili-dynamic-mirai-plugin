package top.colter.mirai.plugin.bilibili.tasker

import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.data.DynamicMessage

object SendTasker: BiliTasker() {

    override val interval: Int = 0

    override suspend fun main() {
        val dynamicMessage = BiliBiliDynamic.messageChannel.receive()
        dynamicMessage.sendMessage(dynamicMessage.buildMessage())
    }


    fun DynamicMessage.sendMessage(message: Message){

    }

    fun DynamicMessage.buildMessage(): Message {
        return buildMessageChain {
            +PlainText("a")
        }
    }

}