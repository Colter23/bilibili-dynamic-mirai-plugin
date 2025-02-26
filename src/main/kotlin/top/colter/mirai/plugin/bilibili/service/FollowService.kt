package top.colter.mirai.plugin.bilibili.service

import top.colter.mirai.plugin.bilibili.data.Receiver
import top.colter.mirai.plugin.bilibili.database.BiliSubscribe
import top.colter.mirai.plugin.bilibili.database.BiliUserData
import top.colter.mirai.plugin.bilibili.database.SubscribeData
import top.colter.mirai.plugin.bilibili.database.UserData

object FollowService {

    val user: MutableMap<Long, UserData> by BiliUserData::user
    val subscribe: MutableMap<Receiver, SubscribeData> by BiliSubscribe::subscribe

    suspend fun follow(user: String, target: Receiver) {
        val u = user.toLongOrNull()
        if (u != null) {
//            followByUid(u, target)
        }else {
            followByUname(user, target)
        }
    }

    // sender
//    suspend fun followByUid(uid: Long, target: Receiver): BiliUser {
//        val userInfo = user[uid] ?: biliClient.getUserInfo(uid).toUserData()
//        subscribe.getOrPut(target) { SubscribeData() }.
//
//
//    }

    suspend fun followByUname(uname: String, target: Receiver) {

    }

}