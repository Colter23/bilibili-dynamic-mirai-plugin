package top.colter.mirai.plugin.bilibili.tasker

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.content
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic.dynamicChannel
import top.colter.mirai.plugin.bilibili.api.getDynamicDetail
import top.colter.mirai.plugin.bilibili.data.DynamicDetail
import top.colter.mirai.plugin.bilibili.utils.biliClient
import top.colter.mirai.plugin.bilibili.utils.delegate

object ListenerTasker : BiliTasker() {
    override val interval: Int = -1

    override suspend fun main() {
        globalEventChannel().subscribeAlways<MessageEvent> {
            println(it.message.content)
            val regex = """^#bili s (.+)""".toRegex()
            val s = regex.find(it.message.content)
            if (s != null) {
                val detail = biliClient.getDynamicDetail(s.destructured.component1())
                if (detail != null) it.subject.sendMessage("绘图中，请稍等")
                detail?.let { d -> dynamicChannel.send(DynamicDetail(d, it.subject.delegate)) }
            }
        }
    }
}