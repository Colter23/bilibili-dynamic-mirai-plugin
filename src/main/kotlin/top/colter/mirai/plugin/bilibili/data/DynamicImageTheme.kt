package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import org.jetbrains.skia.Color
import top.colter.mirai.plugin.bilibili.draw.makeRGB


object BiliImageTheme : ReadOnlyPluginConfig("ImageTheme") {

    @ValueDescription("具体的配置文件描述请前往下方链接查看")
    val help: String by value("https://github.com/Colter23/bilibili-dynamic-mirai-plugin")

    @ValueDescription("图片主题")
    val theme: Map<String, Theme> by value(
        mapOf(
            "v3" to Theme(
                "#A0FFFFFF",
                "#FFFFFF",
                "#A0FFFFFF",
                "#FB7299",
                "#313131",
                "#9C9C9C",
                "#666666",
                "#222222",
                "#178BCF",
                Theme.Shadow("#46000000", 6f, 6f, 25f, 0f),
                Theme.Shadow("#1E000000", 5f, 5f, 15f, 0f),
                Theme.BadgeColor("#00CBFF", "#78FFFFFF"),
                Theme.BadgeColor("#FFFFFF", "#48C7F0"),
                Theme.BadgeColor("#FFFFFF", "#FB7299"),
                Theme.BadgeColor("#FFFFFF", "#48C7F0"),
            ),
            "v2" to Theme(
                "#A0FFFFFF",
                "#FFFFFF",
                "#A0FFFFFF",
                "#FB7299",
                "#313131",
                "#9C9C9C",
                "#666666",
                "#222222",
                "#178BCF",
                Theme.Shadow("#46000000", 6f, 6f, 25f, 0f),
                Theme.Shadow("#1E000000", 5f, 5f, 15f, 0f),
                Theme.BadgeColor("#00CBFF", "#78FFFFFF"),
                Theme.BadgeColor("#FFFFFF", "#48C7F0"),
                Theme.BadgeColor("#FFFFFF", "#FB7299"),
                Theme.BadgeColor("#FFFFFF", "#48C7F0"),
            )
        )
    )

}
@Serializable
data class Theme(
    val cardBgColorHex: String,

    val cardOutlineColorHex: String,
    val faceOutlineColorHex: String,

    val nameColorHex: String,
    val titleColorHex: String,
    val subTitleColorHex: String,
    val descColorHex: String,
    val contentColorHex: String,
    val linkColorHex: String,

    val cardShadow: Shadow,
    val smallCardShadow: Shadow,

    val mainLeftBadge: BadgeColor,
    val mainRightBadge: BadgeColor,
    val subLeftBadge: BadgeColor,
    val subRightBadge: BadgeColor,

    ) {

    @Serializable
    data class Shadow(
        val shadowColorHex: String,
        val offsetX: Float,
        val offsetY: Float,
        val blur: Float,
        val spread: Float = 0f,
    ) {
        val shadowColor: Int = Color.makeRGB(shadowColorHex)
    }

    @Serializable
    data class BadgeColor(
        val fontColorHex: String,
        val bgColorHex: String,
    ){
        val fontColor: Int = Color.makeRGB(fontColorHex)
        val bgColor: Int = Color.makeRGB(bgColorHex)
    }

    val cardBgColor: Int = Color.makeRGB(cardBgColorHex)

    val cardOutlineColor: Int = Color.makeRGB(cardOutlineColorHex)
    val faceOutlineColor: Int = Color.makeRGB(faceOutlineColorHex)

    val nameColor: Int = Color.makeRGB(nameColorHex)
    val titleColor: Int = Color.makeRGB(titleColorHex)
    val subTitleColor: Int = Color.makeRGB(subTitleColorHex)
    val descColor: Int = Color.makeRGB(descColorHex)
    val contentColor: Int = Color.makeRGB(contentColorHex)
    val linkColor: Int = Color.makeRGB(linkColorHex)

}