package top.colter.mirai.plugin.bilibili.api

import io.ktor.client.request.*
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.*
import top.colter.mirai.plugin.bilibili.utils.bodyParameter
import top.colter.mirai.plugin.bilibili.utils.decode

suspend fun BiliClient.getLoginQrcode(): LoginQrcode? = getData(LOGIN_QRCODE)
suspend fun BiliClient.loginInfo(qrcodeKey: String): LoginData? {
    return getData(LOGIN_INFO) {
        parameter("qrcode_key", qrcodeKey)
    }
}

suspend fun BiliClient.userInfo(uid: Long): BiliUser? {
    return getDataWithWbi(USER_INFO_WBI) {
        parameter("mid", uid)
    }
}

suspend fun BiliClient.userInfo(): BiliUser? {
    return getData(USER_ID)
}

suspend fun BiliClient.isFollow(uid: Long): IsFollow? {
    return getData(IS_FOLLOW) {
        parameter("fid", uid)
    }
}

suspend fun BiliClient.followGroup(): List<FollowGroup>? {
    return getData(GROUP_LIST)
}

suspend fun BiliClient.createGroup(tagName: String): FollowGroup? {
    return post<BiliResult>(CREATE_GROUP) {
        bodyParameter("tag", tagName)
        bodyParameter("csrf", BiliBiliDynamic.cookie.biliJct)
    }.data?.decode()
}

suspend fun BiliClient.follow(uid: Long): BiliResult {
    return post(FOLLOW) {
        bodyParameter("fid", uid)
        bodyParameter("act", 1)
        bodyParameter("re_src", 11)
        bodyParameter("csrf", BiliBiliDynamic.cookie.biliJct)
    }
}

suspend fun BiliClient.groupAddUser(uid: Long, tagid: Int): BiliResult {
    return post(ADD_USER) {
        bodyParameter("fids", uid)
        bodyParameter("tagids", tagid)
        bodyParameter("csrf", BiliBiliDynamic.cookie.biliJct)
    }
}

suspend fun BiliClient.searchUser(
    keyword: String,
    order: String = "",
    orderSort: Int = 0,
    userType: Int = 0,
    page: Int = 1,
    pageSize: Int = 20
): BiliSearch? {
    return getData(SEARCH) {
        parameter("page", page)
        parameter("page_size", pageSize)
        parameter("search_type", "bili_user") // bili_user  video  media_bangumi  media_ft  live  article  topic
        parameter("keyword", keyword)
        parameter("order", order) // 空  fans  level
        parameter("order_sort", orderSort) // 0: 由高到低  1: 由低到高
        parameter("user_type", userType) // 0: 全部用户  1: UP主用户  2: 普通用户  3: 认证用户
    }
}

suspend fun BiliClient.searchUserVideo(
    uid: Long,
    count: Int = 1,
    order: String = "pubdate",
): VideoList? {
    return getData(SPACE_SEARCH) {
        parameter("mid", uid)
        parameter("ps", count)
        parameter("order", order)
    }
}