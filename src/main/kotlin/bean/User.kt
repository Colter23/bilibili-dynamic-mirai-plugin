package top.colter.mirai.plugin.bean

import kotlinx.serialization.Serializable

@Serializable
class User {
    // 名用户名
    var name = ""
    // 用户ID
    var uid = ""
    // 动态ID
    var dynamicId = ""
    // 直播间号
    var liveRoom = ""
    // 直播间状态
    var liveStatus = 0
}