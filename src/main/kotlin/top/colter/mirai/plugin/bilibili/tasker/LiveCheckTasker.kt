package top.colter.mirai.plugin.bilibili.tasker

import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.BiliDynamicConfig
import top.colter.mirai.plugin.bilibili.api.getLive
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.LiveDetail
import top.colter.mirai.plugin.bilibili.utils.sendAll
import java.time.Instant

object LiveCheckTasker : BiliTasker() {
    override val interval: Int = BiliDynamicConfig.checkConfig.liveInterval

    private val liveChannel by BiliBiliDynamic::liveChannel

    private val client = BiliClient()

    private var lastLive: Long = Instant.now().epochSecond

    override suspend fun main() {
        logger.debug("Check Live...")
        val liveList = client.getLive()

        if (liveList != null) {
            val lives = liveList.rooms
                .filter {
                    it.liveTime > lastLive
                }.filter {
                    // TODO
                    BiliBiliDynamic.subDynamic.filter { it.value.contacts.isNotEmpty() }.map { it.key }
                        .contains(it.uid)
                }.sortedBy {
                    it.liveTime
                }

            if (lives.isNotEmpty()) lastLive = lives.last().liveTime
            liveChannel.sendAll(lives.map { LiveDetail(it) })
        }

    }
}