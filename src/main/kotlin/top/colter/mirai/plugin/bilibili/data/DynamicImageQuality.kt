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

                //qrCodeWidth = 115,
                fanCardHeight = 115f,

                drawSpace = 15,
                smallCardHeight = 200,
                additionalCardHeight = 130,

                cardOutlineWidth = 2f,
                drawOutlineWidth = 2f,

                nameFontSize = 36f,
                titleFontSize = 32f,
                subTitleFontSize = 28f,
                descFontSize = 24f,
                contentFontSize = 32f,

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

                cardOutlineWidth = 2f,
                drawOutlineWidth = 2f,

                nameFontSize = 36f,
                titleFontSize = 32f,
                subTitleFontSize = 28f,
                descFontSize = 24f,
                contentFontSize = 32f,

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

                cardOutlineWidth = 2f,
                drawOutlineWidth = 2f,

                nameFontSize = 36f,
                titleFontSize = 32f,
                subTitleFontSize = 28f,
                descFontSize = 24f,
                contentFontSize = 32f,

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