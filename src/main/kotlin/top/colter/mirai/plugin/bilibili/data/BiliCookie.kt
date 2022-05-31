package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BiliCookie(
    @SerialName("SESSDATA")
    var sessData: String = "",
    @SerialName("bili_jct")
    var biliJct: String = ""
) {

    companion object {
        fun parse(cookie: String): BiliCookie {
            val c = BiliCookie()
            cookie.split("; ", ";").forEach {
                val cookieKV = it.split("=")
                if (cookieKV[0] == "SESSDATA") c.sessData = cookieKV[1]
                if (cookieKV[0] == "bili_jct") c.biliJct = cookieKV[1]
            }
            return c
        }
    }

    override fun toString(): String {
        return "SESSDATA=$sessData; bili_jct=$biliJct"
    }
}