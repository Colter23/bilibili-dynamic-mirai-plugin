package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Vote(
    @SerialName("info")
    val info: VoteInfo,
) {
    @Serializable
    data class VoteInfo(
        @SerialName("vote_id")
        val voteId: Long,
    )
}