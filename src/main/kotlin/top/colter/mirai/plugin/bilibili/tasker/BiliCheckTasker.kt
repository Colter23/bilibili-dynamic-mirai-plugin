package top.colter.mirai.plugin.bilibili.tasker

import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.utils.logger
import java.time.Instant
import java.time.LocalTime

abstract class BiliCheckTasker(
    val taskerName: String? = null
) : BiliTasker(taskerName) {

    private val intervalTime: Int by lazy { interval }

    protected open var lowSpeedEnable = BiliConfig.enableConfig.lowSpeedEnable
    private var lsl = listOf(0, 0)

    protected open var checkReportEnable = true
    private val checkReportInterval: Int = BiliConfig.checkConfig.checkReportInterval
    private var lastCheck: Long = Instant.now().epochSecond - checkReportInterval * 60
    private var checkCount = 0

    companion object {
        @JvmStatic
        protected val client = BiliClient()
    }

    override fun init() {
        if (lowSpeedEnable) runCatching {
            lsl = BiliConfig.checkConfig.lowSpeed.split("-", "x").map { it.toInt() }
            lowSpeedEnable = lsl[0] != lsl[1]
        }.onFailure {
            logger.error("低频检测参数错误 ${it.message}")
        }
    }

    override fun before() {
        if (checkReportEnable) {
            ++ checkCount
            val now = Instant.now().epochSecond
            if (now - lastCheck >= checkReportInterval * 60){
                logger.debug("$taskerName check running...${checkCount}")
                lastCheck = now
                checkCount = 0
            }
        }
    }

    override fun after() {
        if (lowSpeedEnable) interval = calcTime(intervalTime)
    }

    private fun calcTime(time: Int): Int {
        return if (lowSpeedEnable) {
            val hour = LocalTime.now().hour
            return if (lsl[0] > lsl[1]) {
                if (lsl[0] <= hour || hour <= lsl[1]) time * lsl[2] else time
            } else {
                if (lsl[0] <= hour && hour <= lsl[1]) time * lsl[2] else time
            }
        } else time
    }

}