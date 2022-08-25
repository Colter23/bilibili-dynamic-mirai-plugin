package top.colter.mirai.plugin.bilibili.utils.translate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransResult(
    @SerialName("from")
    val from: String? = null,
    @SerialName("to")
    val to: String? = null,
    @SerialName("trans_result")
    val transResult: List<TransData>? = null,
    @SerialName("error_code")
    val errorCode: String? = null,
    @SerialName("error_msg")
    val errorMsg: String? = null,
)

@Serializable
data class TransData(
    @SerialName("src")
    val src: String,
    @SerialName("dst")
    val dst: String
)