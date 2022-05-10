package top.colter.mirai.plugin.bilibili.tasker

import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.data.DynamicItem
import top.colter.mirai.plugin.bilibili.data.DynamicMessage

object MessageTasker: BiliTasker() {

    override val interval: Int = 0

    override suspend fun main() {
        val dynamicItem = BiliBiliDynamic.dynamicChannel.receive()
        BiliBiliDynamic.messageChannel.send(dynamicItem.buildMessage())
    }

    suspend fun DynamicItem.buildMessage(): DynamicMessage {
        return DynamicMessage(
            idStr,
            "",
            null,
            null,
            null
        )
    }

}