package top.colter.mirai.plugin.bilibili.lisener

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.MessageEvent
import top.colter.mirai.plugin.bilibili.tools.logger
import kotlin.coroutines.CoroutineContext

object MessageListener: SimpleListenerHost() {
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        logger.error("MessageEventListener Exception: $exception")
    }

    @EventHandler
    suspend fun MessageEvent.onMessage() {

    }

}