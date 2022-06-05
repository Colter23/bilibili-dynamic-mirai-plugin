package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.Serializable


@Serializable
data class DynamicMessage(
    val did: String,
    val uid: Long,
    val uname: String,
    val type: String,
    val time: String,
    val timestamp: Int,
    val content: String,
    val images: List<String>?,
    val links: List<Link>?,
    val drawPath: String? = null,
    val contact: String? = null
) {
    @Serializable
    data class Link(
        val tag: String,
        val value: String,
    )
}