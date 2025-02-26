package top.colter.mirai.plugin.bilibili.task

import com.cronutils.model.Cron
import com.cronutils.model.time.ExecutionTime
import kotlinx.coroutines.*
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.tools.logger
import java.time.ZonedDateTime
import kotlin.coroutines.CoroutineContext

interface BiliTasker {
    companion object {
        val taskers: List<BiliTasker> by lazy {
            BiliTasker::class.sealedSubclasses.flatMap { it.sealedSubclasses }.mapNotNull { it.objectInstance }
        }
        fun startAll() {
            taskers.forEach { it.start() }
        }
        fun cancelAll() {
            taskers.forEach { it.cancel() }
        }
    }

    fun init() {}

    fun before() {}
    suspend fun main()
    fun after() {}

//    fun start(delay: Int): Boolean
    fun start()
    fun cancel()

}

abstract class AbstractIntervalTasker {
    abstract fun main()
    fun start(){}
}

class IntervalTasker: AbstractIntervalTasker() {
    var task: () -> Unit = {}

    override fun main() {
        task()
    }

    fun start(delay: Int, interval: Int, task: () -> Unit) {
        this.task = task
        start()
    }
}

sealed class BiliIntervalTasker(
    private val taskerName: String? = null
) : BiliTasker, CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = CoroutineName(taskerName ?: this::class.simpleName ?: "Tasker") +
                SupervisorJob(BiliBiliDynamic.coroutineContext.job)

    private var job: Job? = null

    abstract var interval: Int
    open val unitTime: Long = 1000

    override fun start() {
        job = launch(coroutineContext) {
            try {
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
            }catch (t: Throwable) {
                logger.error("${this::class.simpleName} 初始化失败! $t")
            }
        }
    }

    override fun cancel() {
        job?.cancel()
        coroutineContext.cancelChildren()
    }
}

sealed class BiliCronTasker(
    private val taskerName: String? = null
) : BiliTasker, CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = CoroutineName(taskerName ?: this::class.simpleName ?: "Tasker") +
                SupervisorJob(BiliBiliDynamic.coroutineContext.job)

    private var job: Job? = null

    abstract var cron: Cron

    override fun start() {
        job = launch(coroutineContext) {
            try {
                init()
                while (isActive) {
                    try {
                        before()
                        main()
                        after()
                    } catch (t: Throwable) {
                        logger.error(this::class.simpleName + t)
                        delay(120000L)
                    }
                    val interval = ExecutionTime.forCron(cron).timeToNextExecution(ZonedDateTime.now()).orElseThrow().toMillis()
                    delay(interval)
                }
                if (!isActive) logger.error("${this::class.simpleName} 已停止工作!")
            }catch (t: Throwable) {
                logger.error("${this::class.simpleName} 初始化失败! $t")
            }
        }
    }

    override fun cancel() {
        job?.cancel()
        coroutineContext.cancelChildren()
    }
}


sealed class BiliOneTasker(
    private val taskerName: String? = null

) : BiliTasker, CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = CoroutineName(taskerName ?: this::class.simpleName ?: "Tasker") +
                SupervisorJob(BiliBiliDynamic.coroutineContext.job)

    private var job: Job? = null

    override fun start() {
        job = launch(coroutineContext) {
            try {
                init()
                before()
                main()
                after()
                if (!isActive) logger.error("${this::class.simpleName} 已停止工作!")
            }catch (t: Throwable) {
                logger.error("${this::class.simpleName} 初始化失败! $t")
            }
        }
    }

    override fun cancel() {
        job?.cancel()
        coroutineContext.cancelChildren()
    }
}
