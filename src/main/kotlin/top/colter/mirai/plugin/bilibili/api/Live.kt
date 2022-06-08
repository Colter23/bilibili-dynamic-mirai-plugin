package top.colter.mirai.plugin.bilibili.api

import io.ktor.client.request.*
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.LiveList

suspend fun BiliClient.getLive(page: Int = 1, pageSize: Int = 20): LiveList? {
    return getData(LIVE_LIST) {
        parameter("page", page)
        parameter("page_size", pageSize)
    }
}