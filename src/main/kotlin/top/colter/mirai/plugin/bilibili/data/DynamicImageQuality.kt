package top.colter.mirai.plugin.bilibili.data

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

                badgeHeight = 36,
                badgePadding = 5,
                badgeArc = 5f,

                lineHeight = 30f,
                lineSpace = 8,

                contentSpace = 10,

                drawSpace = 10,
                smallCardHeight = 160,
                additionalCardHeight = 90,

                cardOutlineWidth = 2f,
                drawOutlineWidth = 2f,

                //qrCodeWidth = 90,
                fanCardHeight = 90f,

                nameFontSize = 30f,
                titleFontSize = 26f,
                subTitleFontSize = 22f,
                descFontSize = 20f,
                contentFontSize = 26f,

            ),
            "1000w" to Quality(
                imageWidth = 1000,
                cardMargin = 30,
                cardPadding = 30,
                cardArc = 15f,
                faceSize = 80f,
                noPendantFaceInflate = 10f,
                pendantSize = 140f,
                verifyIconSize = 30f,

                badgeHeight = 45,
                badgePadding = 8,
                badgeArc = 8f,

                lineHeight = 35f,
                lineSpace = 11,

                contentSpace = 12,

                //qrCodeWidth = 115,
                fanCardHeight = 115f,

                drawSpace = 15,
                smallCardHeight = 200,
                additionalCardHeight = 130,

                cardOutlineWidth = 3f,
                drawOutlineWidth = 3f,

                nameFontSize = 36f,
                titleFontSize = 32f,
                subTitleFontSize = 28f,
                descFontSize = 26f,
                contentFontSize = 32f,

            ),
            "1200w" to Quality(
                imageWidth = 1200,
                cardMargin = 40,
                cardPadding = 40,
                cardArc = 20f,
                faceSize = 95f,
                noPendantFaceInflate = 13f,
                pendantSize = 170f,
                verifyIconSize = 40f,

                badgeHeight = 55,
                badgePadding = 11,
                badgeArc = 11f,

                lineHeight = 40f,
                lineSpace = 14,

                contentSpace = 17,

                fanCardHeight = 140f,

                drawSpace = 20,
                smallCardHeight = 240,
                additionalCardHeight = 160,

                cardOutlineWidth = 4f,
                drawOutlineWidth = 4f,

                nameFontSize = 42f,
                titleFontSize = 38f,
                subTitleFontSize = 34f,
                descFontSize = 32f,
                contentFontSize = 38f,

            ),
            "1500w" to Quality(
                imageWidth = 1500,
                cardMargin = 50,
                cardPadding = 50,
                cardArc = 30f,
                faceSize = 100f,
                noPendantFaceInflate = 18f,
                pendantSize = 190f,
                verifyIconSize = 50f,

                badgeHeight = 72,
                badgePadding = 15,
                badgeArc = 16f,

                lineHeight = 48f,
                lineSpace = 20,

                contentSpace = 20,

                fanCardHeight = 150f,

                drawSpace = 25,
                smallCardHeight = 300,
                additionalCardHeight = 205,

                cardOutlineWidth = 6f,
                drawOutlineWidth = 6f,

                nameFontSize = 51f,
                titleFontSize = 46f,
                subTitleFontSize = 43f,
                descFontSize = 40f,
                contentFontSize = 47f,
            ),
        )
    )

}

@Serializable
data class Quality(
    val imageWidth: Int,
    val cardMargin: Int,
    val cardPadding: Int,
    val cardArc: Float,

    val faceSize: Float,
    val noPendantFaceInflate: Float,
    val pendantSize: Float,
    val verifyIconSize: Float,

    var badgeHeight: Int,
    val badgePadding: Int,
    val badgeArc: Float,

    val lineHeight: Float,
    val lineSpace: Int,

    val contentSpace: Int,

    val fanCardHeight: Float,

    val drawSpace: Int,

    val smallCardHeight: Int,
    val additionalCardHeight: Int,

    val cardOutlineWidth: Float,
    val drawOutlineWidth: Float,

    //val qrCodeWidth: Int,
    val nameFontSize: Float,
    val titleFontSize: Float,
    val subTitleFontSize: Float,
    val descFontSize: Float,
    val contentFontSize: Float,
)