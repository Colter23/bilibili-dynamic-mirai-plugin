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
    val cardShadow: Shadow,

    val nameColor: Int = 0,
    val titleColor: Int = 0,
    val subTitleColor: Int = 0,
    val contentColor: Int = 0,
    val linkColor: Int = 0,


    ) {
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