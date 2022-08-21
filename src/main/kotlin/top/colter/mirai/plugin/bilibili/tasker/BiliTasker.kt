package top.colter.mirai.plugin.bilibili.tasker

import kotlinx.coroutines.*
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.utils.logger
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
    open val unitTime: Long = 1000

    protected open fun init() {}

    protected open fun before() {}
    protected abstract suspend fun main()
    protected open fun after() {}

    override fun start(): Boolean {
        job = launch(coroutineContext) {
            init()
            if (interval == -1) {
                before()
                main()
                after()
            } else {
                while (isActive) {
                    try {
                        before()
                        main()
                        after()
                    } catch (t: Throwable) {
                        logger.error(this::class.simpleName + t)
                        delay(120000L)
                    }
                    delay(interval * unitTime)
                }
            }
            if (!isActive) logger.error("${this::class.simpleName} 已停止工作!")
        }

        return taskers.add(this)
    }

    override fun cancel(cause: CancellationException?) {
        job?.cancel(cause)
        coroutineContext.cancelChildren(cause)
    }
}