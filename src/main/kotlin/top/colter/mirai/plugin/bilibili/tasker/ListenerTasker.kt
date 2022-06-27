package top.colter.mirai.plugin.bilibili.tasker

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel

object ListenerTasker : BiliTasker() {
    override var interval: Int = -1

    override suspend fun main() {
        globalEventChannel().subscribeAlways<MessageEvent> {
            //val regex = """^#bili s (.+)""".toRegex()
            //val dy = regex.find(it.message.content)
            //if (dy != null) {
            //    val detail = biliClient.getDynamicDetail(dy.destructured.component1())
            //    if (detail != null) it.subject.sendMessage("绘图中，请稍等")
            //    detail?.let { d -> dynamicChannel.send(DynamicDetail(d, it.subject.delegate)) }
            //}
            //
            //val liveRegex = """^#bili live""".toRegex()
            //val l = liveRegex.find(it.message.content)
            //if (l != null) {
            //    val detail = biliClient.getLive(1, 1)
            //    if (detail != null) it.subject.sendMessage("绘图中，请稍等")
            //    detail?.let { d -> BiliBiliDynamic.liveChannel.send(LiveDetail(d.rooms.first(), it.subject.delegate)) }
            //}
        }
    }
}