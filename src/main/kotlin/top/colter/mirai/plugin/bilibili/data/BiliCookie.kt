package top.colter.mirai.plugin.bilibili.data

import io.ktor.http.*
import io.ktor.util.date.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ([\w-]+)=([\w-%]+);?
// (DedeUserID|DedeUserID__ckMd5|SESSDATA|bili_jct)=([\w-%]+);?

@Serializable
data class BiliCookie(
    @SerialName("SESSDATA")
    var sessData: String = "",
    @SerialName("bili_jct")
    var biliJct: String = ""
) {
    companion object {
        fun parse(cookie: String): BiliCookie {
            return BiliCookie().apply {
                cookie.split("; ", ";").forEach {
                    val cookieKV = it.split("=")
                    if (cookieKV[0] == "SESSDATA") sessData = cookieKV[1].replace(",", "%2C").replace("*", "%2A")
                    if (cookieKV[0] == "bili_jct") biliJct = cookieKV[1]
                }
            }
        }
    }

    fun parse(cookie: String): BiliCookie {
        val c = BiliCookie.parse(cookie)
        sessData = c.sessData
        biliJct = c.biliJct
        return this
    }

    fun isEmpty(): Boolean = sessData == "" && biliJct == ""

    override fun toString(): String {
        return "SESSDATA=$sessData; bili_jct=$biliJct"
    }
}

@Serializable
data class EditThisCookie(
    @SerialName("domain")
    val domain: String,
    @SerialName("expirationDate")
    val expirationDate: Double? = null,
    @SerialName("hostOnly")
    val hostOnly: Boolean = false,
    @SerialName("httpOnly")
    val httpOnly: Boolean,
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("path")
    val path: String,
    @SerialName("sameSite")
    val sameSite: String = "unspecified",
    @SerialName("secure")
    val secure: Boolean,
    @SerialName("session")
    val session: Boolean = false,
    @SerialName("storeId")
    val storeId: String = "0",
    @SerialName("value")
    val value: String
)
fun EditThisCookie.toCookie() = Cookie(
    name = name,
    value = value,
    encoding = CookieEncoding.RAW,
    expires = expirationDate?.run { GMTDate(times(1000).toLong()) },
    domain = domain,
    path = path,
    secure = secure,
    httpOnly = httpOnly
)

fun Cookie.toEditThisCookie(id: Int = 0) = EditThisCookie(
    name = name,
    value = value,
    expirationDate = expires?.timestamp?.toDouble()?.div(1000),
    domain = domain.orEmpty(),
    path = path.orEmpty(),
    secure = secure,
    httpOnly = httpOnly,
    id = id
)


fun List<EditThisCookie>.toCookie(): BiliCookie {
    val bc = BiliCookie()
    for (cookie in this) {
        if (cookie.name == "SESSDATA") bc.sessData = cookie.value
        if (cookie.name == "bili_jct") bc.biliJct = cookie.value
    }
    return bc
}