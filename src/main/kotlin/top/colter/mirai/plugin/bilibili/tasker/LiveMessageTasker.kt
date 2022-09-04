package top.colter.mirai.plugin.bilibili.tasker

import kotlinx.coroutines.withTimeout
import org.jetbrains.skia.Color
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.BiliData
import top.colter.mirai.plugin.bilibili.data.LIVE_LINK
import top.colter.mirai.plugin.bilibili.data.LiveInfo
import top.colter.mirai.plugin.bilibili.data.LiveMessage
import top.colter.mirai.plugin.bilibili.draw.makeDrawLive
import top.colter.mirai.plugin.bilibili.draw.makeRGB
import top.colter.mirai.plugin.bilibili.utils.formatTime
import top.colter.mirai.plugin.bilibili.utils.logger

object LiveMessageTasker : BiliTasker() {
    override var interval: Int = 0

    private val liveChannel by BiliBiliDynamic::liveChannel
    private val messageChannel by BiliBiliDynamic::messageChannel

    override suspend fun main() {
        val liveDetail = liveChannel.receive()
        withTimeout(180004) {
            val liveInfo = liveDetail.item
            logger.debug("直播: ${liveInfo.uname}@${liveInfo.uid}@${liveInfo.title}")
            messageChannel.send(liveInfo.buildMessage(liveDetail.contact))
        }
    }

    suspend fun LiveInfo.buildMessage(contact: String? = null): LiveMessage {
        return LiveMessage(
            roomId,
            uid,
            this.uname,
            liveTime.formatTime,
            liveTime.toInt(),
            title,
            cover,
            area,
            LIVE_LINK(roomId.toString()),
            makeLive(),
            contact
        )
    }

    suspend fun LiveInfo.makeLive(): String? {
        return if (BiliConfig.enableConfig.drawEnable) {
            val color = BiliData.dynamic[uid]?.color ?: BiliConfig.imageConfig.defaultColor
            val colors = color.split(";", "；").map { Color.makeRGB(it.trim()) }
            makeDrawLive(colors)
        } else null
    }

}