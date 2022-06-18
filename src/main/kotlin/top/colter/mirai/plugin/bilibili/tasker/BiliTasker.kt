package top.colter.mirai.plugin.bilibili.tasker

import kotlinx.coroutines.*
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import kotlin.coroutines.CoroutineContext

abstract class BiliTasker(
    private val taskerName: String? = null
) : CoroutineScope, CompletableJob by SupervisorJob(BiliBiliDynamic.coroutineContext.job) {
    override val coroutineContext: CoroutineContext
        get() = this + CoroutineName(taskerName ?: this::class.simpleName ?: "Tasker")

    companion object {
        val taskers = mutableListOf<BiliTasker>()

        fun cancelAll() {
            taskers.forEach {
                it.cancel()
            }
        }
    }

    private var job: Job? = null

    abstract var interval: Int

    protected open fun init(){}

    protected abstract suspend fun main()

    override fun start(): Boolean {
        job = launch(coroutineContext) {
            init()
            if (interval == -1) {
                main()
            } else {
                while (isActive) {
                    try {
                        main()
                    } catch (e: Throwable) {
                        BiliBiliDynamic.logger.error(e)
                        delay(interval * 1000L)
                    }
                    delay(interval * 1000L)
                }
            }
        }

        return taskers.add(this)
    }

    override fun cancel(cause: CancellationException?) {
        job?.cancel(cause)
        coroutineContext.cancelChildren(cause)
    }
}