package top.colter.mirai.plugin.bilibili.tasker

import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.api.getLiveStatus
import top.colter.mirai.plugin.bilibili.data.LIVE_LINK
import top.colter.mirai.plugin.bilibili.data.LiveCloseMessage
import top.colter.mirai.plugin.bilibili.utils.formatTime
import java.time.Duration
import java.time.Instant


object LiveCloseCheckTasker : BiliCheckTasker("LiveClose")  {

    override var interval: Int = BiliConfig.checkConfig.liveInterval

    override var lowSpeedEnable = false
    override var checkReportEnable = false

    private val liveUsers by BiliBiliDynamic::liveUsers
    private var nowTime = Instant.now().epochSecond

    override suspend fun main() {
        if (liveUsers.isNotEmpty()) {
            nowTime = Instant.now().epochSecond

            val liveStatusMap = client.getLiveStatus(liveUsers)
            val liveStatusList = liveStatusMap?.map { it.value }?.filter { it.liveStatus != 1 }

            liveStatusList?.forEach { info ->
                BiliBiliDynamic.messageChannel.send(LiveCloseMessage(
                    info.roomId,
                    info.uid,
                    info.uname,
                    info.liveTime.formatTime,
                    0,
                    nowTime.formatTime,
                    formatDuration(nowTime - info.liveTime),
                    info.title,
                    info.area,
                    LIVE_LINK(info.roomId)
                ))
                liveUsers.remove(info.uid)
            }
        }
    }

    private fun formatDuration(time: Long): String {
        val duration = Duration.ofSeconds(time)
        val day = duration.toDays()
        val hour = duration.minusDays(day).toHours()
        val minute = duration.minusDays(day).minusHours(hour).toMinutes()
        val second = duration.minusDays(day).minusHours(hour).minusMinutes(minute).toSeconds()
        return buildString {
            if (day > 0) append("${day}天 ")
            if (hour > 0) append("${hour}小时 ")
            if (minute > 0) append("${minute}分钟 ")
            if (second > 0) append("${second}秒")
        }
    }

}