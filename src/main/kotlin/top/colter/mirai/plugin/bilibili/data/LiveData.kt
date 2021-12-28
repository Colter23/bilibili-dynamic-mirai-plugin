package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Live(
    @SerialName("rooms")
    val liveList: List<LiveInfo>
)

@Serializable
data class LiveInfo(
    @SerialName("title")
    val title: String,
    @SerialName("room_id")
    val roomId: Long,
    @SerialName("uid")
    val uid: Long,
    @SerialName("uname")
    val uname: String,
    @SerialName("face")
    val face: String,
    @SerialName("cover_from_user")
    val cover: String,
    @SerialName("liveTime")
    val liveTime: Long
)