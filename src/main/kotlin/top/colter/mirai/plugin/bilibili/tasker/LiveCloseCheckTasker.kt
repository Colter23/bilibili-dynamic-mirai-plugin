package top.colter.mirai.plugin.bilibili.tasker

import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.api.getLiveStatus
import top.colter.mirai.plugin.bilibili.data.LIVE_LINK
import top.colter.mirai.plugin.bilibili.data.LiveCloseMessage
import top.colter.mirai.plugin.bilibili.utils.formatDuration
import top.colter.mirai.plugin.bilibili.utils.formatTime
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

            val liveStatusMap = client.getLiveStatus(liveUsers.map { it.key })
            val liveStatusList = liveStatusMap?.map { it.value }?.filter { it.liveStatus != 1 }

            liveStatusList?.forEach { info ->
                val liveTime = liveUsers[info.uid]!!
                BiliBiliDynamic.messageChannel.send(LiveCloseMessage(
                    info.roomId,
                    info.uid,
                    info.uname,
                    liveTime.formatTime,
                    0,
                    nowTime.formatTime,
                    (nowTime - liveTime).formatDuration(),
                    info.title,
                    info.area,
                    LIVE_LINK(info.roomId.toString())
                ))
                liveUsers.remove(info.uid)
            }
        }
    }

}