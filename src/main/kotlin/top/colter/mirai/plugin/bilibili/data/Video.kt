package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoDetail(
    @SerialName("bvid")
    val bvid: String,
    @SerialName("aid")
    val aid: String,
    @SerialName("videos")
    val videos: Int? = null,
    @SerialName("tid")
    val tid: Int? = null,
    @SerialName("tname")
    val tname: String? = null,
    @SerialName("copyright")
    val copyright: Int? = null,
    @SerialName("pic")
    val pic: String,
    @SerialName("title")
    val title: String,
    @SerialName("pubdate")
    val pubdate: Long,
    @SerialName("ctime")
    val ctime: Long,
    @SerialName("desc")
    val desc: String,
    @SerialName("state")
    val state: Int? = null,
    @SerialName("duration")
    val duration: Long,
    @SerialName("mission_id")
    val missionId: Int? = null,
    @SerialName("rights")
    val rights: Rights? = null,
    @SerialName("owner")
    val owner: Owner,
    @SerialName("stat")
    val stat: Stat,
    @SerialName("dynamic")
    val dynamic: String? = null,
    @SerialName("cid")
    val cid: Int? = null,
    @SerialName("dimension")
    val dimension: Dimension? = null,
    @SerialName("season_id")
    val seasonId: Int? = null,
    @SerialName("premiere")
    val premiere: String? = null,
    @SerialName("teenage_mode")
    val teenageMode: Int? = null,
    @SerialName("is_chargeable_season")
    val isChargeableSeason: Boolean? = null,
    @SerialName("is_story")
    val isStory: Boolean? = null,
    @SerialName("no_cache")
    val noCache: Boolean? = null,
    @SerialName("pages")
    val pages: List<Pages>? = null,
    @SerialName("ugc_season")
    val ugcSeason: UgcSeason? = null,
    @SerialName("is_season_display")
    val isSeasonDisplay: Boolean? = null,
    @SerialName("like_icon")
    val likeIcon: String? = null,
): BiliDetail{
    @Serializable
    data class Rights(
        @SerialName("bp")
        val bp: Int? = null,
        @SerialName("elec")
        val elec: Int? = null,
        @SerialName("download")
        val download: Int? = null,
        @SerialName("movie")
        val movie: Int? = null,
        @SerialName("pay")
        val pay: Int? = null,
        @SerialName("hd5")
        val hd5: Int? = null,
        @SerialName("no_reprint")
        val noReprint: Int? = null,
        @SerialName("autoplay")
        val autoplay: Int? = null,
        @SerialName("ugc_pay")
        val ugcPay: Int? = null,
        @SerialName("is_cooperation")
        val isCooperation: Int? = null,
        @SerialName("ugc_pay_preview")
        val ugcPayPreview: Int? = null,
        @SerialName("no_background")
        val noBackground: Int? = null,
        @SerialName("clean_mode")
        val cleanMode: Int? = null,
        @SerialName("is_stein_gate")
        val isSteinGate: Int? = null,
        @SerialName("is_360")
        val is360: Int? = null,
        @SerialName("no_share")
        val noShare: Int? = null,
        @SerialName("arc_pay")
        val arcPay: Int? = null,
        @SerialName("free_watch")
        val freeWatch: Int? = null,
    )
    @Serializable
    data class Owner(
        @SerialName("mid")
        val mid: Long,
        @SerialName("name")
        val name: String,
        @SerialName("face")
        val face: String,
    )
    @Serializable
    data class Stat(
        @SerialName("aid")
        val aid: Int? = null,
        @SerialName("view")
        val view: Int,
        @SerialName("danmaku")
        val danmaku: Int,
        @SerialName("reply")
        val reply: Int? = null,
        @SerialName("favorite")
        val favorite: Int? = null,
        @SerialName("coin")
        val coin: Int? = null,
        @SerialName("share")
        val share: Int? = null,
        @SerialName("now_rank")
        val nowRank: Int? = null,
        @SerialName("his_rank")
        val hisRank: Int? = null,
        @SerialName("like")
        val like: Int? = null,
        @SerialName("dislike")
        val dislike: Int? = null,
        @SerialName("evaluation")
        val evaluation: String? = null,
        @SerialName("argue_msg")
        val argueMsg: String? = null,
    )
    @Serializable
    data class Dimension(
        @SerialName("width")
        val width: Int? = null,
        @SerialName("height")
        val height: Int? = null,
        @SerialName("rotate")
        val rotate: Int? = null,
    )
    @Serializable
    data class Pages(
        @SerialName("cid")
        val cid: Int? = null,
        @SerialName("page")
        val page: Int? = null,
        @SerialName("from")
        val from: String? = null,
        @SerialName("part")
        val part: String? = null,
        @SerialName("duration")
        val duration: Int? = null,
        @SerialName("vid")
        val vid: String? = null,
        @SerialName("weblink")
        val weblink: String? = null,
        @SerialName("dimension")
        val dimension: Dimension? = null,
        @SerialName("first_frame")
        val firstFrame: String? = null,
    ){
        @Serializable
        data class Dimension(
            @SerialName("width")
            val width: Int? = null,
            @SerialName("height")
            val height: Int? = null,
            @SerialName("rotate")
            val rotate: Int? = null,
        )
    }

    @Serializable
    data class UgcSeason(
        @SerialName("id")
        val id: Int? = null,
        @SerialName("title")
        val title: String? = null,
        @SerialName("cover")
        val cover: String? = null,
        @SerialName("mid")
        val mid: Int? = null,
        @SerialName("intro")
        val intro: String? = null,
        @SerialName("sign_state")
        val signState: Int? = null,
        @SerialName("attribute")
        val attribute: Int? = null,
        @SerialName("ep_count")
        val epCount: Int? = null,
        @SerialName("season_type")
        val seasonType: Int? = null,
        @SerialName("is_pay_season")
        val isPaySeason: Boolean? = null,
    )
}
