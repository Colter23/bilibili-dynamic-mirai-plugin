package top.colter.mirai.plugin.bilibili.data

const val BASE_DYNAMIC = "https://t.bilibili.com"
const val BASE_ARTICLE = "https://www.bilibili.com/read"
const val BASE_VIDEO = "https://www.bilibili.com/video"
const val BASE_MUSIC = "https://www.bilibili.com/audio"
const val BASE_PGC = "https://www.bilibili.com/bangumi/play"
const val BASE_LIVE = "https://live.bilibili.com"

fun DYNAMIC_LINK(id: String) = "$BASE_DYNAMIC/$id"
fun ARTICLE_LINK(id: Long) = "$BASE_ARTICLE/cv$id"
fun VIDEO_LINK(aid: String? = null, bvid: String? = null): String {
    require(aid!=null || bvid!=null) { "aid or bvid 不能都为空格" }
    return "$BASE_VIDEO/${if(aid!=null) "av$aid" else bvid ?: ""}"
}
fun MUSIC_LINK(id: Long) = "$BASE_MUSIC/au$id"
fun PGC_LINK(id: Int) = "$BASE_PGC/ep$id"
fun LIVE_LINK(id: Long) = "$BASE_LIVE/$id"

