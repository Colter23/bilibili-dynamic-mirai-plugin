package top.colter.mirai.plugin.bilibili.draw

import kotlinx.serialization.Serializable
import org.jetbrains.skia.Color

@Serializable
data class Theme(
    val cardBgColorHex: String = "",

    //val mainBadgeBgColorHex: String = "",
    //val subBadgeBgColorHex: String = "",

    val cardOutlineColorHex: String = "",

    val cardShadow: Shadow,
    val smallCardShadow: Shadow,

    val nameColorHex: String = "",
    val titleColorHex: String = "",
    val subTitleColorHex: String = "",
    val descColorHex: String = "",
    val contentColorHex: String = "",
    val linkColorHex: String = "",


    ) {
    @Serializable
    data class Shadow(
        val shadowColorHex: String = "",
        val offsetX: Float = 0f,
        val offsetY: Float = 0f,
        val blur: Float = 0f,
        val spread: Float = 0f,
    ) {
        val shadowColor: Int = Color.makeRGB(shadowColorHex)
    }

    val cardBgColor: Int = Color.makeRGB(cardBgColorHex)

    val cardOutlineColor: Int = Color.makeRGB(cardOutlineColorHex)

    val nameColor: Int = Color.makeRGB(nameColorHex)
    val titleColor: Int = Color.makeRGB(titleColorHex)
    val subTitleColor: Int = Color.makeRGB(subTitleColorHex)
    val descColor: Int = Color.makeRGB(descColorHex)
    val contentColor: Int = Color.makeRGB(contentColorHex)
    val linkColor: Int = Color.makeRGB(linkColorHex)

    companion object {
        val v3 = Theme(
            "#A0FFFFFF",
            "#FFFFFF",
            Shadow("#46000000", 6f, 6f, 25f, 0f),
            Shadow("#1E000000", 5f, 5f, 15f, 0f),
            "#FB7299",
            "#313131",
            "#9C9C9C",
            "#666666",
            "#222222",
            "#178BCF"
        )
    }
}