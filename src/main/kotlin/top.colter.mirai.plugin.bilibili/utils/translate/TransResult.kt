package top.colter.mirai.plugin.bilibili.utils.translate

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class TransResult(
    @SerialName("from")
    val from: String,
    @SerialName("to")
    val to: String,
    @SerialName("trans_result")
    val transResult: List<TransData>
)

@Serializable
data class TransData(
    @SerialName("src")
    val src: String,
    @SerialName("dst")
    val dst: String
)