package top.colter.mirai.plugin.bilibili

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import java.time.Instant

object BiliSubscribeData : AutoSavePluginData("BiliSubscribeData") {
    @ValueDescription("订阅信息")
    val dynamic: MutableMap<Long, SubData> by value(mutableMapOf(0L to SubData("ALL")))
}

@Serializable
data class SubData(
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