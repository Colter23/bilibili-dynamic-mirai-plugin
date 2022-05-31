package top.colter.mirai.plugin.bilibili.api

import io.ktor.client.request.*
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.BiliUser
import top.colter.mirai.plugin.bilibili.data.DynamicResult
import top.colter.mirai.plugin.bilibili.data.IsFollow
import top.colter.mirai.plugin.bilibili.data.LoginResult

suspend fun BiliClient.getLoginUrl(): LoginResult = get(LOGIN_URL)

suspend fun BiliClient.userInfo(uid: Long): BiliUser? {
    return getData(USER_INFO) {
        parameter("mid", uid)
    }
}

suspend fun BiliClient.isFollow(uid: Long): IsFollow? {
    return getData(IS_FOLLOW) {
        parameter("fid", uid)
    }
}

suspend fun BiliClient.follow(uid: Long): DynamicResult {
    return useHttpClient {
        it.post(FOLLOW){
            parameter("fid", uid)
            parameter("act", 1)
            parameter("re_src", 11)
            parameter("csrf", cookie!!.biliJct)
        }
    }
}

suspend fun BiliClient.groupAddUser(uid: Long, tagid: Int): DynamicResult {
    return useHttpClient {
        it.post(ADD_USER){
            parameter("fids", uid)
            parameter("tagids", tagid)
            parameter("csrf", cookie!!.biliJct)
        }
    }
}