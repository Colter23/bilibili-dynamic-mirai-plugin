package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import org.jetbrains.skia.Color
import top.colter.mirai.plugin.bilibili.draw.makeRGB


object BiliImageTheme : ReadOnlyPluginConfig("ImageTheme") {

    @ValueDescription("具体的配置文件描述请前往下方链接查看")
    val help: String by value("https://github.com/Colter23/bilibili-dynamic-mirai-plugin#ImageTheme.yml")

    @ValueDescription("是否启用自定义数据\n启用后配置文件中的主题配置将失效")
    val customOverload: Boolean by value(false)

    @ValueDescription("自定义图片主题\n默认数据为v3主题数据")
    val customTheme: Theme by value(theme["v3"]!!)

    @ValueDescription("图片主题")
    val theme: Map<String, Theme>
        get() = mapOf(
            "v3" to Theme(
                "#C8FFFFFF",
                "#FFFFFF",
                "#A0FFFFFF",
                "#FFFFFF",
                "#FB7299",
                "#313131",
                "#9C9C9C",
                "#666666",
                "#222222",
                "#178BCF",
                Theme.Shadow("#46000000", 6f, 6f, 25f, 0f),
                Theme.Shadow("#1E000000", 5f, 5f, 15f, 0f),
                Theme.BadgeColor("#00CBFF", "#A0FFFFFF"),
                Theme.BadgeColor("#FFFFFF", "#48C7F0"),
                Theme.BadgeColor("#FFFFFF", "#FB7299"),
                Theme.BadgeColor("#FFFFFF", "#48C7F0"),
            ),
            "v3RainbowOutline" to Theme(
                "#A0FFFFFF",
                "#ff0000;#ff00ff;#0000ff;#00ffff;#00ff00;#ffff00;#ff0000",
                "#A0FFFFFF",
                "#FFFFFF",
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
                "#C8FFFFFF",
                "#FFFFFF",
                "#A0FFFFFF",
                "#FFFFFF",
                "#FB7299",
                "#313131",
                "#9C9C9C",
                "#666666",
                "#222222",
                "#178BCF",
                Theme.Shadow("#00000000", 0f, 0f, 0f, 0f),
                Theme.Shadow("#00000000", 0f, 0f, 0f, 0f),
                Theme.BadgeColor("#00CBFF", "#78FFFFFF"),
                Theme.BadgeColor("#FFFFFF", "#48C7F0"),
                Theme.BadgeColor("#FFFFFF", "#FB7299"),
                Theme.BadgeColor("#FFFFFF", "#48C7F0"),
            )
        )
}

@Serializable
data class Theme(
    val cardBgColorHex: String,

    val cardOutlineColorHex: String,
    val faceOutlineColorHex: String,
    val drawOutlineColorHex: String,

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
        val shadowColor: Int get() = Color.makeRGB(shadowColorHex)
    }

    @Serializable
    data class BadgeColor(
        val fontColorHex: String,
        val bgColorHex: String,
    ) {

        val fontColor: Int get() = Color.makeRGB(fontColorHex)
        val bgColor: Int get() = Color.makeRGB(bgColorHex)
    }

    val cardBgColor: Int get() = Color.makeRGB(cardBgColorHex)

    //val cardOutlineColor: Int get() = Color.makeRGB(cardOutlineColorHex)
    val cardOutlineColors: IntArray get() = cardOutlineColorHex.split(";").map { Color.makeRGB(it) }.toIntArray()
    val faceOutlineColor: Int get() = Color.makeRGB(faceOutlineColorHex)
    val drawOutlineColor: Int get() = Color.makeRGB(drawOutlineColorHex)

    val nameColor: Int get() = Color.makeRGB(nameColorHex)
    val titleColor: Int get() = Color.makeRGB(titleColorHex)
    val subTitleColor: Int get() = Color.makeRGB(subTitleColorHex)
    val descColor: Int get() = Color.makeRGB(descColorHex)
    val contentColor: Int get() = Color.makeRGB(contentColorHex)
    val linkColor: Int get() = Color.makeRGB(linkColorHex)

}