package top.colter.mirai.plugin.bilibili.tasker

import net.mamoe.mirai.event.events.BotLeaveEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.content
import top.colter.mirai.plugin.bilibili.BiliData
import top.colter.mirai.plugin.bilibili.api.getLiveStatus
import top.colter.mirai.plugin.bilibili.service.DynamicService.removeAllSubscribe
import top.colter.mirai.plugin.bilibili.utils.biliClient
import top.colter.mirai.plugin.bilibili.utils.delegate
import top.colter.mirai.plugin.bilibili.utils.findContact
import top.colter.mirai.plugin.bilibili.utils.logger

object ListenerTasker : BiliTasker() {
    override var interval: Int = -1

    override suspend fun main() {
        globalEventChannel().subscribeAlways<BotLeaveEvent> {
            val d = group.delegate
            if (findContact(d) == null) {
                removeAllSubscribe(d)
                BiliData.dynamicPushTemplate.forEach { (_, c) -> c.remove(d) }
                BiliData.livePushTemplate.forEach { (_, c) -> c.remove(d) }
                logger.warning("Bot退出群 ${group.name}(${group.id}) 已删除此群的所有订阅数据")
            }
        }

        globalEventChannel().subscribeAlways<GroupMessageEvent> {
            if (this.message.content == "#test"){
                println(biliClient.getLiveStatus(listOf(4711217, 672328094)))
            }
        }

    }
}