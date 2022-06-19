package top.colter.mirai.plugin.bilibili.draw

import org.jetbrains.skia.*
import org.jetbrains.skia.svg.SVGDOM
import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.data.LiveInfo
import top.colter.mirai.plugin.bilibili.utils.*


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
    val cover = getOrDownloadImage(cover, CacheType.IMAGES)

    val height = (avatar.height + quality.contentSpace + cover.height * cardRect.width / cover.width).toInt()

    return Surface.makeRasterN32Premul(
        (cardRect.width + margin).toInt(),
        height + quality.badgeHeight + margin
    ).apply {
        canvas.apply {

            val rrect = RRect.makeComplexXYWH(
                margin / 2f,
                quality.badgeHeight + margin / 2f,
                cardRect.width,
                height.toFloat(),
                cardBadgeArc
            )

            drawCard(rrect)
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

            var top = quality.cardMargin + quality.badgeHeight.toFloat()

            drawScaleWidthImage(avatar, cardRect.width, quality.cardMargin.toFloat(), top)
            top += avatar.height + quality.contentSpace

            val dst = RRect.makeComplexXYWH(
                quality.cardMargin.toFloat(),
                top,
                cardRect.width,
                cardRect.width * cover.height / cover.width,
                cardBadgeArc
            )
            drawImageRRect(cover, dst)

        }
    }.makeImageSnapshot()
}

suspend fun LiveInfo.drawAvatar(): Image {
    return Surface.makeRasterN32Premul(
        quality.imageWidth - quality.cardMargin * 2,
        (quality.faceSize + quality.cardPadding * 2f).toInt()
    ).apply surface@{
        canvas.apply {
            drawAvatar(face, null, null, quality.faceSize, quality.verifyIconSize)

            val textLineTitle = TextLine.make(title, font.makeWithSize(quality.nameFontSize))
            val textLineTime =
                TextLine.make("$uname   ${liveTime.formatTime}", font.makeWithSize(quality.subTitleFontSize))

            var x = quality.faceSize + quality.cardPadding * 3f
            var y =
                ((quality.faceSize - (quality.nameFontSize + textLineTime.height)) / 2) + quality.nameFontSize + (quality.cardPadding * 1.2f)

            drawTextLine(textLineTitle, x, y, Paint().apply { color = theme.titleColor })

            y += textLineTime.height
            drawTextLine(textLineTime, x, y, Paint().apply { color = theme.subTitleColor })

            //drawOrnament(decorate, link)
        }
    }.makeImageSnapshot()
}