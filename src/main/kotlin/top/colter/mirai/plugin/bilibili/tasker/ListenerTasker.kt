package top.colter.mirai.plugin.bilibili.tasker

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.BotLeaveEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.data.toMessageChain
import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.BiliData
import top.colter.mirai.plugin.bilibili.service.DynamicService.removeAllSubscribe
import top.colter.mirai.plugin.bilibili.service.TriggerMode
import top.colter.mirai.plugin.bilibili.service.matchingRegular
import top.colter.mirai.plugin.bilibili.utils.delegate
import top.colter.mirai.plugin.bilibili.utils.findContact
import top.colter.mirai.plugin.bilibili.utils.logger
import top.colter.mirai.plugin.bilibili.utils.sendImage

object ListenerTasker : BiliTasker() {
    override var interval: Int = -1

    private val triggerMode = BiliConfig.linkResolveConfig.triggerMode

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
            when (triggerMode) {
                TriggerMode.At -> {
                    val at = message.filterIsInstance(At::class.java)
                    if (at.isNotEmpty() && at.any { Bot.instances.map { it.id }.contains(it.target) }) {
                        val msg = message.filter { it !is At && it !is Image }.toMessageChain().content.trim()
                        matchingRegular(msg)?.drawGeneral()?.let { it1 -> subject.sendImage(it1) }
                    }
                }
                TriggerMode.Always -> {
                    val msg = message.filter { it !is At && it !is Image }.toMessageChain().content.trim()
                    matchingRegular(msg)?.drawGeneral()?.let { it1 -> subject.sendImage(it1) }
                }
                TriggerMode.Never -> {}
            }
        }
    }
}