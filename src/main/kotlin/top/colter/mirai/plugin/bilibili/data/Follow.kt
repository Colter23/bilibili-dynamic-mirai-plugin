package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IsFollow(
    //0：未关注
    //2：已关注
    //6：已互粉
    //128：拉黑
    @SerialName("attribute")
    val attribute: Int
)

@Serializable
data class FollowGroup(
    @SerialName("tagid")
    val tagId: Int,
    @SerialName("name")
    val name: String = "",
    @SerialName("count")
    val count: Int = 0
)