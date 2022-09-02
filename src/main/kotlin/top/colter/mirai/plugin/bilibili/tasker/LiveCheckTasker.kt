package top.colter.mirai.plugin.bilibili.tasker

import kotlinx.coroutines.withTimeout
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic.liveUsers
import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.BiliData
import top.colter.mirai.plugin.bilibili.api.getLive
import top.colter.mirai.plugin.bilibili.data.LiveDetail
import top.colter.mirai.plugin.bilibili.utils.sendAll
import java.time.Instant

object LiveCheckTasker : BiliCheckTasker("Live") {
    override var interval = BiliConfig.checkConfig.liveInterval
    private val liveCloseEnable = BiliConfig.enableConfig.liveCloseNotifyEnable

    private val liveChannel by BiliBiliDynamic::liveChannel
    private val dynamic by BiliData::dynamic

    private var lastLive: Long = Instant.now().epochSecond

    override suspend fun main() = withTimeout(180003) {
        val liveList = client.getLive()

        if (liveList != null) {
            val followingUsers = dynamic.filter { it.value.contacts.isNotEmpty() }.map { it.key }
            val lives = liveList.rooms
                .filter {
                    it.liveTime > lastLive
                }.filter {
                    followingUsers.contains(it.uid)
                }.sortedBy {
                    it.liveTime
                }

            if (lives.isNotEmpty()) {
                lastLive = lives.last().liveTime
                liveChannel.sendAll(lives.map { LiveDetail(it) })
                if (liveCloseEnable) liveUsers.putAll(lives.map { it.uid to it.liveTime })
            }
        }

    }
}