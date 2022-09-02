package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ArticleDetail(
    @SerialName("id")
    val aid: Long,
    @SerialName("title")
    val title: String,
    @SerialName("summary")
    val summary: String,
    @SerialName("author")
    val author: ModuleAuthor,
    @SerialName("image_urls")
    val covers: List<String>,
    @SerialName("publish_time")
    val time: Long,
    @SerialName("stats")
    val stats: Stats,
    @SerialName("words")
    val words: Int,
): BiliDetail

@Serializable
data class ArticleInfo(
    @SerialName("like")
    val like: Int? = null,
    @SerialName("attention")
    val attention: Boolean? = null,
    @SerialName("favorite")
    val favorite: Boolean? = null,
    @SerialName("coin")
    val coin: Int? = null,
    @SerialName("stats")
    val stats: Stats? = null,
    @SerialName("title")
    val title: String,
    @SerialName("banner_url")
    val bannerUrl: String? = null,
    @SerialName("mid")
    val mid: Long,
    @SerialName("author_name")
    val authorName: String,
    @SerialName("is_author")
    val isAuthor: Boolean? = null,
    @SerialName("image_urls")
    val imageUrls: List<String>? = null,
    @SerialName("origin_image_urls")
    val originImageUrls: List<String>? = null,
    @SerialName("shareable")
    val shareable: Boolean? = null,
    @SerialName("show_later_watch")
    val showLaterWatch: Boolean? = null,
    @SerialName("show_small_window")
    val showSmallWindow: Boolean? = null,
    @SerialName("in_list")
    val inList: Boolean? = null,
    @SerialName("pre")
    val pre: Int? = null,
    @SerialName("next")
    val next: Int? = null,
    @SerialName("type")
    val type: Int? = null,
    @SerialName("video_url")
    val videoUrl: String? = null,
    @SerialName("location")
    val location: String? = null,
    @SerialName("disable_share")
    val disableShare: Boolean? = null,
): BiliDetail
