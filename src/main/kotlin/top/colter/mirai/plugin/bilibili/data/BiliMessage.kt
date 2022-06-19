package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.Serializable


@Serializable
sealed interface BiliMessage {
    val uid: Long
    val uname: String
    val time: String
    val timestamp: Int
    val drawPath: String?
    val contact: String?
}

@Serializable
data class DynamicMessage(
    val did: String,
    override val uid: Long,
    override val uname: String,
    val type: DynamicType,
    override val time: String,
    override val timestamp: Int,
    val content: String,
    val images: List<String>?,
    val links: List<Link>?,
    override val drawPath: String? = null,
    override val contact: String? = null
) : BiliMessage {
    @Serializable
    data class Link(
        val tag: String,
        val value: String,
    )
}

@Serializable
data class LiveMessage(
    val rid: Long,
    override val uid: Long,
    override val uname: String,
    override val time: String,
    override val timestamp: Int,
    val title: String,
    val cover: String,
    val area: String,
    val link: String,
    override val drawPath: String? = null,
    override val contact: String? = null
) : BiliMessage