package top.colter.mirai.plugin.bilibili.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.BiliResult
import top.colter.mirai.plugin.bilibili.data.ShortLinkData
import top.colter.mirai.plugin.bilibili.data.WbiImg
import top.colter.mirai.plugin.bilibili.utils.*
import java.time.LocalDate

fun twemoji(code: String) = "$TWEMOJI/$code.png"

private var isLogin = true

internal suspend inline fun <reified T> BiliClient.getData(
    url: String,
    crossinline block: HttpRequestBuilder.() -> Unit = {}
): T? {
    val res = get<BiliResult>(url, block)

    return if (res.code == -101) {
        if (isLogin) actionNotify("账号登录失效，请使用 /bili login 重新登录")
        isLogin = false
        throw Exception("账号登录失效，请使用 /bili login 重新登录")
    } else if (res.code != 0 || res.data == null) {
        throw Exception("URL: $url, CODE: ${res.code}, MSG: ${res.message}")
    } else {
        isLogin = true
        res.data.decode()
    }
}
internal suspend inline fun <reified T> BiliClient.getDataWithWbi(
    url: String,
    crossinline block: HttpRequestBuilder.() -> Unit = {}
): T? {
    val builder = HttpRequestBuilder()
    builder.block()
    val params = builder.url.parameters.build().formUrlEncode()
    val wts = System.currentTimeMillis() / 1000
    val wrid = "$params&wts=$wts${getVerifyString()}".md5()
    return getData(url) {
        block()
        parameter("w_rid", wrid)
        parameter("wts", wts)
    }
}

suspend fun BiliClient.redirect(url: String): String? {
    return useHttpClient {
        it.config {
            followRedirects = false
            expectSuccess = false
        }.head(url)
    }.headers[HttpHeaders.Location]
}

suspend fun BiliClient.videoShortLink(aid: String): String? =
    toShortLink(aid, "main.ugc-video-detail.0.0.pv", "vinfo_player")

suspend fun BiliClient.articleShortLink(aid: String): String? =
    toShortLink(aid, "read.column-detail.roof.8.click")

suspend fun BiliClient.dynamicShortLink(did: String): String? =
    toShortLink(did, "dt.dt-detail.0.0.pv", "dynamic")

suspend fun BiliClient.liveShortLink(rid: String): String? =
    toShortLink(rid, "live.live-room-detail.0.0.pv", "vertical-three-point-panel")

suspend fun BiliClient.spaceShortLink(mid: String): String? =
    toShortLink(mid, "dt.space-dt.0.0.pv")

suspend fun BiliClient.toShortLink(oid: String, shareId: String, shareOrigin: String? = null): String? {
    return try {
        useHttpClient {
            it.post(SHORT_LINK) {
                bodyParameter("build", "6880300")
                bodyParameter("buvid", "abcdefg")
                bodyParameter("platform", "android")
                bodyParameter("oid", oid)
                bodyParameter("share_channel", "QQ")
                bodyParameter("share_id", shareId)
                bodyParameter("share_mode", "3")
                if (shareOrigin != null) bodyParameter("share_origin", shareOrigin)
            }.body<String>().decode<BiliResult>().data?.decode<ShortLinkData>()?.link
        }
    }catch (e: Exception) { null }
}

var lastWbiTime: LocalDate = LocalDate.now()
var wbiImg: WbiImg? = null
suspend fun getWbiImg(): WbiImg {
    val now = LocalDate.now()
    if (now.isAfter(lastWbiTime) || wbiImg == null) {
        lastWbiTime = now
        wbiImg = biliClient.userInfo()?.wbiImg
    }
    return wbiImg!!
}

suspend fun getVerifyString(): String {
    val wi = getWbiImg()
    val r = splitUrl(wi.imgUrl) + splitUrl(wi.subUrl)
    val array = intArrayOf(46,47,18,2,53,8,23,32,15,50,10,31,58,3,45,35,27,43,5,49,33,9,42,19,29,28,14,39,12,38,41,13,37,48,7,16,24,55,40,61,26,17,0,1,60,51,30,4,22,25,54,21,56,59,6,63,57,62,11,36,20,34,44,52)
    return buildString {
        array.forEach { t ->
            if (t < r.length) {
                append(r[t])
            }
        }
    }.slice(IntRange(0, 31))
}

fun splitUrl(url: String): String {
    return url.removeSuffix("/").split("/").last().split(".").first()
}