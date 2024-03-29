package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.Serializable


@Serializable
sealed interface BiliMessage {
    val mid: Long
    val name: String
    val time: String
    val timestamp: Int
    val drawPath: String?
    val contact: String?
}

@Serializable
data class DynamicMessage(
    val did: String,
    override val mid: Long,
    override val name: String,
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
    override val mid: Long,
    override val name: String,
    override val time: String,
    override val timestamp: Int,
    val title: String,
    val cover: String,
    val area: String,
    val link: String,
    override val drawPath: String? = null,
    override val contact: String? = null
) : BiliMessage

@Serializable
data class LiveCloseMessage(
    val rid: Long,
    override val mid: Long,
    override val name: String,
    override val time: String,
    override val timestamp: Int,
    val endTime: String,
    val duration: String,
    val title: String,
    val area: String,
    val link: String,
    override val drawPath: String? = null,
    override val contact: String? = null
) : BiliMessage