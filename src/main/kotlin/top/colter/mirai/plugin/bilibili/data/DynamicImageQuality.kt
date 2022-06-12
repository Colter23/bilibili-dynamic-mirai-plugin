package top.colter.mirai.plugin.bilibili.draw

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object BiliImageQuality : ReadOnlyPluginConfig("ImageQuality") {

    @ValueDescription("具体的配置文件描述请前往下方链接查看")
    val help: String by value("https://github.com/Colter23/bilibili-dynamic-mirai-plugin")

    @ValueDescription("图片质量")
    val quality: Map<String, Quality> by value(
        mapOf(
            "800w" to Quality(
                imageWidth = 800,
                cardMargin = 20,
                cardPadding = 20,
                cardArc = 10f,
                faceSize = 64f,
                noPendantFaceInflate = 5f,
                pendantSize = 112f,
                verifyIconSize = 20f,

                // 36   5
                badgeHeight = 36,
                badgePadding = 5,
                badgeArc = 5f,

                lineHeight = 30f,
                lineSpace = 8,

                contentSpace = 10,

                drawSpace = 10,
                smallCardHeight = 160,
                additionalCardHeight = 90,

                //mainFontSize = 22f,
                //subFontSize = 18f,
                //emojiSize = 30f,
                //iconSize = 26f,
                fanCardHeight = 90f,

                nameFontSize = 30f,
                titleFontSize = 26f,
                subTitleFontSize = 22f,
                descFontSize = 20f,
                contentFontSize = 26f,

                emojiSize = 34f,
                iconSize = 30f,
            ),
            "1000w" to Quality(
                imageWidth = 1000,
                cardMargin = 30,
                cardPadding = 30,
                cardArc = 15f,
                // 80
                faceSize = 80f,
                noPendantFaceInflate = 10f,
                // 120
                pendantSize = 140f,
                verifyIconSize = 30f,

                badgeHeight = 45,
                badgePadding = 8,
                badgeArc = 8f,

                lineHeight = 35f,
                lineSpace = 8,

                contentSpace = 15,

                fanCardHeight = 115f,

                drawSpace = 15,
                smallCardHeight = 200,
                additionalCardHeight = 130,

                nameFontSize = 36f,
                titleFontSize = 32f,
                subTitleFontSize = 28f,
                descFontSize = 24f,
                contentFontSize = 32f,

                emojiSize = 36f,
                iconSize = 31f,
            ),
            "1200w" to Quality(
                imageWidth = 1000,
                cardMargin = 30,
                cardPadding = 30,
                cardArc = 15f,
                // 80
                faceSize = 80f,
                noPendantFaceInflate = 10f,
                // 120
                pendantSize = 140f,
                verifyIconSize = 30f,

                badgeHeight = 45,
                badgePadding = 8,
                badgeArc = 8f,

                lineHeight = 35f,
                lineSpace = 8,

                contentSpace = 15,

                fanCardHeight = 115f,

                drawSpace = 15,
                smallCardHeight = 200,
                additionalCardHeight = 130,

                nameFontSize = 36f,
                titleFontSize = 32f,
                subTitleFontSize = 28f,
                descFontSize = 24f,
                contentFontSize = 32f,

                emojiSize = 36f,
                iconSize = 31f,
            ),
            "1500w" to Quality(
                imageWidth = 1000,
                cardMargin = 30,
                cardPadding = 30,
                cardArc = 15f,
                // 80
                faceSize = 80f,
                noPendantFaceInflate = 10f,
                // 120
                pendantSize = 140f,
                verifyIconSize = 30f,

                badgeHeight = 45,
                badgePadding = 8,
                badgeArc = 8f,

                lineHeight = 35f,
                lineSpace = 8,

                contentSpace = 15,

                fanCardHeight = 115f,

                drawSpace = 15,
                smallCardHeight = 200,
                additionalCardHeight = 130,

                nameFontSize = 36f,
                titleFontSize = 32f,
                subTitleFontSize = 28f,
                descFontSize = 24f,
                contentFontSize = 32f,

                emojiSize = 36f,
                iconSize = 31f,
            ),
        )
    )

}

@Serializable
data class Quality(
    val imageWidth: Int = 0,
    val cardMargin: Int = 0,
    val cardPadding: Int = 0,
    val cardArc: Float = 0f,

    val faceSize: Float = 0f,
    val noPendantFaceInflate: Float = 0f,
    val pendantSize: Float = 0f,
    val verifyIconSize: Float = 0f,

    var badgeHeight: Int = 0,
    val badgePadding: Int = 0,
    val badgeArc: Float = 0f,

    val lineHeight: Float = 0f,
    val lineSpace: Int = 0,

    val contentSpace: Int = 0,

    val fanCardHeight: Float = 0f,

    val drawSpace: Int = 0,

    val smallCardHeight: Int = 0,
    val additionalCardHeight: Int = 0,

    val nameFontSize: Float = 0f,
    val titleFontSize: Float = 0f,
    val subTitleFontSize: Float = 0f,
    val descFontSize: Float = 0f,
    val contentFontSize: Float = 0f,

    val emojiSize: Float = 0f,
    val iconSize: Float = 0f
)