package top.colter.mirai.plugin.bilibili.api

import io.ktor.client.request.*
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.BiliResult
import top.colter.mirai.plugin.bilibili.data.DynamicDetail
import top.colter.mirai.plugin.bilibili.data.DynamicItem
import top.colter.mirai.plugin.bilibili.data.DynamicList
import top.colter.mirai.plugin.bilibili.utils.decode

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

