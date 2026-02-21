import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Liiii(
    @SerialName("code")
    val code: Int? = null,
    @SerialName("msg")
    val msg: String? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: Data? = null,
){
    @Serializable
    data class Data(
        @SerialName("uid")
        val uid: Int? = null,
        @SerialName("room_id")
        val roomId: Int? = null,
        @SerialName("short_id")
        val shortId: Int? = null,
        @SerialName("attention")
        val attention: Int? = null,
        @SerialName("online")
        val online: Int? = null,
        @SerialName("is_portrait")
        val isPortrait: Boolean? = null,
        @SerialName("description")
        val description: String? = null,
        @SerialName("live_status")
        val liveStatus: Int? = null,
        @SerialName("area_id")
        val areaId: Int? = null,
        @SerialName("parent_area_id")
        val parentAreaId: Int? = null,
        @SerialName("parent_area_name")
        val parentAreaName: String? = null,
        @SerialName("old_area_id")
        val oldAreaId: Int? = null,
        @SerialName("background")
        val background: String? = null,
        @SerialName("title")
        val title: String? = null,
        @SerialName("user_cover")
        val userCover: String? = null,
        @SerialName("keyframe")
        val keyframe: String? = null,
        @SerialName("is_strict_room")
        val isStrictRoom: Boolean? = null,
        @SerialName("live_time")
        val liveTime: String? = null,
        @SerialName("tags")
        val tags: String? = null,
        @SerialName("is_anchor")
        val isAnchor: Int? = null,
        @SerialName("room_silent_type")
        val roomSilentType: String? = null,
        @SerialName("room_silent_level")
        val roomSilentLevel: Int? = null,
        @SerialName("room_silent_second")
        val roomSilentSecond: Int? = null,
        @SerialName("area_name")
        val areaName: String? = null,
        @SerialName("pendants")
        val pendants: String? = null,
        @SerialName("area_pendants")
        val areaPendants: String? = null,
        @SerialName("hot_words")
        val hotWords: List<Int>? = null,
        @SerialName("hot_words_status")
        val hotWordsStatus: Int? = null,
        @SerialName("verify")
        val verify: String? = null,
        @SerialName("new_pendants")
        val newPendants: NewPendants? = null,
        @SerialName("up_session")
        val upSession: String? = null,
        @SerialName("pk_status")
        val pkStatus: Int? = null,
        @SerialName("pk_id")
        val pkId: Int? = null,
        @SerialName("battle_id")
        val battleId: Int? = null,
        @SerialName("allow_change_area_time")
        val allowChangeAreaTime: Int? = null,
        @SerialName("allow_upload_cover_time")
        val allowUploadCoverTime: Int? = null,
        @SerialName("studio_info")
        val studioInfo: StudioInfo? = null,
    ){
        @Serializable
        data class NewPendants(
            @SerialName("frame")
            val frame: Frame? = null,
            @SerialName("badge")
            val badge: String? = null,
            @SerialName("mobile_frame")
            val mobileFrame: MobileFrame? = null,
            @SerialName("mobile_badge")
            val mobileBadge: String? = null,
        ){
            @Serializable
            data class Frame(
                @SerialName("name")
                val name: String? = null,
                @SerialName("value")
                val value: String? = null,
                @SerialName("position")
                val position: Int? = null,
                @SerialName("desc")
                val desc: String? = null,
                @SerialName("area")
                val area: Int? = null,
                @SerialName("area_old")
                val areaOld: Int? = null,
                @SerialName("bg_color")
                val bgColor: String? = null,
                @SerialName("bg_pic")
                val bgPic: String? = null,
                @SerialName("use_old_area")
                val useOldArea: Boolean? = null,
            )
            @Serializable
            data class MobileFrame(
                @SerialName("name")
                val name: String? = null,
                @SerialName("value")
                val value: String? = null,
                @SerialName("position")
                val position: Int? = null,
                @SerialName("desc")
                val desc: String? = null,
                @SerialName("area")
                val area: Int? = null,
                @SerialName("area_old")
                val areaOld: Int? = null,
                @SerialName("bg_color")
                val bgColor: String? = null,
                @SerialName("bg_pic")
                val bgPic: String? = null,
                @SerialName("use_old_area")
                val useOldArea: Boolean? = null,
            )
        }
        @Serializable
        data class StudioInfo(
            @SerialName("status")
            val status: Int? = null,
            @SerialName("master_list")
            val masterList: List<String>? = null,
        )
    }
}