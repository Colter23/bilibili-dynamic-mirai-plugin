package top.colter.mirai.plugin.bilibili.tasker

import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.data.LiveInfo
import top.colter.mirai.plugin.bilibili.data.LiveMessage
import top.colter.mirai.plugin.bilibili.draw.makeDrawLive
import top.colter.mirai.plugin.bilibili.utils.formatTime

object LiveMessageTasker : BiliTasker() {
    override val interval: Int = 0

    private val liveChannel by BiliBiliDynamic::liveChannel
    private val messageChannel by BiliBiliDynamic::messageChannel

    override suspend fun main() {
        val liveDetail = liveChannel.receive()
        val liveInfo = liveDetail.item
        logger.debug("直播: ${liveInfo.uname}@${liveInfo.uid}@${liveInfo.title}")
        messageChannel.send(liveInfo.buildMessage(liveDetail.contact))
    }

    suspend fun LiveInfo.buildMessage(contact: String? = null): LiveMessage{
        return LiveMessage(
            roomId,
            uid,
            this.uname,
            liveTime.formatTime,
            liveTime.toInt(),
            title,
            cover,
            area,
            "https://live.bilibili.com/$roomId",
            makeLive(),
            contact
        )
    }

    suspend fun LiveInfo.makeLive(): String?{
        val drawEnable = true
        return if (drawEnable) makeDrawLive() else null
    }

}