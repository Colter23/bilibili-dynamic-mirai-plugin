package top.colter.mirai.plugin.bilibili.api

import io.ktor.client.request.*
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.LiveInfo
import top.colter.mirai.plugin.bilibili.data.LiveList
import top.colter.mirai.plugin.bilibili.data.LiveRoomDetail

suspend fun BiliClient.getLive(page: Int = 1, pageSize: Int = 20): LiveList? {
    return getData(LIVE_LIST) {
        parameter("page", page)
        parameter("page_size", pageSize)
    }
}

suspend fun BiliClient.getLiveStatus(uids: List<Long>): Map<Long, LiveInfo>? {
    return getData(LIVE_STATUS_BATCH) {
        for (uid in uids) {
            parameter("uids[]", uid)
        }
    }
}

suspend fun BiliClient.getLiveDetail(rid: String): LiveRoomDetail? {
    return getData(LIVE_DETAIL) {
        parameter("room_id", rid)
    }
}