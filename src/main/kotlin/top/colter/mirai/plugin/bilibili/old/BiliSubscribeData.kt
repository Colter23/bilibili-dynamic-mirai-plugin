package top.colter.mirai.plugin.bilibili.old

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import java.time.Instant

/**
 * v2版数据，用于数据迁移
 */
object BiliSubscribeData : AutoSavePluginData("BiliSubscribeData") {
    @ValueDescription("数据是否迁移")
    var migrated: Boolean by value(false)

    @ValueDescription("订阅信息")
    val dynamic: MutableMap<Long, SubDataOld> by value(mutableMapOf(0L to SubDataOld("ALL")))
}

@Serializable
data class SubDataOld(
    @SerialName("name")
    val name: String,
    @SerialName("color")
    var color: String = "#d3edfa",
    @SerialName("last")
    var last: Long = Instant.now().epochSecond,
    @SerialName("lastLive")
    var lastLive: Long = Instant.now().epochSecond,
    @SerialName("contacts")
    val contacts: MutableMap<String, String> = mutableMapOf(),
    @SerialName("banList")
    val banList: MutableMap<String, String> = mutableMapOf(),
    @SerialName("filter")
    val filter: MutableMap<String, MutableList<String>> = mutableMapOf(),
    @SerialName("containFilter")
    val containFilter: MutableMap<String, MutableList<String>> = mutableMapOf()
)