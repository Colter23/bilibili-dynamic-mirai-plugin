package top.colter.mirai.plugin.bilibili.service

import kotlinx.coroutines.*
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.nextMessage
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.exception.InteractTimeoutException
import top.colter.mirai.plugin.bilibili.exception.InteractTryLimitException
import kotlin.coroutines.CoroutineContext

class InteractMessage(
    private val context: MessageEvent,
    private val globalTryLimit: Int = 2,
    private val globalTimeout: Int = 180,
): CoroutineScope, CompletableJob by SupervisorJob(BiliBiliDynamic.coroutineContext.job) {

    override val coroutineContext: CoroutineContext
        get() = this + CoroutineName(this::class.simpleName ?: "InteractMessage")

    private val target = context.subject

    suspend fun send(msg: String) = target.sendMessage(msg)
    suspend fun send(msg: Message) = target.sendMessage(msg)

    suspend fun receiveMessage(
        tryLimit: Int = globalTryLimit,
        timeout: Int = globalTimeout,
        checkBlock: ((MessageChain) -> Boolean) = { true }
    ): MessageChain{
        return receive(tryLimit, timeout, checkBlock)
    }
    suspend fun receivePlain(
        tryLimit: Int = globalTryLimit,
        timeout: Int = globalTimeout,
        checkBlock: ((String) -> Boolean) = { true }
    ): String {
        return receive(tryLimit, timeout) {
            checkBlock(it.content)
        }.content
    }

    suspend fun receive(
        tryLimit: Int = globalTryLimit,
        timeout: Int = globalTimeout,
        checkBlock: ((MessageChain) -> Boolean) = { true }
    ): MessageChain {
        repeat(tryLimit) {
            val msg = try {
                context.nextMessage(timeoutMillis = timeout * 1000L)
            } catch (e: TimeoutCancellationException) {
                throw InteractTimeoutException()
            }
            if (checkBlock(msg)) return msg
        }
        throw InteractTryLimitException()
    }


}

suspend fun MessageEvent.interactMessage(
    globalTryLimit: Int = 2,
    globalTimeout: Int = 180,
    block: suspend InteractMessage.() -> Unit
): String {
    val im = InteractMessage(this@interactMessage, globalTryLimit, globalTimeout)
//    im.block()
    block(im)


    return ""
}

suspend fun GroupMessageEvent.test() {

    interactMessage() {
        send("")
        receive {
            true
        }
    }


}
