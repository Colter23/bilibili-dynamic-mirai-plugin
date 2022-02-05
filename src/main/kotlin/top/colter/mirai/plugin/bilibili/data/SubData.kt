package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

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
