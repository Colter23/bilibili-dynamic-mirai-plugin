package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 统计
 */
@Serializable
data class Stats(
    @SerialName("danmaku")
    val danmaku: Int = 0,
    @SerialName("dynamic")
    val dynamic: Int = 0,
    @SerialName("view")
    val view: Int,
    @SerialName("favorite")
    val favorite: Int,
    @SerialName("like")
    val like: Int,
    @SerialName("dislike")
    val dislike: Int,
    @SerialName("reply")
    val reply: Int,
    @SerialName("share")
    val share: Int,
    @SerialName("coin")
    val coin: Int,
)