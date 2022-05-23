package top.colter.mirai.plugin.bilibili.draw

import kotlinx.serialization.Serializable

@Serializable
data class DynamicImageTheme(
    val cardBgColor: Int = 0,
    val cardBgAlpha: Int = 0,

    val mainBadgeBgColor: Int = 0,
    val mainBadgeBgAlpha: Int = 0,

    val subBadgeBgColor: Int = 0,
    val subBadgeBgAlpha: Int = 0,

    val smallCardShadow: Shadow,
    val cardShadow: Shadow

){
    @Serializable
    data class Shadow(
        val shadowColor: Int = 0,
        val shadowColorAlpha: Int = 0,
        val offsetX: Int = 0,
        val offsetY: Int = 0,
        val blur: Int = 0,
        val spread: Int = 0,
    )
}