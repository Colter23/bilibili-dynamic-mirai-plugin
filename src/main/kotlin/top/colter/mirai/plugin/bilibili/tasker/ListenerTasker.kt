package top.colter.mirai.plugin.bilibili.tasker

import net.mamoe.mirai.event.events.BotLeaveEvent
import net.mamoe.mirai.event.globalEventChannel
import top.colter.mirai.plugin.bilibili.BiliData
import top.colter.mirai.plugin.bilibili.utils.delegate
import top.colter.mirai.plugin.bilibili.utils.findContact

object ListenerTasker : BiliTasker() {
    override var interval: Int = -1

    override suspend fun main() {
        globalEventChannel().subscribeAlways<BotLeaveEvent> {
            val d = group.delegate
            if (findContact(d) == null) {
                BiliDataTasker.removeAllSubscribe(d)
                BiliData.dynamicPushTemplate.forEach { (_, c) -> c.remove(d) }
                BiliData.livePushTemplate.forEach { (_, c) -> c.remove(d) }
                logger.warning("Bot退出群 ${group.name}(${group.id}) 已删除此群的所有订阅数据")
            }
        }
    }
}