package top.colter.mirai.plugin.bilibili.tasker

import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.BiliData
import top.colter.mirai.plugin.bilibili.api.getNewDynamic
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.DynamicDetail
import top.colter.mirai.plugin.bilibili.data.DynamicType
import top.colter.mirai.plugin.bilibili.utils.sendAll
import top.colter.mirai.plugin.bilibili.utils.time
import java.time.Instant
import java.time.LocalTime

object DynamicCheckTasker : BiliTasker() {

    override var interval: Int = BiliConfig.checkConfig.interval

    private val dynamicChannel by BiliBiliDynamic::dynamicChannel

    private val dynamic by BiliData::dynamic

    private val listenAllDynamicMode = false

    private val client = BiliClient()

    private var lsl = listOf(0, 0)
    private var isLowSpeed = false

    private val banType = listOf(
        DynamicType.DYNAMIC_TYPE_LIVE,
        DynamicType.DYNAMIC_TYPE_LIVE_RCMD,
        DynamicType.DYNAMIC_TYPE_PGC
    )

    private var lastDynamic: Long = Instant.now().epochSecond

    override fun init() {
        runCatching {
            lsl = BiliConfig.checkConfig.lowSpeed.split("-", "x").map { it.toInt() }
            isLowSpeed = lsl[0] != lsl[1]
        }.onFailure {
            logger.error("低频检测参数错误 ${it.message}")
        }
    }

    override suspend fun main() {
        logger.debug("Check Dynamic...")
        val dynamicList = client.getNewDynamic()
        if (dynamicList != null) {
            //logger.info(dynamicList.updateBaseline)
            val followingUsers = dynamic.filter { it.value.contacts.isNotEmpty() }.map { it.key }
            val dynamics = dynamicList.items
                .filter {
                    !banType.contains(it.type)
                }.filter {
                    it.time > lastDynamic
                }.filter {
                    if (listenAllDynamicMode) {
                        true
                    } else {
                        followingUsers.contains(it.modules.moduleAuthor.mid)
                    }
                }.sortedBy {
                    it.time
                }

            if (dynamics.isNotEmpty()) lastDynamic = dynamics.last().time
            dynamicChannel.sendAll(dynamics.map { DynamicDetail(it) })
        }
        interval = calcTime(interval)
    }

    private fun calcTime(time: Int): Int {
        return if (isLowSpeed) {
            val hour = LocalTime.now().hour
            return if (lsl[0] > lsl[1]) {
                if (lsl[0] <= hour || hour <= lsl[1]) time * lsl[2] else time
            } else {
                if (lsl[0] <= hour && hour <= lsl[1]) time * lsl[2] else time
            }
        } else time
    }

}