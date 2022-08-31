package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class LiveList(
    @SerialName("rooms")
    val rooms: List<LiveInfo>
)

@Serializable
data class LiveDetail(
    @SerialName("item")
    val item: LiveInfo,

    @SerialName("contact")
    val contact: String? = null
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
    val liveTimeStart: Long? = null,
    @SerialName("live_time")
    val liveTimeDuration: Long,
    @SerialName("live_status")
    val liveStatus: Int,
    @SerialName("area_v2_name")
    val area: String,
){
    val liveTime get() = liveTimeStart ?: liveTimeDuration
}
