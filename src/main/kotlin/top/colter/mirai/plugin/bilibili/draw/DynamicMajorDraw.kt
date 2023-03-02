package top.colter.mirai.plugin.bilibili.draw

import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.data.ModuleDynamic
import top.colter.mirai.plugin.bilibili.utils.*
import kotlin.math.ceil


suspend fun ModuleDynamic.Major.makeGeneral(isForward: Boolean = false): Image {
    return when (type) {
        "MAJOR_TYPE_ARCHIVE" -> if (isForward) archive!!.drawSmall() else archive!!.drawGeneral()
        "MAJOR_TYPE_BLOCKED" -> blocked!!.drawGeneral()
        "MAJOR_TYPE_DRAW" -> draw!!.drawGeneral()
        "MAJOR_TYPE_ARTICLE" -> article!!.drawGeneral()
        "MAJOR_TYPE_MUSIC" -> music!!.drawGeneral()
        "MAJOR_TYPE_LIVE" -> live!!.drawGeneral()
        "MAJOR_TYPE_LIVE_RCMD" -> liveRcmd!!.drawGeneral()
        "MAJOR_TYPE_PGC" -> pgc!!.drawSmall()
        "MAJOR_TYPE_UGC_SEASON" -> ugcSeason!!.drawSmall()
        "MAJOR_TYPE_COMMON" -> common!!.drawGeneral()
        "MAJOR_TYPE_NONE" -> drawInfoText(none?.tips!!)
        else -> drawInfoText("无法绘制类型为 [$type] 的动态类型, 请把动态链接反馈给开发者")
    }
}

fun drawInfoText(text: String): Image {
    val lineCount = if (TextLine.make(text, font).width / cardContentRect.width > 1) 2 else 1
    return Surface.makeRasterN32Premul(
        cardRect.width.toInt(),
        quality.contentFontSize.toInt() * lineCount + quality.badgeHeight + quality.cardPadding
    ).apply {
        canvas.apply {
            val paragraphStyle = ParagraphStyle().apply {
                alignment = Alignment.LEFT
                textStyle = contentTextStyle
            }
            val contentParagraph =
                ParagraphBuilder(paragraphStyle, FontUtils.fonts).addText(text).build().layout(cardContentRect.width)
            contentParagraph.paint(
                this,
                quality.cardPadding.toFloat(),
                quality.contentFontSize + quality.cardPadding / 2
            )
            //val textLine = TextLine.make(text, font)
            //drawTextLine(textLine, quality.cardPadding.toFloat(), quality.contentFontSize+quality.cardPadding/2, generalPaint)
        }
    }.makeImageSnapshot()
}


suspend fun ModuleDynamic.Major.Common.drawGeneral(): Image {

    val paragraphStyle = ParagraphStyle().apply {
        maxLinesCount = 1
        ellipsis = "..."
        alignment = Alignment.LEFT
        textStyle = titleTextStyle.apply {
            fontSize = quality.titleFontSize * 0.8f
        }
    }

    val height = quality.additionalCardHeight.toFloat()

    val commonCardRect = RRect.makeXYWH(
        quality.cardPadding.toFloat(),
        1f,
        cardContentRect.width,
        height,
        quality.cardArc
    )

    return Surface.makeRasterN32Premul(
        cardRect.width.toInt(),
        (height + quality.cardPadding).toInt()
    ).apply {
        canvas.apply {
            drawCard(commonCardRect)
            drawRectShadowAntiAlias(commonCardRect.inflate(1f), theme.smallCardShadow)

            if (badge.text.isNotBlank()) {
                val labelTextLine = TextLine.make(badge.text, font.makeWithSize(quality.subTitleFontSize))

                drawLabelCard(
                    labelTextLine,
                    commonCardRect.right - labelTextLine.width - quality.badgePadding * 4 - quality.cardPadding,
                    1 + (height - labelTextLine.height) / 2,
                    Paint().apply {
                        color = Color.makeRGB(badge.color)
                    },
                    Paint().apply {
                        color = Color.makeRGB(badge.bgColor)
                    }
                )
            }

            var x = quality.cardPadding.toFloat()

            getOrDownloadImage(cover, CacheType.OTHER)?.let { img ->
                val imgRect = RRect.makeXYWH(
                    quality.cardPadding.toFloat(),
                    1f,
                    quality.additionalCardHeight.toFloat() * img.width / img.height,
                    quality.additionalCardHeight.toFloat(),
                    quality.cardArc
                ).inflate(-1f) as RRect
                drawImageRRect(img, imgRect)
                x += imgRect.width + quality.cardPadding
            }

            val titleParagraph =
                ParagraphBuilder(paragraphStyle, FontUtils.fonts).addText(title).build()
                    .layout(cardContentRect.width - x)
            paragraphStyle.apply {
                textStyle = descTextStyle.apply {
                    fontSize = quality.subTitleFontSize * 0.8f
                }
            }
            val desc1Paragraph =
                ParagraphBuilder(paragraphStyle, FontUtils.fonts).addText(desc).build()
                    .layout(cardContentRect.width - x)
            val desc2Paragraph =
                if (label.isNotBlank()) ParagraphBuilder(paragraphStyle, FontUtils.fonts).addText(label).build()
                    .layout(cardContentRect.width - x) else null

            val top = (commonCardRect.height - (titleParagraph.height * 3)) / 2

            var y = commonCardRect.top + top + if (label.isBlank()) titleParagraph.height / 4 else 0f
            titleParagraph.paint(this, x, y)

            y += titleParagraph.height + if (label.isBlank()) titleParagraph.height / 2 else 0f
            desc1Paragraph.paint(this, x, y)

            if (desc2Paragraph != null) {
                y += titleParagraph.height
                desc2Paragraph.paint(this, x, y)
            }
        }
    }.makeImageSnapshot()
}

suspend fun ModuleDynamic.Major.Archive.drawGeneral(showStat: Boolean = false): Image {

    val paragraphStyle = ParagraphStyle().apply {
        maxLinesCount = 2
        ellipsis = "..."
        alignment = Alignment.LEFT
        textStyle = titleTextStyle
    }

    val paragraphWidth = cardContentRect.width - quality.cardPadding

    val titleParagraph = ParagraphBuilder(paragraphStyle, FontUtils.fonts).addText(title).build().layout(paragraphWidth)

    paragraphStyle.apply {
        maxLinesCount = 3
        textStyle = descTextStyle
    }

    val descParagraph =
        ParagraphBuilder(paragraphStyle, FontUtils.fonts).addText(desc.replace("\r\n", " ").replace("\n", " ")).build()
            .layout(paragraphWidth)

    val fallbackUrl = imgApi(cover, cardContentRect.width.toInt(), (cardContentRect.width * 0.625).toInt())
    val coverImg = getOrDownloadImageDefault(cover, fallbackUrl, CacheType.IMAGES)

    val videoCoverHeight = cardContentRect.width * coverImg.height / coverImg.width
    val videoCardHeight = videoCoverHeight + titleParagraph.height + descParagraph.height + quality.cardPadding

    val videoCardRect = RRect.makeComplexXYWH(
        quality.cardPadding.toFloat(),
        quality.badgeHeight + 1f,
        cardContentRect.width,
        videoCardHeight,
        cardBadgeArc
    )

    return Surface.makeRasterN32Premul(
        cardRect.width.toInt(),
        videoCardHeight.toInt() + quality.badgeHeight + quality.cardPadding
    ).apply {
        canvas.apply {

            // 绘制卡片背景
            drawCard(videoCardRect)
            // 卡片阴影
            drawRectShadowAntiAlias(videoCardRect.inflate(1f), theme.smallCardShadow)

            // 封面
            val coverRRect = RRect.makeComplexXYWH(
                videoCardRect.left,
                videoCardRect.top,
                videoCardRect.width,
                videoCoverHeight,
                cardBadgeArc
            ).inflate(-1f) as RRect
            drawImageRRect(coverImg, coverRRect)

            // 徽章
            if (BiliConfig.imageConfig.badgeEnable.left) {
                drawBadge(
                    badge.text, font, theme.subLeftBadge.fontColor, theme.subLeftBadge.bgColor, videoCardRect,
                    Position.TOP_LEFT
                )
            } else {
                val labelTextLine = TextLine.make(badge.text, font.makeWithSize(quality.subTitleFontSize))

                drawLabelCard(
                    labelTextLine,
                    videoCardRect.right - labelTextLine.width - quality.badgePadding * 4 - quality.cardPadding * 1.3f,
                    videoCardRect.top + quality.cardPadding,
                    Paint().apply { color = Color.makeRGB(badge.color) },
                    Paint().apply { color = Color.makeRGB(badge.bgColor) }
                )
            }
            if (BiliConfig.imageConfig.badgeEnable.right) {
                drawBadge(
                    "av$aid  |  $bvid",
                    font,
                    theme.subRightBadge.fontColor,
                    theme.subRightBadge.bgColor,
                    videoCardRect,
                    Position.TOP_RIGHT
                )
            }

            // 封面遮罩
            val coverMaskRRect = RRect.makeComplexLTRB(
                coverRRect.left,
                coverRRect.bottom - videoCoverHeight * 0.2f,
                coverRRect.right,
                coverRRect.bottom,
                cardBadgeArc
            )
            drawRRect(coverMaskRRect, Paint().apply {
                color = Color.BLACK
                alpha = 120
                shader = Shader.makeLinearGradient(
                    Point(coverMaskRRect.left, coverMaskRRect.bottom),
                    Point(coverMaskRRect.left, coverMaskRRect.top),
                    intArrayOf(0xFF000000.toInt(), 0x00000000.toInt())
                )
            })

            val durationText = TextLine.make(durationText, font)
            val playInfo = if (showStat) {
                val play = stat.play.toInt()
                var playStr = play.toString()
                if (play > 10000) {
                    playStr = "%.1f".format(play / 10000f) + "万"
                }
                TextLine.make("${playStr}观看 ${stat.danmaku}弹幕", font)
            } else null

            val textX = coverMaskRRect.left + quality.cardPadding * 1.3f
            val textY = coverRRect.bottom - durationText.height - quality.cardPadding
            drawLabelCard(
                durationText,
                textX,
                textY,
                Paint().apply {
                    color = Color.WHITE
                },
                Paint().apply {
                    color = Color.BLACK
                    alpha = 140
                }
            )

            if (playInfo != null) {
                drawLabelCard(
                    playInfo,
                    textX + durationText.width + quality.badgePadding * 4,
                    textY,
                    Paint().apply {
                        color = Color.WHITE
                    },
                    Paint().apply {
                        color = Color.BLACK
                        alpha = 0
                    }
                )
            }

            titleParagraph.paint(
                this,
                quality.cardPadding * 1.5f,
                quality.badgeHeight + videoCoverHeight + quality.cardPadding / 2
            )

            descParagraph.paint(
                this,
                quality.cardPadding * 1.5f,
                quality.badgeHeight + videoCoverHeight + quality.cardPadding / 2 + titleParagraph.height
            )
        }
    }.makeImageSnapshot()
}

suspend fun ModuleDynamic.Major.Live.drawGeneral(): Image {
    return drawSmallCard(title, "$descFirst $descSecond", cover, badge.text, "$id", null)
}

suspend fun ModuleDynamic.Major.LiveRcmd.drawGeneral(): Image {
    val info = liveInfo.livePlayInfo
    return drawSmallCard(
        info.title,
        "${info.parentAreaName} · ${info.areaName}",
        info.cover,
        when (info.liveStatus) {
            0 -> "未开播"
            1 -> "直播中"
            2 -> "轮播中"
            else -> "直播"
        },
        "${info.roomId}",
        null
    )
}

suspend fun ModuleDynamic.Major.Archive.drawSmall(): Image {
    return drawSmallCard(title, desc, cover, badge.text, "av$aid", durationText)
}

suspend fun ModuleDynamic.Major.Pgc.drawSmall(): Image {
    return drawSmallCard(title, "播放: ${stat.play}  弹幕: ${stat.danmaku}", cover, badge.text, "ep$epid", null)
}

suspend fun drawSmallCard(
    title: String,
    desc: String,
    cover: String,
    lbadge: String,
    rbadge: String,
    duration: String?
): Image {
    val paragraphStyle = ParagraphStyle().apply {
        maxLinesCount = 2
        ellipsis = "..."
        alignment = Alignment.LEFT
        textStyle = titleTextStyle
    }

    val coverWidth = quality.smallCardHeight * 1.6f  // 封面比例 16:10
    val paragraphWidth = cardContentRect.width - quality.cardPadding - coverWidth

    val titleParagraph = ParagraphBuilder(paragraphStyle, FontUtils.fonts).addText(title).build().layout(paragraphWidth)

    paragraphStyle.apply {
        maxLinesCount = if (titleParagraph.lineNumber == 1) 3 else 2
        textStyle = descTextStyle
    }

    val descParagraph = ParagraphBuilder(paragraphStyle, FontUtils.fonts).addText(desc).build().layout(paragraphWidth)

    val videoCardRect = RRect.makeComplexXYWH(
        quality.cardPadding.toFloat(),
        quality.badgeHeight + 1f,
        cardContentRect.width,
        quality.smallCardHeight.toFloat(),
        cardBadgeArc
    )

    return Surface.makeRasterN32Premul(
        cardRect.width.toInt(),
        quality.smallCardHeight + quality.badgeHeight + quality.cardPadding
    ).apply {
        canvas.apply {

            // 绘制卡片背景
            drawCard(videoCardRect)
            // 卡片阴影
            drawRectShadowAntiAlias(videoCardRect.inflate(1f), theme.smallCardShadow)

            // 徽章
            if (BiliConfig.imageConfig.badgeEnable.left) {
                drawBadge(
                    lbadge,
                    font,
                    theme.subLeftBadge.fontColor,
                    theme.subLeftBadge.bgColor,
                    videoCardRect,
                    Position.TOP_LEFT
                )
            }
            if (BiliConfig.imageConfig.badgeEnable.right) {
                drawBadge(
                    rbadge,
                    font,
                    theme.subRightBadge.fontColor,
                    theme.subRightBadge.bgColor,
                    videoCardRect,
                    Position.TOP_RIGHT
                )
            }

            // 封面
            val fallbackUrl = imgApi(cover, coverWidth.toInt(), quality.smallCardHeight)
            val coverImg = getOrDownloadImageDefault(cover, fallbackUrl, CacheType.IMAGES)
            val coverRRect = RRect.makeComplexXYWH(
                videoCardRect.left, videoCardRect.top, coverWidth,
                quality.smallCardHeight.toFloat(), cardBadgeArc
            ).inflate(-1f) as RRect
            drawImageRRect(coverImg, coverRRect)

            val space = (videoCardRect.height - titleParagraph.height - descParagraph.height) / 3
            val y = videoCardRect.top + space
            titleParagraph.paint(
                this,
                quality.cardPadding * 1.5f + coverWidth,
                y
            )

            descParagraph.paint(
                this,
                quality.cardPadding * 1.5f + coverWidth,
                y + titleParagraph.height + space
            )

            if (duration != null) {
                val durationTextLine = TextLine.make(duration, font.makeWithSize(quality.subTitleFontSize))
                drawLabelCard(
                    durationTextLine,
                    coverRRect.left + quality.badgePadding * 2,
                    coverRRect.bottom - durationTextLine.height - quality.badgePadding * 2,
                    Paint().apply { color = Color.WHITE },
                    Paint().apply {
                        color = Color.BLACK
                        alpha = 130
                    }
                )
            }
        }
    }.makeImageSnapshot()
}

suspend fun ModuleDynamic.Major.Draw.drawGeneral(): Image {

    var drawItemWidth = 0f
    var drawItemHeight = 0f
    var drawItemSpace = quality.drawSpace * 2
    var drawItemNum = 1

    when (items.size) {
        1 -> {
            drawItemWidth = if (items[0].width > cardContentRect.width / 2) {
                cardContentRect.width
            } else {
                items[0].width * 2f
            }
            val drawHeight = items[0].height.toFloat() / items[0].width.toFloat() * drawItemWidth
            drawItemHeight = if (drawHeight > drawItemWidth * 2) {
                drawItemWidth * 2
            } else {
                drawHeight
            }
        }

        2, 4 -> {
            drawItemWidth = (cardContentRect.width - quality.drawSpace) / 2
            drawItemHeight = drawItemWidth
            if (items.size >= 3) {
                drawItemSpace += quality.drawSpace
            }
            drawItemNum = 2
        }

        3, in 5..9 -> {
            drawItemWidth = (cardContentRect.width - quality.drawSpace * 2) / 3
            drawItemHeight = drawItemWidth
            drawItemSpace += if (items.size <= 6) {
                quality.drawSpace
            } else {
                quality.drawSpace * 2
            }
            drawItemNum = 3
        }
    }

    return Surface.makeRasterN32Premul(
        cardRect.width.toInt(),
        (drawItemHeight * ceil(items.size / drawItemNum.toFloat()) + drawItemSpace).toInt()
    ).apply {
        canvas.apply {

            var x = quality.cardPadding.toFloat()
            var y = quality.drawSpace.toFloat()

            items.forEachIndexed { index, drawItem ->
                val fallbackUrl = imgApi(drawItem.src, drawItemWidth.toInt(), drawItemHeight.toInt())
                val img = getOrDownloadImageDefault(drawItem.src, fallbackUrl, CacheType.IMAGES)
                val dstRect = RRect.makeXYWH(x, y, drawItemWidth, drawItemHeight, quality.cardArc)

                drawRRect(dstRect, Paint().apply {
                    color = Color.WHITE
                    alpha = 160
                    mode = PaintMode.FILL
                    isAntiAlias = true
                })

                drawImageClip(img, dstRect)

                drawRRect(dstRect, Paint().apply {
                    color = theme.drawOutlineColor
                    mode = PaintMode.STROKE
                    strokeWidth = quality.drawOutlineWidth
                    isAntiAlias = true
                })

                x += drawItemWidth + quality.drawSpace

                if ((index + 1) % drawItemNum == 0) {
                    x = quality.cardPadding.toFloat()
                    y += drawItemHeight + quality.drawSpace
                }
            }
        }
    }.makeImageSnapshot()
}

fun ModuleDynamic.Major.Blocked.drawGeneral(): Image {
    val img = when(blockType){
        1 -> Image.makeFromEncoded(loadResourceBytes("image/SponsorBlocked.png"))
        else -> Image.makeFromEncoded(loadResourceBytes("image/Blocked_BG_Day.png"))
    }
    val w = cardContentRect.width - 2 * quality.cardPadding
    val h = img.height / img.width * w
    return Surface.makeRasterN32Premul(
        (w + 2 * quality.cardPadding).toInt(), (h + 2 * quality.cardPadding).toInt()
    ).apply {
        canvas.apply {
            drawImageClip(
                img,
                RRect.Companion.makeXYWH(
                    quality.cardPadding.toFloat(), 0f,
                    w, h, quality.cardArc
            ))
            if(blockType!= 1){
                val paragraphStyle = ParagraphStyle().apply {
                    maxLinesCount = 2
                    ellipsis = "..."
                    alignment = Alignment.CENTER
                    textStyle = titleTextStyle.apply {
                        color = Color.WHITE
                    }
                }
                val hintMessage = ParagraphBuilder(paragraphStyle, FontUtils.fonts)
                    .addText(hintMessage).build().layout(w)
                hintMessage.paint(this, 0f, (h - hintMessage.height)/2 + quality.cardPadding)
            }
        }
    }.makeImageSnapshot()
}


suspend fun ModuleDynamic.Major.Article.drawGeneral(): Image {

    val paragraphStyle = ParagraphStyle().apply {
        maxLinesCount = 2
        ellipsis = "..."
        alignment = Alignment.LEFT
        textStyle = titleTextStyle
    }

    val paragraphWidth = cardContentRect.width - quality.cardPadding

    val titleParagraph = ParagraphBuilder(paragraphStyle, FontUtils.fonts).addText(title).build().layout(paragraphWidth)

    paragraphStyle.apply {
        maxLinesCount = 3
        textStyle = descTextStyle
    }

    val descParagraph = ParagraphBuilder(paragraphStyle, FontUtils.fonts).addText(desc).build().layout(paragraphWidth)

    val articleCoverHeight = cardContentRect.width * if (covers.size == 1) 0.29375f else 0.23166f
    val articleCardHeight = articleCoverHeight + titleParagraph.height + descParagraph.height + quality.cardPadding

    val articleCardRect = RRect.makeComplexXYWH(
        quality.cardPadding.toFloat(),
        quality.badgeHeight + 1f,
        cardContentRect.width,
        articleCardHeight,
        cardBadgeArc
    )

    return Surface.makeRasterN32Premul(
        cardRect.width.toInt(),
        articleCardHeight.toInt() + quality.badgeHeight + quality.cardPadding
    ).apply {
        canvas.apply {

            // 绘制卡片背景
            drawCard(articleCardRect)
            // 卡片阴影
            drawRectShadowAntiAlias(articleCardRect.inflate(1f), theme.smallCardShadow)

            // 封面
            val coverRRect = RRect.makeComplexXYWH(
                articleCardRect.left,
                articleCardRect.top,
                articleCardRect.width,
                articleCoverHeight,
                cardBadgeArc
            ).inflate(-1f) as RRect
            if (covers.size == 1) {
                val fallbackUrl = imgApi(covers[0], articleCardRect.width.toInt(), articleCoverHeight.toInt())
                val coverImg = getOrDownloadImageDefault(covers[0], fallbackUrl, CacheType.IMAGES)
                drawImageRRect(coverImg, coverRRect)
            } else {
                var imgX = articleCardRect.left
                val imgW = articleCardRect.width / 3 - 4
                save()
                clipRRect(coverRRect, true)
                covers.forEach {
                    val fallbackUrl = imgApi(it, imgW.toInt(), articleCoverHeight.toInt())
                    val img = getOrDownloadImageDefault(it, fallbackUrl, CacheType.IMAGES)
                    val tar = RRect.makeXYWH(imgX, articleCardRect.top, imgW, articleCoverHeight, 0f)
                    drawImageClip(img, tar, Paint())
                    imgX += articleCardRect.width / 3 + 2
                }
                restore()
            }

            // 徽章
            if (BiliConfig.imageConfig.badgeEnable.left) {
                drawBadge(
                    "专栏", font, theme.subLeftBadge.fontColor, theme.subLeftBadge.bgColor, articleCardRect,
                    Position.TOP_LEFT
                )
            } else {
                val labelTextLine = TextLine.make("专栏", font.makeWithSize(quality.subTitleFontSize))
                drawLabelCard(
                    labelTextLine,
                    articleCardRect.right - labelTextLine.width - quality.badgePadding * 4 - quality.cardPadding,
                    articleCardRect.top + quality.cardPadding * 0.8f,
                    Paint().apply { color = Color.WHITE },
                    Paint().apply { color = Color.makeRGB(251, 114, 153) }
                )
            }
            if (BiliConfig.imageConfig.badgeEnable.right) {
                drawBadge(
                    "cv$id",
                    font,
                    theme.subRightBadge.fontColor,
                    theme.subRightBadge.bgColor,
                    articleCardRect,
                    Position.TOP_RIGHT
                )
            }

            titleParagraph.paint(
                this,
                quality.cardPadding * 1.5f,
                quality.badgeHeight + articleCoverHeight + quality.cardPadding / 2
            )

            descParagraph.paint(
                this,
                quality.cardPadding * 1.5f,
                quality.badgeHeight + articleCoverHeight + quality.cardPadding / 2 + titleParagraph.height
            )

        }
    }.makeImageSnapshot()
}

suspend fun ModuleDynamic.Major.Music.drawGeneral(): Image {

    val paragraphStyle = ParagraphStyle().apply {
        maxLinesCount = 2
        ellipsis = "..."
        alignment = Alignment.LEFT
        textStyle = titleTextStyle
    }

    val musicCardHeight = cardContentRect.width * 0.19f

    val paragraphWidth = cardContentRect.width - quality.cardPadding * 2 - musicCardHeight

    val titleParagraph = ParagraphBuilder(paragraphStyle, FontUtils.fonts).addText(title).build().layout(paragraphWidth)

    paragraphStyle.apply {
        textStyle = descTextStyle
    }

    val descParagraph = ParagraphBuilder(paragraphStyle, FontUtils.fonts).addText(label).build().layout(paragraphWidth)

    val musicCardRect = RRect.makeComplexXYWH(
        quality.cardPadding.toFloat(),
        quality.badgeHeight + 1f,
        cardContentRect.width,
        musicCardHeight,
        cardBadgeArc
    )

    return Surface.makeRasterN32Premul(
        cardRect.width.toInt(),
        musicCardHeight.toInt() + quality.badgeHeight + quality.cardPadding
    ).apply {
        canvas.apply {

            // 绘制卡片背景
            drawCard(musicCardRect)
            // 卡片阴影
            drawRectShadowAntiAlias(musicCardRect.inflate(1f), theme.smallCardShadow)

            // 徽章
            if (BiliConfig.imageConfig.badgeEnable.left) {
                drawBadge(
                    "音乐", font, theme.subLeftBadge.fontColor, theme.subLeftBadge.bgColor, musicCardRect,
                    Position.TOP_LEFT
                )
            }
            if (BiliConfig.imageConfig.badgeEnable.right) {
                drawBadge(
                    "au$id",
                    font,
                    theme.subRightBadge.fontColor,
                    theme.subRightBadge.bgColor,
                    musicCardRect,
                    Position.TOP_RIGHT
                )
            }

            // 封面
            val fallbackUrl = imgApi(cover, musicCardHeight.toInt(), musicCardHeight.toInt())
            val coverImg = getOrDownloadImageDefault(cover, fallbackUrl, CacheType.IMAGES)
            val coverRRect = RRect.makeComplexXYWH(
                musicCardRect.left,
                musicCardRect.top,
                musicCardHeight,
                musicCardHeight,
                cardBadgeArc
            ).inflate(-1f) as RRect
            drawImageRRect(coverImg, coverRRect)

            val space = (musicCardHeight - titleParagraph.height - descParagraph.height) / 3
            val y = musicCardRect.top + space

            titleParagraph.paint(
                this,
                musicCardHeight + quality.cardMargin * 2,
                y
                //(quality.badgeHeight + quality.cardMargin * 2).toFloat()
            )

            descParagraph.paint(
                this,
                musicCardHeight + quality.cardMargin * 2,
                y + space + titleParagraph.height
                //quality.badgeHeight + quality.cardMargin * 2 + titleParagraph.height
            )

        }
    }.makeImageSnapshot()
}