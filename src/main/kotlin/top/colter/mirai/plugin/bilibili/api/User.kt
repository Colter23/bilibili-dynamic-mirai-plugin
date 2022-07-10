package top.colter.mirai.plugin.bilibili.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.*
import top.colter.mirai.plugin.bilibili.utils.bodyParameter
import top.colter.mirai.plugin.bilibili.utils.decode

suspend fun BiliClient.getLoginUrl(): LoginResult = get(LOGIN_URL)
suspend fun BiliClient.loginInfo(oauthKey: String): LoginResult {
    return useHttpClient {
        it.post(LOGIN_INFO) {
            bodyParameter("oauthKey", oauthKey)
        }.body()
    }
}

suspend fun BiliClient.userInfo(uid: Long): BiliUser? {
    return getData(USER_INFO) {
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