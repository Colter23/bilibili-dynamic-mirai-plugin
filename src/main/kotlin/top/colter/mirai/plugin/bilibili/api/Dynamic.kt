package top.colter.mirai.plugin.bilibili.api

import io.ktor.client.request.*
import kotlinx.serialization.serializer
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.DynamicList
import top.colter.mirai.plugin.bilibili.data.DynamicResult
import top.colter.mirai.plugin.bilibili.draw.logger
import top.colter.mirai.plugin.bilibili.utils.json

suspend fun BiliClient.getNewDynamic(page: Int = 1, type: String = "all"): DynamicList? {

    val res = get<DynamicResult>(NEW_DYNAMIC){
        parameter("timezone_offset", "-480")
        parameter("type", type)
        parameter("page", page)
    }

    return if (res.code != 0 || res.data == null){
        logger.warning("获取动态失败 ${res.message}")
        null
    }else {
        json.decodeFromJsonElement(json.serializersModule.serializer(), res.data)
    }

}