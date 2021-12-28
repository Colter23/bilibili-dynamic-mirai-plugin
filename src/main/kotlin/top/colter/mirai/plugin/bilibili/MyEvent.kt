package top.colter.mirai.plugin.bilibili

import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.events.MessageEvent

class MyEvent(
    val uid: Long,
    val subject: String,
    val message: MessageEvent,
) : AbstractEvent()