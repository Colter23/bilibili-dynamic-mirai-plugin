package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginData(
    @SerialName("code")
    val code: Int? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    @SerialName("timestamp")
    val timestamp: Long? = null,
    @SerialName("url")
    val url: String? = null,
)

@Serializable
data class LoginQrcode(
    @SerialName("url")
    val url: String,
    @SerialName("qrcode_key")
    val qrcodeKey: String? = null
)