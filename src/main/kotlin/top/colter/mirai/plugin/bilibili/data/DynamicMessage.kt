package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.skia.Image


@Serializable
data class DynamicMessage(
    val did: String,
    val content: String,
    val images: List<String>?,
    val links: List<Link>?,
    @Transient
    val draw: Image? = null
){
    @Serializable
    data class Link(
        val tag: String,
        val value: String,
    )
}