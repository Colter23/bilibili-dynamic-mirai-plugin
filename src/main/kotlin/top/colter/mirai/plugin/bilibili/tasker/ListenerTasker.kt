package top.colter.mirai.plugin.bilibili.tasker

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.BotLeaveEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.*
import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.BiliData
import top.colter.mirai.plugin.bilibili.service.DynamicService.removeAllSubscribe
import top.colter.mirai.plugin.bilibili.service.TriggerMode
import top.colter.mirai.plugin.bilibili.service.matchingRegular
import top.colter.mirai.plugin.bilibili.utils.*

object ListenerTasker : BiliTasker() {
    override var interval: Int = -1

    private val triggerMode = BiliConfig.linkResolveConfig.triggerMode
    private val returnLink = BiliConfig.linkResolveConfig.returnLink

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
            var f = false
            when (triggerMode) {
                TriggerMode.At -> {
                    val at = message.filterIsInstance(At::class.java)
                    if (at.isNotEmpty() && at.any { Bot.instances.map { it.id }.contains(it.target) }) {
                        f = true
                    }
                }
                TriggerMode.Always -> f = true
                TriggerMode.Never -> f = false
            }
            if (f) {
                val msg = message.filter { it !is At && it !is Image }.toMessageChain().content.trim()
                val type = matchingRegular(msg)
                if (type != null) {
                    val ms = subject.sendMessage("加载中...")
                    val img = type.drawGeneral()
                    if (img == null) {
                        ms.recall()
                        subject.sendMessage("解析失败")
                        return@subscribeAlways
                    }
                    val imgMsg = subject.uploadImage(img, CacheType.DRAW_SEARCH)
                    if (imgMsg == null) {
                        ms.recall()
                        subject.sendMessage("图片上传失败")
                        return@subscribeAlways
                    }
                    subject.sendMessage(buildMessageChain {
                        + imgMsg
                        if (returnLink) + PlainText(type.getLink())
                    })
                    ms.recall()
                }
            }
        }
    }
}