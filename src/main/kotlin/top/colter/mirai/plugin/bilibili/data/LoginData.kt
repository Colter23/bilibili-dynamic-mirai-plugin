package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginData(
    @SerialName("url")
    val url: String,
    @SerialName("oauthKey")
    val oauthKey: String = ""
)