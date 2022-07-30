package top.colter.mirai.plugin.bilibili.draw

import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.svg.SVGDOM
import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.BiliData
import top.colter.mirai.plugin.bilibili.data.LiveInfo
import top.colter.mirai.plugin.bilibili.utils.*
import kotlin.math.abs


suspend fun LiveInfo.makeDrawLive(colors: List<Int>): String {
    val live = drawLive()
    val img = makeCardBg(live.height, colors) {
        it.drawImage(live, 0f, 0f)
    }
    return cacheImage(img, "$uid/${liveTime.formatTime("yyyyMMddHHmmss")}.png", CacheType.DRAW_LIVE)
}

suspend fun LiveInfo.drawLive(): Image {
    val margin = quality.cardMargin * 2

    val avatar = drawAvatar()
    val cover = getOrDownloadImageDefault(cover, CacheType.IMAGES)

    val height = (avatar.height + quality.contentSpace + cover.height * cardRect.width / cover.width).toInt()

    val footerTemplate = BiliConfig.templateConfig.footer.liveFooter
    val footerParagraph = if (footerTemplate.isNotBlank()) {
        val footer = footerTemplate
            .replace("{name}", uname)
            .replace("{uid}", uid.toString())
            .replace("{id}", roomId.toString())
            .replace("{time}", liveTime.formatTime)
            .replace("{type}", "直播")
        ParagraphBuilder(footerParagraphStyle, FontUtils.fonts).addText(footer).build().layout(cardRect.width)
    } else null

    return Surface.makeRasterN32Premul(
        (cardRect.width + margin).toInt(),
        height + quality.badgeHeight + margin + (footerParagraph?.height?.toInt() ?: 0)
    ).apply {
        canvas.apply {

            val rrect = RRect.makeComplexXYWH(
                margin / 2f,
                quality.badgeHeight + margin / 2f,
                cardRect.width,
                height.toFloat(),
                cardBadgeArc
            )

            drawRectShadowAntiAlias(rrect.inflate(1f), theme.cardShadow)

            if (BiliConfig.imageConfig.badgeEnable.left) {
                val svg = SVGDOM(Data.makeFromBytes(loadResourceBytes("icon/LIVE.svg")))
                drawBadge(
                    "直播",
                    font,
                    theme.mainLeftBadge.fontColor,
                    theme.mainLeftBadge.bgColor,
                    rrect,
                    Position.TOP_LEFT,
                    svg.makeImage(quality.contentFontSize, quality.contentFontSize)
                )
            }
            if (BiliConfig.imageConfig.badgeEnable.right) {
                drawBadge(roomId.toString(), font, Color.WHITE, Color.makeRGB(72, 199, 240), rrect, Position.TOP_RIGHT)
            }

            drawCard(rrect)

            var top = quality.cardMargin + quality.badgeHeight.toFloat()

            drawScaleWidthImage(avatar, cardRect.width, quality.cardMargin.toFloat(), top)
            top += avatar.height + quality.contentSpace

            val dst = RRect.makeXYWH(
                quality.cardMargin.toFloat(),
                top,
                cardRect.width - quality.cardOutlineWidth / 2,
                (cardRect.width * cover.height / cover.width)  - quality.cardOutlineWidth / 2,
                quality.cardArc
            )
            drawImageRRect(cover, dst)

            footerParagraph?.paint(this, cardRect.left, rrect.bottom + quality.cardMargin / 2)

        }
    }.makeImageSnapshot()
}

suspend fun LiveInfo.drawAvatar(): Image {
    return Surface.makeRasterN32Premul(
        cardRect.width.toInt(),
        (quality.faceSize + quality.cardPadding * 2f).toInt()
    ).apply surface@{
        canvas.apply {
            drawAvatar(face, null, null, quality.faceSize, quality.verifyIconSize)

            val paragraphStyle = ParagraphStyle().apply {
                maxLinesCount = 1
                ellipsis = "..."
                alignment = Alignment.LEFT
                textStyle = titleTextStyle.apply {
                    fontSize = quality.nameFontSize
                }
            }

            val w = cardContentRect.width - quality.pendantSize -
                if (BiliConfig.imageConfig.cardOrnament == "QrCode" ) quality.ornamentHeight else 0f

            val titleParagraph =
                ParagraphBuilder(paragraphStyle, FontUtils.fonts).addText(title).build()
                    .layout(w)
            paragraphStyle.apply {
                textStyle = descTextStyle.apply {
                    fontSize = quality.subTitleFontSize
                }
            }
            val timeParagraph =
                ParagraphBuilder(paragraphStyle, FontUtils.fonts).addText("$uname  ${liveTime.formatTime}").build()
                    .layout(w)

            val x = quality.faceSize + quality.cardPadding * 3f
            val space = (quality.pendantSize - quality.nameFontSize - quality.subTitleFontSize) / 3
            var y =  space * 1.25f

            titleParagraph.paint(this, x, y)

            y += quality.nameFontSize + space * 0.5f
            timeParagraph.paint(this, x, y)

            val color = BiliData.dynamic[uid]?.color ?: BiliConfig.imageConfig.defaultColor
            val colors = color.split(";", "；").map { Color.makeRGB(it.trim()) }.first()
            drawLiveOrnament("https://live.bilibili.com/$roomId", colors, area)
        }
    }.makeImageSnapshot()
}

fun Canvas.drawLiveOrnament(link: String?, qrCodeColor: Int?, label: String?) {
    when (BiliConfig.imageConfig.cardOrnament) {
        "QrCode" -> {
            val qrCodeImg = qrCode(link!!, quality.ornamentHeight.toInt(), qrCodeColor!!)
            val y = ((quality.faceSize - qrCodeImg.height + quality.contentSpace) / 2)
            val tarFRect = Rect.makeXYWH(
                cardRect.width - qrCodeImg.width - abs(y),
                y + quality.cardPadding,
                qrCodeImg.width.toFloat(),
                qrCodeImg.height.toFloat()
            )

            val srcFRect = Rect.makeXYWH(0f, 0f, qrCodeImg.width.toFloat(), qrCodeImg.height.toFloat())
            drawImageRect(
                qrCodeImg,
                srcFRect,
                tarFRect,
                FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST),
                Paint(),
                true
            )
        }
        "None" -> {}
        else -> {
            //val labelTextLine = TextLine.make(label, font.makeWithSize(quality.subTitleFontSize))
            //val y =
            //    ((quality.faceSize - quality.subTitleFontSize - quality.badgePadding * 2 + quality.contentSpace) / 2)
            //drawLabelCard(
            //    labelTextLine,
            //    cardContentRect.right - labelTextLine.width - quality.badgePadding * 4 - abs(y),
            //    y + quality.cardPadding,
            //    Paint().apply {
            //        color = theme.subLeftBadge.fontColor
            //    },
            //    Paint().apply {
            //        color = theme.subLeftBadge.bgColor
            //    }
            //)
        }
    }
}