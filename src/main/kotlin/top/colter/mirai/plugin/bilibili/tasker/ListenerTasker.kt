package top.colter.mirai.plugin.bilibili.tasker

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel

object ListenerTasker : BiliTasker() {
    override var interval: Int = -1

    override suspend fun main() {
        globalEventChannel().subscribeAlways<MessageEvent> {

        }
    }
}