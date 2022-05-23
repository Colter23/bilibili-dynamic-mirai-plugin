package top.colter.mirai.plugin.bilibili.api

import io.ktor.client.request.*
import kotlinx.serialization.serializer
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.DynamicList
import top.colter.mirai.plugin.bilibili.data.DynamicResult
import top.colter.mirai.plugin.bilibili.data.IsFollow
import top.colter.mirai.plugin.bilibili.draw.logger
import top.colter.mirai.plugin.bilibili.utils.json

internal suspend inline fun <reified T> BiliClient.getData(url: String, crossinline block: HttpRequestBuilder.() -> Unit = {}): T? {
    val res = get<DynamicResult>(url, block)

    return if (res.code != 0 || res.data == null){
        logger.error("URL:${url} ${res.message}")
        null
    }else {
        json.decodeFromJsonElement(json.serializersModule.serializer(), res.data)
    }
}

suspend fun BiliClient.getNewDynamic(page: Int = 1, type: String = "all"): DynamicList? {
    return getData(NEW_DYNAMIC){
        parameter("timezone_offset", "-480")
        parameter("type", type)
        parameter("page", page)
    }
}

suspend fun BiliClient.isFollow(uid: Long): IsFollow? {
    return getData(IS_FOLLOW){
        parameter("fid", uid)
    }
}

