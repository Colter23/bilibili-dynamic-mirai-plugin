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

    private val client = BiliClient().apply {
        cookie = BiliCookie.parse(BiliDynamicConfig.biliAccountConfig.cookie)
    }

    private val banType = listOf(
        DynamicType.DYNAMIC_TYPE_LIVE,
        DynamicType.DYNAMIC_TYPE_LIVE_RCMD,
        DynamicType.DYNAMIC_TYPE_PGC
    )

    private var lastDynamic: Long = Instant.now().epochSecond


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

            if (dynamics.isNotEmpty()) lastDynamic = dynamics.last().time
            dynamicChannel.sendAll(dynamics)
        }
    }

}