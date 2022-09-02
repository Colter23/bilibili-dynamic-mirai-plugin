package top.colter.mirai.plugin.bilibili.api

import io.ktor.client.request.*
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.*

/**
 * 获取账号全部最新动态
 * @param page 分页 (每页20左右)
 * @param type 动态类型 video: 视频  pgc: 番剧  article: 专栏
 */
suspend fun BiliClient.getNewDynamic(page: Int = 1, type: String = "all"): DynamicList? {
    return getData(NEW_DYNAMIC) {
        parameter("timezone_offset", "-480")
        parameter("type", type)
        parameter("page", page)
    }
}

/**
 * 获取用户最新动态
 * @param uid 用户ID
 * @param hasTop 是否包含置顶动态
 * @param offset 动态偏移
 */
suspend fun BiliClient.getUserNewDynamic(uid: Long, hasTop: Boolean = false, offset: String = ""): DynamicList? {
    return getData(if (hasTop) SPACE_DYNAMIC else NEW_DYNAMIC) {
        parameter("timezone_offset", "-480")
        parameter("host_mid", uid)
        parameter("offset", offset)
    }
}

/**
 * 获取指定动态详情
 * @param did 动态ID
 */
suspend fun BiliClient.getDynamicDetail(did: String): DynamicItem? {
    return getData<DynamicDetail>(DYNAMIC_DETAIL) {
        parameter("timezone_offset", "-480")
        parameter("id", did)
    }?.item
}

suspend fun BiliClient.getVideoDetail(id: String): VideoDetail? {
    return getData(VIDEO_DETAIL) {
        if (id.contains("BV")) parameter("bvid", id)
        else parameter("aid", id.removePrefix("av"))
    }
}

suspend fun BiliClient.getArticleDetailOld(id: String): ArticleDetail? {
    return getData(ARTICLE_DETAIL) {
        if (id.startsWith("cv")) parameter("id", id.removePrefix("cv"))
        else parameter("id", id)
    }
}

suspend fun BiliClient.getArticleDetail(id: String): ArticleDetail? {
    return getArticleList(listOf(id))?.get(id)
}

suspend fun BiliClient.getArticleList(ids: List<String>): Map<String, ArticleDetail>? {
    return getData(ARTICLE_LIST) {
        parameter("ids", ids.joinToString(","))
    }
}
