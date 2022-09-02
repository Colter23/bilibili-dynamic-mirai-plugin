package top.colter.mirai.plugin.bilibili.api

import io.ktor.client.request.*
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.*
import top.colter.mirai.plugin.bilibili.service.pgcRegex
import top.colter.mirai.plugin.bilibili.utils.bodyParameter
import top.colter.mirai.plugin.bilibili.utils.decode


internal suspend inline fun <reified T> BiliClient.pgcGet(
    url: String,
    crossinline block: HttpRequestBuilder.() -> Unit = {}
): T? = get<PgcResult>(url, block).result?.decode()


suspend fun BiliClient.followPgc(ssid: Long): PgcFollow? {
    return post<PgcResult>(FOLLOW_PGC) {
        bodyParameter("season_id", ssid)
        bodyParameter("csrf", BiliBiliDynamic.cookie.biliJct)
    }.result?.decode()
}

suspend fun BiliClient.unFollowPgc(ssid: Long): PgcFollow? {
    return post<PgcResult>(UNFOLLOW_PGC) {
        bodyParameter("season_id", ssid)
        bodyParameter("csrf", BiliBiliDynamic.cookie.biliJct)
    }.result?.decode()
}

suspend fun BiliClient.getPcgInfo(id: String): BiliDetail? {
    val regex = pgcRegex.find(id) ?: return null

    val type = regex.destructured.component1()
    val id = regex.destructured.component2().toLong()

    return when (type) {
        "ss" -> getSeasonInfo(id)
        "md" -> getMediaInfo(id)
        "ep" -> getEpisodeInfo(id)
        else -> null
    }
}

suspend fun BiliClient.getMediaInfo(mdid: Long): PgcMedia? {
    return pgcGet(PGC_MEDIA_INFO) {
        parameter("media_id", mdid)
    }
}

suspend fun BiliClient.getEpisodeInfo(epid: Long): PgcSeason? {
    return pgcGet(PGC_INFO) {
        parameter("ep_id", epid)
    }
}

suspend fun BiliClient.getSeasonInfo(ssid: Long): PgcSeason? {
    return pgcGet(PGC_INFO) {
        parameter("season_id", ssid)
    }
}