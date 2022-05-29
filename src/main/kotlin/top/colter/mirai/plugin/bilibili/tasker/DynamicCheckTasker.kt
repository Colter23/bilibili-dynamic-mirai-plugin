package top.colter.mirai.plugin.bilibili.tasker

import top.colter.mirai.plugin.bilibili.BiliBiliDynamic.dynamicChannel
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic.subDynamic
import top.colter.mirai.plugin.bilibili.BiliDynamicConfig
import top.colter.mirai.plugin.bilibili.api.getNewDynamic
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.DynamicType
import top.colter.mirai.plugin.bilibili.utils.sendAll
import top.colter.mirai.plugin.bilibili.utils.time
import java.time.Instant

object DynamicCheckTasker : BiliTasker() {

    override val interval: Int = BiliDynamicConfig.checkConfig.interval

    private val client = BiliClient()

    private val banType = listOf(
        DynamicType.DYNAMIC_TYPE_LIVE,
        DynamicType.DYNAMIC_TYPE_LIVE_RCMD,
        DynamicType.DYNAMIC_TYPE_PGC
    )

    private var lastDynamic: Long = Instant.now().epochSecond

    override suspend fun main() {
        val dynamicList = client.getNewDynamic()
        if (dynamicList != null) {
            val dynamics = dynamicList.items
                .filter {
                    !banType.contains(it.type)
                }.filter {
                    it.time > lastDynamic
                }.filter {
                    subDynamic.filter { it.value.contacts.isNotEmpty() }.map { it.key }
                        .contains(it.modules.moduleAuthor.mid)
                }.sortedBy {
                    it.time
                }
            dynamicChannel.sendAll(dynamics)
        }
    }

}