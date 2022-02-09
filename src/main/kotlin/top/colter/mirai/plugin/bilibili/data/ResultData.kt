package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ResultData(
    @SerialName("code")
    val code: Int? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("status")
    val status: Boolean? = null,
    @SerialName("msg")
    val msg: String? = null,
    @SerialName("data")
    val data: JsonElement? = null
)
