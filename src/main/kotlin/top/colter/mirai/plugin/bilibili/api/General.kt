package top.colter.mirai.plugin.bilibili.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.BiliResult
import top.colter.mirai.plugin.bilibili.data.ShortLinkData
import top.colter.mirai.plugin.bilibili.utils.actionNotify
import top.colter.mirai.plugin.bilibili.utils.bodyParameter
import top.colter.mirai.plugin.bilibili.utils.decode

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