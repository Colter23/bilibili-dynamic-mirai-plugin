package top.colter.mirai.plugin.bilibili.data

import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.api.articleShortLink
import top.colter.mirai.plugin.bilibili.api.dynamicShortLink
import top.colter.mirai.plugin.bilibili.api.liveShortLink
import top.colter.mirai.plugin.bilibili.api.spaceShortLink
import top.colter.mirai.plugin.bilibili.utils.biliClient

const val BASE_DYNAMIC = "https://t.bilibili.com"
const val BASE_ARTICLE = "https://www.bilibili.com/read"
const val BASE_VIDEO = "https://www.bilibili.com/video"
const val BASE_MUSIC = "https://www.bilibili.com/audio"
const val BASE_PGC = "https://www.bilibili.com/bangumi/play"
const val BASE_PGC_MEDIA = "https://www.bilibili.com/bangumi/media"
const val BASE_LIVE = "https://live.bilibili.com"
const val BASE_SPACE = "https://space.bilibili.com"
const val BASE_SHORT = "b23.tv"

val toShortLink: Boolean by lazy { BiliConfig.pushConfig.toShortLink }

suspend fun DYNAMIC_LINK(id: String) =
    if (toShortLink) biliClient.dynamicShortLink(id).run {
        this?.removePrefix("https://") ?: "$BASE_DYNAMIC/$id"
    } else "$BASE_DYNAMIC/$id"

suspend fun ARTICLE_LINK(id: String) =
    if (toShortLink) {
        biliClient.articleShortLink(id).run {
            this?.removePrefix("https://") ?: "$BASE_ARTICLE/cv$id"
        }
    }else "$BASE_ARTICLE/cv$id"

fun VIDEO_LINK(id: String): String {
    val tid = if (id.contains("BV") || id.contains("av")) id else "av$id"
    return if (toShortLink) "$BASE_SHORT/$tid" else "$BASE_VIDEO/$tid"
}

suspend fun SPACE_LINK(id: String): String = if (toShortLink) {
    biliClient.spaceShortLink(id).run {
        this?.removePrefix("https://") ?: "$BASE_SPACE/$id"
    }
} else "$BASE_SPACE/$id"

fun MUSIC_LINK(id: String) = "$BASE_MUSIC/au$id"
fun MEDIA_LINK(id: String) = "$BASE_PGC_MEDIA/md$id"
fun SEASON_LINK(id: String) = if (toShortLink) "$BASE_SHORT/ss$id" else "$BASE_PGC/ss$id"
fun EPISODE_LINK(id: String) = if (toShortLink) "$BASE_SHORT/ep$id" else "$BASE_PGC/ep$id"
fun PGC_LINK(id: String) = if (toShortLink) "$BASE_SHORT/$id" else "$BASE_PGC/$id"
suspend fun LIVE_LINK(id: String) = if (toShortLink) {
    biliClient.liveShortLink(id).run {
        this?.removePrefix("https://") ?: "$BASE_LIVE/$id"
    }
}else "$BASE_LIVE/$id"

