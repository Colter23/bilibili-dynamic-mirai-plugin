package top.colter.miraiplugin.bean

import kotlinx.serialization.Serializable

@Serializable
data class User (
    // 名用户名
    var name: String="",
    // 用户ID
    var uid: String="",
    // 动态ID
    var dynamicId: String="",
    // 直播间号
    var liveRoom: String="",
    // 直播间状态
    var liveStatus: Int=0
)