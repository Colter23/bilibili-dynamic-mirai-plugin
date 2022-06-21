package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class BiliResult(
    @SerialName("code")
    val code: Int,
    @SerialName("message")
    val message: String? = null,
    @SerialName("ttl")
    val ttl: Int? = null,
    @SerialName("data")
    val data: JsonElement? = null
)

@Serializable
data class LoginResult(
    @SerialName("code")
    val code: Int? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("ts")
    val ts: Int? = null,
    @SerialName("status")
    val status: Boolean? = null,
    @SerialName("data")
    val data: JsonElement? = null
)

@Serializable
data class LoginData(
    @SerialName("url")
    val url: String,
    @SerialName("oauthKey")
    val oauthKey: String? = null
)