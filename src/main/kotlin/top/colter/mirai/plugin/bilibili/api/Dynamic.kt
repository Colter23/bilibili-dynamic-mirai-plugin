package top.colter.mirai.plugin.bilibili.api

import io.ktor.client.request.*
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.BiliResult
import top.colter.mirai.plugin.bilibili.data.DynamicDetail
import top.colter.mirai.plugin.bilibili.data.DynamicItem
import top.colter.mirai.plugin.bilibili.data.DynamicList
import top.colter.mirai.plugin.bilibili.utils.decode

internal suspend inline fun <reified T> BiliClient.getData(
    url: String,
    crossinline block: HttpRequestBuilder.() -> Unit = {}
): T? {
    val res = get<BiliResult>(url, block)

    return if (res.code != 0 || res.data == null) {
        throw Exception("URL: $url ${res.message}")
    } else {
        res.data.decode()
    }
}

suspend fun BiliClient.getNewDynamic(page: Int = 1, type: String = "all"): DynamicList? {
    return getData(NEW_DYNAMIC) {
        parameter("timezone_offset", "-480")
        parameter("type", type)
        parameter("page", page)
    }
}

suspend fun BiliClient.getDynamicDetail(did: String): DynamicItem? {
    return getData<DynamicDetail>(DYNAMIC_DETAIL) {
        parameter("timezone_offset", "-480")
        parameter("id", did)
    }?.item
}

