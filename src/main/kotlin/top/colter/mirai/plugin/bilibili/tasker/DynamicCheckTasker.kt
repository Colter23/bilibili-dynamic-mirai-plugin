package top.colter.mirai.plugin.bilibili.tasker

import top.colter.mirai.plugin.bilibili.BiliBiliDynamic.dynamicChannel
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic.subDynamic
import top.colter.mirai.plugin.bilibili.BiliDynamicConfig
import top.colter.mirai.plugin.bilibili.api.getNewDynamic
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.BiliCookie
import top.colter.mirai.plugin.bilibili.data.DynamicType
import top.colter.mirai.plugin.bilibili.draw.logger
import top.colter.mirai.plugin.bilibili.utils.sendAll
import top.colter.mirai.plugin.bilibili.utils.time
import java.time.Instant

object DynamicCheckTasker : BiliTasker() {

    override val interval: Int = BiliDynamicConfig.checkConfig.interval

    private val allMessageMode = true

    private val client = BiliClient()

    private val banType = listOf(
        DynamicType.DYNAMIC_TYPE_LIVE,
        DynamicType.DYNAMIC_TYPE_LIVE_RCMD,
        DynamicType.DYNAMIC_TYPE_PGC
    )

    private var lastDynamic: Long = Instant.now().epochSecond

    init {
        client.cookie = BiliCookie("f6157d07%2C1657251675%2C00db3*11", "f8934ab39f7940ca5237381e07115e7e")
    }

    override suspend fun main() {
        logger.info("Check Dynamic...")
        val dynamicList = client.getNewDynamic()
        if (dynamicList != null) {
            logger.info(dynamicList.updateBaseline)
            val dynamics = dynamicList.items
                .filter {
                    !banType.contains(it.type)
                }.filter {
                    it.time > lastDynamic
                }.filter {
                    if (allMessageMode){
                        true
                    }else{
                        subDynamic.filter { it.value.contacts.isNotEmpty() }.map { it.key }
                            .contains(it.modules.moduleAuthor.mid)
                    }
                }.sortedBy {
                    it.time
                }
            dynamicChannel.sendAll(dynamics)
        }
    }

}