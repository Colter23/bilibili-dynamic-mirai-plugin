package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BiliSearch(
    @SerialName("seid")
    val seid: Float? = null,
    @SerialName("page")
    val page: Int? = null,
    @SerialName("pagesize")
    val pagesize: Int? = null,
    @SerialName("numResults")
    val numResults: Int? = null,
    @SerialName("numPages")
    val numPages: Int? = null,
    @SerialName("suggest_keyword")
    val suggestKeyword: String? = null,
    @SerialName("rqt_type")
    val rqtType: String? = null,
    @SerialName("cost_time")
    val costTime: CostTime? = null,
    @SerialName("exp_list")
    val expList: String? = null,
    @SerialName("egg_hit")
    val eggHit: Int? = null,
    @SerialName("result")
    val result: List<SearchResult>? = null,
    @SerialName("show_column")
    val showColumn: Int? = null,
    @SerialName("in_black_key")
    val inBlackKey: Int? = null,
    @SerialName("in_white_key")
    val inWhiteKey: Int? = null,
) {
    @Serializable
    data class CostTime(
        @SerialName("params_check")
        val paramsCheck: Float? = null,
        @SerialName("get_upuser_live_status")
        val getUpuserLiveStatus: Float? = null,
        @SerialName("illegal_handler")
        val illegalHandler: Float? = null,
        @SerialName("as_response_format")
        val asResponseFormat: Float? = null,
        @SerialName("as_request")
        val asRequest: Float? = null,
        @SerialName("save_cache")
        val saveCache: Float? = null,
        @SerialName("deserialize_response")
        val deserializeResponse: Float? = null,
        @SerialName("as_request_format")
        val asRequestFormat: Float? = null,
        @SerialName("total")
        val total: Float? = null,
        @SerialName("main_handler")
        val mainHandler: Float? = null,
    )

    @Serializable
    data class SearchResult(
        @SerialName("type")
        val type: String? = null,
        @SerialName("mid")
        val mid: Int? = null,
        @SerialName("uname")
        val uname: String? = null,
        @SerialName("usign")
        val usign: String? = null,
        @SerialName("fans")
        val fans: Int? = null,
        @SerialName("videos")
        val videos: Int? = null,
        @SerialName("upic")
        val upic: String? = null,
        @SerialName("face_nft")
        val faceNft: Int? = null,
        @SerialName("face_nft_type")
        val faceNftType: Int? = null,
        @SerialName("verify_info")
        val verifyInfo: String? = null,
        @SerialName("level")
        val level: Int? = null,
        @SerialName("gender")
        val gender: Int? = null,
        @SerialName("is_upuser")
        val isUpuser: Int? = null,
        @SerialName("is_live")
        val isLive: Int? = null,
        @SerialName("room_id")
        val roomId: Int? = null,
        @SerialName("res")
        val res: List<Res>? = null,
        @SerialName("official_verify")
        val officialVerify: OfficialVerify? = null,
        @SerialName("hit_columns")
        val hitColumns: List<String>? = null,
    ) {
        @Serializable
        data class Res(
            @SerialName("aid")
            val aid: Int? = null,
            @SerialName("bvid")
            val bvid: String? = null,
            @SerialName("title")
            val title: String? = null,
            @SerialName("pubdate")
            val pubdate: Int? = null,
            @SerialName("arcurl")
            val arcurl: String? = null,
            @SerialName("pic")
            val pic: String? = null,
            @SerialName("play")
            val play: Int? = null,
            @SerialName("dm")
            val dm: Int? = null,
            @SerialName("coin")
            val coin: Int? = null,
            @SerialName("fav")
            val fav: Int? = null,
            @SerialName("desc")
            val desc: String? = null,
            @SerialName("duration")
            val duration: String? = null,
            @SerialName("is_pay")
            val isPay: Int? = null,
            @SerialName("is_union_video")
            val isUnionVideo: Int? = null,
        )

        @Serializable
        data class OfficialVerify(
            @SerialName("type")
            val type: Int? = null,
            @SerialName("desc")
            val desc: String? = null,
        )
    }

}