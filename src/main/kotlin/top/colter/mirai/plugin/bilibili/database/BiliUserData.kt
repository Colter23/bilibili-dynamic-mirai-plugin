package top.colter.mirai.plugin.bilibili.database

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import top.colter.bilibili.data.LazyImage
import top.colter.bilibili.data.live.LiveStatus
import top.colter.bilibili.data.user.*


object BiliUserData: AutoSavePluginData("BiliUser") {
    val user: MutableMap<Long, UserData> by value(mutableMapOf(
        487550002L to UserData(
            mid = 487550002L,
            color = "",
            name = "猫芒ベル_Official",
            face = LazyImage("http://i1.hdslb.com/bfs/face/652385c47e4742b6e26e19995a2407c83756b1f7.jpg"),
            pendant = Pendant(
                pid = 2425,
                name = "湊-阿库娅",
                image = LazyImage("http://i1.hdslb.com/bfs/garb/item/d8caba83a6c5d917c15436c0bead848e41a5d0b5.png")
            ),
            official = OfficialVerify(OfficialVerifyType.PERSONA),
            decorate = null,
            liveRoom = 21811136,
            subscribeCount = 1,
            laseUpdate = 1663297302
        )
    ))
}

@Serializable
data class UserData(
    override val mid: Long,
    override var name: String,
    override var face: LazyImage,
    var color: String,
    val liveRoom: Long,
    var subscribeCount: Int,
    var laseUpdate: Long,
    override var pendant: Pendant?,
    override var official: OfficialVerify,
    override var decorate: Decorate?
): BiliUser

@Serializable
data class LiveRoomInfo(
    val rid: Long,
    val roomStatus: Int,
    val liveStatus: LiveStatus,
    val lastLive: Long,
)

internal operator fun Collection<UserData>.get(mid: Long) = find { it.mid == mid }
internal fun Collection<UserData>.getByName(name: String) = find { it.name == name }

fun BiliUserInfo.toUserData(): UserData {
    return UserData(
        mid = mid,
        name = name,
        face = face,
        pendant = pendant,
        official = official ?: OfficialVerify(OfficialVerifyType.NONE),
        decorate = decorate,
        color = "",
        liveRoom = liveRoom.roomId,
        subscribeCount = 0,
        laseUpdate = System.currentTimeMillis() / 1000
    )
}