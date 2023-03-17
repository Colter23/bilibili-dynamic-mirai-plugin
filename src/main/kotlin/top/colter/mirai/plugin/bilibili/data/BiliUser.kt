package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BiliUser(
    @SerialName("mid")
    val mid: Long,
    @SerialName("name")
    val name: String? = "",
    @SerialName("face")
    val face: String? = "",
    @SerialName("official")
    val official: ModuleAuthor.OfficialVerify? = null,
    @SerialName("vip")
    val vip: ModuleAuthor.Vip? = null,
    @SerialName("pendant")
    val pendant: ModuleAuthor.Pendant? = null,
    //@SerialName("sex")
    //val sex: String,
    //@SerialName("face_nft")
    //val faceNft: Int,
    //@SerialName("face_nft_type")
    //val faceNftType: Int,
    //@SerialName("rank")
    //val rank: Int,
    //@SerialName("level")
    //val level: Int,
    //@SerialName("moral")
    //val moral: Int,
    //@SerialName("silence")
    //val silence: Int,
    //@SerialName("coins")
    //val coins: Int,
    //@SerialName("is_followed")
    //val isFollowed: Boolean,
    //@SerialName("top_photo")
    //val top_photo: String,
    //@SerialName("birthday")
    //val birthday: String,
    //@SerialName("is_senior_member")
    //val isSeniorMember: Int,
    //@SerialName("fans_badge")
    //val fansBadge: Boolean,
    //@SerialName("fans_medal")
    //val fansMedal: FansMedal,
    //@SerialName("nameplate")
    //val nameplate: Nameplate,
    //@SerialName("user_honour_info")
    //val userHonourInfo: UserHonourInfo,
    //@SerialName("live_room")
    //val liveRoom: LiveRoom,
    //@SerialName("school")
    //val school: School,
    //@SerialName("profession")
    //val profession: Profession,
    //@SerialName("tags")
    //val tags: List<String>,
    //@SerialName("series")
    //val series: Series,

    //@SerialName("theme")
    //val theme: ?,
    //@SerialName("sys_notice")
    //val sysNotice: ?,

): BiliDetail {
    @Serializable
    data class Nameplate(
        @SerialName("nid")
        val nid: Int,
        @SerialName("name")
        val name: String,
        @SerialName("image")
        val image: String,
        @SerialName("image_small")
        val imageSmall: String,
        @SerialName("level")
        val level: String,
        @SerialName("condition")
        val condition: String
    )

    @Serializable
    data class UserHonourInfo(
        @SerialName("mid")
        val mid: Long,
        @SerialName("colour")
        val colour: String?,
        @SerialName("tags")
        val tags: List<String>,
    )

    @Serializable
    data class School(
        @SerialName("name")
        val name: String,
    )

    @Serializable
    data class Profession(
        @SerialName("name")
        val name: String,
        @SerialName("department")
        val department: String,
        @SerialName("title")
        val title: String,
        @SerialName("is_show")
        val isShow: Int,
    )

    @Serializable
    data class Series(
        @SerialName("user_upgrade_status")
        val userUpgradeStatus: Int,
        @SerialName("show_upgrade_window")
        val showUpgradeWindow: Boolean,
    )

}

@Serializable
data class FansMedal(
    @SerialName("show")
    val show: Boolean,
    @SerialName("wear")
    val wear: Boolean,
    @SerialName("medal")
    val medal: Medal,
) {
    @Serializable
    data class Medal(
        @SerialName("uid")
        val uid: Long,
        @SerialName("target_id")
        val targetId: Long,
        @SerialName("medal_id")
        val medalId: Long,
        @SerialName("level")
        val level: Int,
        @SerialName("medal_name")
        val medalName: String,
        @SerialName("intimacy")
        val intimacy: Int,
        @SerialName("next_intimacy")
        val nextIntimacy: Int,
        @SerialName("day_limit")
        val dayLimit: Int,
        @SerialName("medal_color")
        val medalColor: Int,
        @SerialName("medal_color_start")
        val medalColorStart: Int,
        @SerialName("medal_color_end")
        val medalColorEnd: Int,
        @SerialName("medal_color_border")
        val medalColorBorder: Int,
        @SerialName("is_lighted")
        val isLighted: Int,
        @SerialName("light_status")
        val lightStatus: Int,
        @SerialName("wearing_status")
        val wearingStatus: Int,
        @SerialName("score")
        val score: Int,
    )
}

@Serializable
data class Official(
    @SerialName("role")
    val role: Int,
    @SerialName("title")
    val title: String,
    @SerialName("desc")
    val desc: String,
    @SerialName("type")
    val type: Int,
)

@Serializable
data class LiveRoom(
    @SerialName("roomStatus")
    val roomStatus: Int,
    @SerialName("liveStatus")
    val liveStatus: Int,
    @SerialName("url")
    val url: String,
    @SerialName("title")
    val title: String,
    @SerialName("cover")
    val cover: String,
    @SerialName("roomid")
    val roomid: Long,
    @SerialName("roundStatus")
    val roundStatus: Int,
    @SerialName("broadcast_type")
    val broadcastType: Int,
    @SerialName("watched_show")
    val watchedShow: WatchedShow,
) {
    @Serializable
    data class WatchedShow(
        @SerialName("switch")
        val switch: Boolean,
        @SerialName("num")
        val num: Int,
        @SerialName("text_small")
        val textSmall: Int,
        @SerialName("text_large")
        val textLarge: String,
        @SerialName("icon")
        val icon: String,
        @SerialName("icon_location")
        val iconLocation: String,
        @SerialName("icon_web")
        val iconWeb: String,
    )
}

