package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PgcFollow(
    @SerialName("fmid")
    val fmid: Int,
    @SerialName("relation")
    val relation: Boolean,
    @SerialName("status")
    val status: Int,
    @SerialName("toast")
    val toast: String,
)

@Serializable
data class PgcMedia(
    @SerialName("media")
    val media: Media,
) {
    @Serializable
    data class Media(
        @SerialName("media_id")
        val mediaId: Long,
        @SerialName("season_id")
        val seasonId: Long,
        @SerialName("title")
        val title: String,
        @SerialName("cover")
        val cover: String,
        @SerialName("horizontal_picture")
        val horizontalPicture: String,
        @SerialName("type")
        val type: Int,
        @SerialName("type_name")
        val typeName: String,

        //@SerialName("areas")
        //val areas: List<Areas>? = null,
        //@SerialName("new_ep")
        //val newEp: NewEp? = null,
        //@SerialName("share_url")
        //val shareUrl: String? = null,

    ){
        @Serializable
        data class Areas(
            @SerialName("id")
            val id: Int? = null,
            @SerialName("name")
            val name: String? = null,
        )
        @Serializable
        data class NewEp(
            @SerialName("id")
            val id: Int? = null,
            @SerialName("index")
            val index: Int? = null,
            @SerialName("index_show")
            val indexShow: String? = null,
        )
    }
}


@Serializable
data class PgcSeason(
    @SerialName("cover")
    val cover: String,
    @SerialName("media_id")
    val mediaId: Long,
    @SerialName("mode")
    val mode: Int? = null,
    @SerialName("season_id")
    val seasonId: Long,
    @SerialName("season_title")
    val seasonTitle: String? = null,
    @SerialName("square_cover")
    val squareCover: String? = null,
    @SerialName("stat")
    val stat: Stat? = null,
    @SerialName("status")
    val status: Int? = null,
    @SerialName("subtitle")
    val subtitle: String? = null,
    @SerialName("title")
    val title: String,
    @SerialName("total")
    val total: Int? = null,
    @SerialName("type")
    val type: Int,

    //@SerialName("activity")
    //val activity: Activity? = null,
    //@SerialName("alias")
    //val alias: String? = null,
    //@SerialName("areas")
    //val areas: List<Areas>? = null,
    //@SerialName("bkg_cover")
    //val bkgCover: String? = null,
    //@SerialName("episodes")
    //val episodes: List<Episodes>? = null,
    //@SerialName("evaluate")
    //val evaluate: String? = null,
    //@SerialName("freya")
    //val freya: Freya? = null,
    //@SerialName("jp_title")
    //val jpTitle: String? = null,
    //@SerialName("link")
    //val link: String? = null,
    //@SerialName("new_ep")
    //val newEp: NewEp? = null,
    //@SerialName("payment")
    //val payment: Payment? = null,
    //@SerialName("positive")
    //val positive: Positive? = null,
    //@SerialName("publish")
    //val publish: Publish? = null,
    //@SerialName("rating")
    //val rating: Rating? = null,
    //@SerialName("record")
    //val record: String? = null,
    //@SerialName("rights")
    //val rights: Rights? = null,
    //@SerialName("seasons")
    //val seasons: List<Seasons>? = null,
    //@SerialName("section")
    //val section: List<Section>? = null,
    //@SerialName("series")
    //val series: Series? = null,
    //@SerialName("share_copy")
    //val shareCopy: String? = null,
    //@SerialName("share_sub_title")
    //val shareSubTitle: String? = null,
    //@SerialName("share_url")
    //val shareUrl: String? = null,
    //@SerialName("show")
    //val show: Show? = null,
    //@SerialName("show_season_type")
    //val showSeasonType: Int? = null,
    //@SerialName("up_info")
    //val upInfo: UpInfo? = null,
    //@SerialName("user_status")
    //val userStatus: UserStatus? = null,
){
    @Serializable
    data class Activity(
        @SerialName("head_bg_url")
        val headBgUrl: String? = null,
        @SerialName("id")
        val id: Int? = null,
        @SerialName("title")
        val title: String? = null,
    )
    @Serializable
    data class Areas(
        @SerialName("id")
        val id: Int? = null,
        @SerialName("name")
        val name: String? = null,
    )
    @Serializable
    data class Episodes(
        @SerialName("aid")
        val aid: Int? = null,
        @SerialName("badge")
        val badge: String? = null,
        @SerialName("badge_info")
        val badgeInfo: BadgeInfo? = null,
        @SerialName("badge_type")
        val badgeType: Int? = null,
        @SerialName("bvid")
        val bvid: String? = null,
        @SerialName("cid")
        val cid: Int? = null,
        @SerialName("cover")
        val cover: String? = null,
        @SerialName("dimension")
        val dimension: Dimension? = null,
        @SerialName("duration")
        val duration: Int? = null,
        @SerialName("from")
        val from: String? = null,
        @SerialName("id")
        val id: Int? = null,
        @SerialName("is_view_hide")
        val isViewHide: Boolean? = null,
        @SerialName("link")
        val link: String? = null,
        @SerialName("long_title")
        val longTitle: String? = null,
        @SerialName("pub_time")
        val pubTime: Int? = null,
        @SerialName("pv")
        val pv: Int? = null,
        @SerialName("release_date")
        val releaseDate: String? = null,
        @SerialName("rights")
        val rights: Rights? = null,
        @SerialName("share_copy")
        val shareCopy: String? = null,
        @SerialName("share_url")
        val shareUrl: String? = null,
        @SerialName("short_link")
        val shortLink: String? = null,
        @SerialName("status")
        val status: Int? = null,
        @SerialName("subtitle")
        val subtitle: String? = null,
        @SerialName("title")
        val title: Int? = null,
        @SerialName("vid")
        val vid: String? = null,
    ){
        @Serializable
        data class BadgeInfo(
            @SerialName("bg_color")
            val bgColor: String? = null,
            @SerialName("bg_color_night")
            val bgColorNight: String? = null,
            @SerialName("text")
            val text: String? = null,
        )
        @Serializable
        data class Dimension(
            @SerialName("height")
            val height: Int? = null,
            @SerialName("rotate")
            val rotate: Int? = null,
            @SerialName("width")
            val width: Int? = null,
        )
        @Serializable
        data class Rights(
            @SerialName("allow_demand")
            val allowDemand: Int? = null,
            @SerialName("allow_dm")
            val allowDm: Int? = null,
            @SerialName("allow_download")
            val allowDownload: Int? = null,
            @SerialName("area_limit")
            val areaLimit: Int? = null,
        )
    }
    @Serializable
    data class Freya(
        @SerialName("bubble_desc")
        val bubbleDesc: String? = null,
        @SerialName("bubble_show_cnt")
        val bubbleShowCnt: Int? = null,
        @SerialName("icon_show")
        val iconShow: Int? = null,
    )
    @Serializable
    data class NewEp(
        @SerialName("desc")
        val desc: String? = null,
        @SerialName("id")
        val id: Int? = null,
        @SerialName("is_new")
        val isNew: Int? = null,
        @SerialName("title")
        val title: Int? = null,
    )
    @Serializable
    data class Payment(
        @SerialName("discount")
        val discount: Int? = null,
        @SerialName("pay_type")
        val payType: PayType? = null,
        @SerialName("price")
        val price: Float? = null,
        @SerialName("promotion")
        val promotion: String? = null,
        @SerialName("tip")
        val tip: String? = null,
        @SerialName("view_start_time")
        val viewStartTime: Int? = null,
        @SerialName("vip_discount")
        val vipDiscount: Int? = null,
        @SerialName("vip_first_promotion")
        val vipFirstPromotion: String? = null,
        @SerialName("vip_promotion")
        val vipPromotion: String? = null,
    ){
        @Serializable
        data class PayType(
            @SerialName("allow_discount")
            val allowDiscount: Int? = null,
            @SerialName("allow_pack")
            val allowPack: Int? = null,
            @SerialName("allow_ticket")
            val allowTicket: Int? = null,
            @SerialName("allow_time_limit")
            val allowTimeLimit: Int? = null,
            @SerialName("allow_vip_discount")
            val allowVipDiscount: Int? = null,
            @SerialName("forbid_bb")
            val forbidBb: Int? = null,
        )
    }
    @Serializable
    data class Positive(
        @SerialName("id")
        val id: Int? = null,
        @SerialName("title")
        val title: String? = null,
    )
    @Serializable
    data class Publish(
        @SerialName("is_finish")
        val isFinish: Int? = null,
        @SerialName("is_started")
        val isStarted: Int? = null,
        @SerialName("pub_time")
        val pubTime: String? = null,
        @SerialName("pub_time_show")
        val pubTimeShow: String? = null,
        @SerialName("unknow_pub_date")
        val unknowPubDate: Int? = null,
        @SerialName("weekday")
        val weekday: Int? = null,
    )
    @Serializable
    data class Rating(
        @SerialName("count")
        val count: Int? = null,
        @SerialName("score")
        val score: Float? = null,
    )
    @Serializable
    data class Rights(
        @SerialName("allow_bp")
        val allowBp: Int? = null,
        @SerialName("allow_bp_rank")
        val allowBpRank: Int? = null,
        @SerialName("allow_download")
        val allowDownload: Int? = null,
        @SerialName("allow_review")
        val allowReview: Int? = null,
        @SerialName("area_limit")
        val areaLimit: Int? = null,
        @SerialName("ban_area_show")
        val banAreaShow: Int? = null,
        @SerialName("can_watch")
        val canWatch: Int? = null,
        @SerialName("copyright")
        val copyright: String? = null,
        @SerialName("forbid_pre")
        val forbidPre: Int? = null,
        @SerialName("freya_white")
        val freyaWhite: Int? = null,
        @SerialName("is_cover_show")
        val isCoverShow: Int? = null,
        @SerialName("is_preview")
        val isPreview: Int? = null,
        @SerialName("only_vip_download")
        val onlyVipDownload: Int? = null,
        @SerialName("resource")
        val resource: String? = null,
        @SerialName("watch_platform")
        val watchPlatform: Int? = null,
    )
    @Serializable
    data class Seasons(
        @SerialName("badge")
        val badge: String? = null,
        @SerialName("badge_info")
        val badgeInfo: BadgeInfo? = null,
        @SerialName("badge_type")
        val badgeType: Int? = null,
        @SerialName("cover")
        val cover: String? = null,
        @SerialName("horizontal_cover_1610")
        val horizontalCover1610: String? = null,
        @SerialName("horizontal_cover_169")
        val horizontalCover169: String? = null,
        @SerialName("media_id")
        val mediaId: Int? = null,
        @SerialName("new_ep")
        val newEp: NewEp? = null,
        @SerialName("season_id")
        val seasonId: Int? = null,
        @SerialName("season_title")
        val seasonTitle: String? = null,
        @SerialName("season_type")
        val seasonType: Int? = null,
        @SerialName("stat")
        val stat: Stat? = null,
    ){
        @Serializable
        data class BadgeInfo(
            @SerialName("bg_color")
            val bgColor: String? = null,
            @SerialName("bg_color_night")
            val bgColorNight: String? = null,
            @SerialName("text")
            val text: String? = null,
        )
        @Serializable
        data class NewEp(
            @SerialName("cover")
            val cover: String? = null,
            @SerialName("id")
            val id: Int? = null,
            @SerialName("index_show")
            val indexShow: String? = null,
        )
        @Serializable
        data class Stat(
            @SerialName("favorites")
            val favorites: Int? = null,
            @SerialName("series_follow")
            val seriesFollow: Int? = null,
            @SerialName("views")
            val views: Int? = null,
        )
    }
    @Serializable
    data class Section(
        @SerialName("attr")
        val attr: Int? = null,
        @SerialName("episode_id")
        val episodeId: Int? = null,
        @SerialName("episode_ids")
        val episodeIds: List<Int>? = null,
        @SerialName("episodes")
        val episodes: List<Episodes>? = null,
        @SerialName("id")
        val id: Int? = null,
        @SerialName("title")
        val title: String? = null,
        @SerialName("type")
        val type: Int? = null,
    ){
        @Serializable
        data class Episodes(
            @SerialName("aid")
            val aid: Int? = null,
            @SerialName("badge")
            val badge: String? = null,
            @SerialName("badge_info")
            val badgeInfo: BadgeInfo? = null,
            @SerialName("badge_type")
            val badgeType: Int? = null,
            @SerialName("bvid")
            val bvid: String? = null,
            @SerialName("cid")
            val cid: Int? = null,
            @SerialName("cover")
            val cover: String? = null,
            @SerialName("dimension")
            val dimension: Dimension? = null,
            @SerialName("duration")
            val duration: Int? = null,
            @SerialName("from")
            val from: String? = null,
            @SerialName("id")
            val id: Int? = null,
            @SerialName("is_view_hide")
            val isViewHide: Boolean? = null,
            @SerialName("link")
            val link: String? = null,
            @SerialName("long_title")
            val longTitle: String? = null,
            @SerialName("pub_time")
            val pubTime: Int? = null,
            @SerialName("pv")
            val pv: Int? = null,
            @SerialName("release_date")
            val releaseDate: String? = null,
            @SerialName("rights")
            val rights: Rights? = null,
            @SerialName("share_copy")
            val shareCopy: String? = null,
            @SerialName("share_url")
            val shareUrl: String? = null,
            @SerialName("short_link")
            val shortLink: String? = null,
            @SerialName("stat")
            val stat: Stat? = null,
            @SerialName("status")
            val status: Int? = null,
            @SerialName("subtitle")
            val subtitle: String? = null,
            @SerialName("title")
            val title: String? = null,
            @SerialName("vid")
            val vid: String? = null,
        ){
            @Serializable
            data class BadgeInfo(
                @SerialName("bg_color")
                val bgColor: String? = null,
                @SerialName("bg_color_night")
                val bgColorNight: String? = null,
                @SerialName("text")
                val text: String? = null,
            )
            @Serializable
            data class Dimension(
                @SerialName("height")
                val height: Int? = null,
                @SerialName("rotate")
                val rotate: Int? = null,
                @SerialName("width")
                val width: Int? = null,
            )
            @Serializable
            data class Rights(
                @SerialName("allow_demand")
                val allowDemand: Int? = null,
                @SerialName("allow_dm")
                val allowDm: Int? = null,
                @SerialName("allow_download")
                val allowDownload: Int? = null,
                @SerialName("area_limit")
                val areaLimit: Int? = null,
            )
            @Serializable
            data class Stat(
                @SerialName("coin")
                val coin: Int? = null,
                @SerialName("danmakus")
                val danmakus: Int? = null,
                @SerialName("likes")
                val likes: Int? = null,
                @SerialName("play")
                val play: Int? = null,
                @SerialName("reply")
                val reply: Int? = null,
            )
        }
    }
    @Serializable
    data class Series(
        @SerialName("series_id")
        val seriesId: Int? = null,
        @SerialName("series_title")
        val seriesTitle: String? = null,
    )
    @Serializable
    data class Show(
        @SerialName("wide_screen")
        val wideScreen: Int? = null,
    )
    @Serializable
    data class Stat(
        @SerialName("coins")
        val coins: Int? = null,
        @SerialName("danmakus")
        val danmakus: Int? = null,
        @SerialName("favorite")
        val favorite: Int? = null,
        @SerialName("favorites")
        val favorites: Int? = null,
        @SerialName("likes")
        val likes: Int? = null,
        @SerialName("reply")
        val reply: Int? = null,
        @SerialName("share")
        val share: Int? = null,
        @SerialName("views")
        val views: Int? = null,
    )
    @Serializable
    data class UpInfo(
        @SerialName("avatar")
        val avatar: String? = null,
        @SerialName("avatar_subscript_url")
        val avatarSubscriptUrl: String? = null,
        @SerialName("follower")
        val follower: Int? = null,
        @SerialName("is_follow")
        val isFollow: Int? = null,
        @SerialName("mid")
        val mid: Int? = null,
        @SerialName("nickname_color")
        val nicknameColor: String? = null,
        @SerialName("pendant")
        val pendant: Pendant? = null,
        @SerialName("theme_type")
        val themeType: Int? = null,
        @SerialName("uname")
        val uname: String? = null,
        @SerialName("verify_type")
        val verifyType: Int? = null,
        @SerialName("vip_label")
        val vipLabel: VipLabel? = null,
        @SerialName("vip_status")
        val vipStatus: Int? = null,
        @SerialName("vip_type")
        val vipType: Int? = null,
    ){
        @Serializable
        data class Pendant(
            @SerialName("image")
            val image: String? = null,
            @SerialName("name")
            val name: String? = null,
            @SerialName("pid")
            val pid: Int? = null,
        )
        @Serializable
        data class VipLabel(
            @SerialName("bg_color")
            val bgColor: String? = null,
            @SerialName("bg_style")
            val bgStyle: Int? = null,
            @SerialName("border_color")
            val borderColor: String? = null,
            @SerialName("text")
            val text: String? = null,
            @SerialName("text_color")
            val textColor: String? = null,
        )
    }
    @Serializable
    data class UserStatus(
        @SerialName("area_limit")
        val areaLimit: Int? = null,
        @SerialName("ban_area_show")
        val banAreaShow: Int? = null,
        @SerialName("follow")
        val follow: Int? = null,
        @SerialName("follow_status")
        val followStatus: Int? = null,
        @SerialName("login")
        val login: Int? = null,
        @SerialName("pay")
        val pay: Int? = null,
        @SerialName("pay_pack_paid")
        val payPackPaid: Int? = null,
        @SerialName("sponsor")
        val sponsor: Int? = null,
    )
}

