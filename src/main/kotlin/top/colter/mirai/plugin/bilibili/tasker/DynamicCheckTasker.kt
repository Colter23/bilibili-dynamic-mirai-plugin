package top.colter.mirai.plugin.bilibili.tasker

import kotlinx.coroutines.withTimeout
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

object DynamicCheckTasker : BiliCheckTasker("Dynamic") {

    override var interval = BiliConfig.checkConfig.interval

    private val dynamicChannel by BiliBiliDynamic::dynamicChannel

    private val dynamic by BiliData::dynamic

    private val listenAllDynamicMode = false

    private val client = BiliClient()

    private val banType = listOf(
        DynamicType.DYNAMIC_TYPE_LIVE,
        DynamicType.DYNAMIC_TYPE_LIVE_RCMD,
        DynamicType.DYNAMIC_TYPE_PGC
    )

    private const val capacity = 200
    private val historyDynamic = ArrayList<String>(capacity)
    private var lastIndex = 0

    private var lastDynamic: Long = Instant.now().epochSecond

    override suspend fun main() = withTimeout(180001) {
        val dynamicList = client.getNewDynamic()
        if (dynamicList != null) {
            val followingUsers = dynamic.filter { it.value.contacts.isNotEmpty() }.map { it.key }
            val dynamics = dynamicList.items
                .filter {
                    !banType.contains(it.type)
                }.filter {
                    it.time > lastDynamic
                }.filter {
                    !historyDynamic.contains(it.did)
                }.filter {
                    if (listenAllDynamicMode) true else followingUsers.contains(it.modules.moduleAuthor.mid)
                }.sortedBy {
                    it.time
                }
            dynamics.map { it.did }.forEach {
                historyDynamic.add(lastIndex, it)
                lastIndex ++
                if (lastIndex >= capacity) lastIndex = 0
            }
            //if (dynamics.isNotEmpty()) lastDynamic = dynamics.last().time
            dynamicChannel.sendAll(dynamics.map { DynamicDetail(it) })
        }
    }

}