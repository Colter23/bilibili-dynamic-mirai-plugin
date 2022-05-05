package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BiliCookie(
    @SerialName("SESSDATA")
    var sessData: String = "",
    @SerialName("bili_jct")
    var biliJct: String = ""
){
    fun parse(cookie: String): BiliCookie{
        cookie.split("; ", ";").forEach {
            val cookieKV = it.split("=")
            if (cookieKV[0] == "SESSDATA") sessData = cookieKV[1]
            if (cookieKV[0] == "bili_jct") biliJct = cookieKV[1]
        }
        return this
    }

    override fun toString(): String {
        return "SESSDATA=$sessData; bili_jct=$biliJct"
    }
}