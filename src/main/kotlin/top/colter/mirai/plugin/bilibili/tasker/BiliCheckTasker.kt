package top.colter.mirai.plugin.bilibili.tasker

import top.colter.mirai.plugin.bilibili.BiliConfig
import java.time.LocalTime

abstract class BiliCheckTasker(
    taskerName: String? = null
): BiliTasker(taskerName) {

    private val intervalTime: Int by lazy { interval }

    private val lowSpeedEnable by BiliConfig.enableConfig::lowSpeedEnable

    private var lsl = listOf(0, 0)
    private var isLowSpeed = false

    override fun init() {
        runCatching {
            lsl = BiliConfig.checkConfig.lowSpeed.split("-", "x").map { it.toInt() }
            isLowSpeed = lsl[0] != lsl[1]
        }.onFailure {
            logger.error("低频检测参数错误 ${it.message}")
        }
    }

    override fun after() {
        interval = calcTime(intervalTime)
    }

    private fun calcTime(time: Int): Int {
        return if (lowSpeedEnable && isLowSpeed) {
            val hour = LocalTime.now().hour
            return if (lsl[0] > lsl[1]) {
                if (lsl[0] <= hour || hour <= lsl[1]) time * lsl[2] else time
            } else {
                if (lsl[0] <= hour && hour <= lsl[1]) time * lsl[2] else time
            }
        } else time
    }

}