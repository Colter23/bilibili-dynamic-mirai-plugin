package top.colter.mirai.plugin.bilibili.draw

import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skia.svg.SVGDOM
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.BiliDynamicConfig.debugMode
import top.colter.mirai.plugin.bilibili.BiliDynamicConfig.imageConfig
import top.colter.mirai.plugin.bilibili.data.DynamicItem
import top.colter.mirai.plugin.bilibili.data.DynamicType.DYNAMIC_TYPE_FORWARD
import top.colter.mirai.plugin.bilibili.data.ModuleAuthor
import top.colter.mirai.plugin.bilibili.data.ModuleDispute
import top.colter.mirai.plugin.bilibili.data.ModuleDynamic
import top.colter.mirai.plugin.bilibili.draw.Position.*
import top.colter.mirai.plugin.bilibili.utils.*
import top.colter.mirai.plugin.bilibili.utils.FontUtils.fonts
import top.colter.mirai.plugin.bilibili.utils.FontUtils.loadTypeface
import kotlin.io.path.pathString
import kotlin.math.ceil

internal val logger by BiliBiliDynamic::logger

//private const val resourcesPath = "src/main/resources"
private val resourcesPath = loadResource("")

private val quality: Quality by lazy {
    Quality.level(imageConfig.quality).apply {
        badgeHeight = if (imageConfig.badgeEnable) badgeHeight else 0
    }
}

private val cardRect: Rect by lazy {
    Rect.makeLTRB(quality.cardMargin.toFloat(), 0f, quality.imageWidth - quality.cardMargin.toFloat(), 0f)
}

private val cardContentRect: Rect by lazy {
    cardRect.inflate(-1f * quality.cardPadding)
}

private val mainTypeface: Typeface by lazy {
    //loadTypeface("data/top.colter.bilibili-dynamic-mirai-plugin/font/HarmonyOS_Sans_SC_Medium.ttf")
    loadTypeface("${BiliBiliDynamic.dataFolderPath.pathString}/font/HarmonyOS_Sans_SC_Medium.ttf")
}

private val font: Font by lazy {
    Font(mainTypeface, quality.contentFontSize)
}

private val fansCardFont: Font by lazy {
    Font(loadTypeface("$resourcesPath/font/FansCard.ttf"), quality.subTitleFontSize)
}

val titleTextStyle = TextStyle().apply {
    fontSize = quality.titleFontSize
    color = Color.makeRGB(49, 49, 49)
    typeface = mainTypeface
}
val descTextStyle = TextStyle().apply {
    fontSize = quality.subTitleFontSize
    color = Color.makeRGB(102, 102, 102)
    typeface = mainTypeface
}

val topTwoBadgeCardArc = if (imageConfig.badgeEnable)
    floatArrayOf(0f, 0f, quality.cardArc, quality.cardArc)
else
    floatArrayOf(quality.cardArc, quality.cardArc, quality.cardArc, quality.cardArc)

val linkPaint = Paint().apply {
    color = Color.makeRGB(23, 139, 207)
    isAntiAlias = true
}
val generalPaint = Paint().apply {
    color = Color.makeRGB(34, 34, 34)
    isAntiAlias = true
}


enum class Position {
    TOP_LEFT,
    TOP,
    TOP_RIGHT,
    LEFT,
    CENTER,
    RIGHT,
    BOTTOM_LEFT,
    BOTTOM,
    BOTTOM_RIGHT
}

suspend fun DynamicItem.makeDrawDynamic(): String {

    val dynamic = drawDynamic()

    val img = makeCardBg(dynamic.height) {
        it.drawImage(dynamic, 0f, 0f)
    }
    return cacheImage(img, "$uid/$idStr.png", CacheType.DRAW_DYNAMIC)
}

suspend fun DynamicItem.drawDynamic(isForward: Boolean = false): Image {

    val orig = orig?.drawDynamic(type == DYNAMIC_TYPE_FORWARD)

    var imgList = modules.makeGeneral(formatTime, link, isForward)

    if (orig != null){
        imgList = if (this.modules.moduleDynamic.additional != null){
            val result = ArrayList<Image>(imgList.size + 1)
            result.addAll(imgList.subList(0, imgList.size - 1))
            result.add(orig)
            result.add(imgList.last())
            result
        }else {
            imgList.plus(orig)
        }
    }

    val height = imgList.sumOf {
        if (it.width > cardRect.width) {
            (cardRect.width * it.height / it.width + 10).toInt()
        } else {
            it.height + 10
        }
    }

    val margin = if (isForward) quality.cardPadding * 2 else quality.cardMargin * 2

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
                topTwoBadgeCardArc
            )

            drawCard(rrect)

            if (isForward) {
                drawRectShadowAntiAlias(rrect.inflate(1f), 5f, 5f, 15f, 0f, Color.makeARGB(30, 0, 0, 0))
            } else {
                drawRectShadowAntiAlias(rrect.inflate(1f), 6f, 6f, 25f, 0f, Color.makeARGB(70, 0, 0, 0))
            }

            if (imageConfig.badgeEnable) {
                val svg = SVGDOM(Data.makeFromBytes(loadResourceBytes("icon/${if (isForward) "FORWARD" else "BILIBILI_LOGO"}.svg")))
                //val svg = SVGDOM(Data.makeFromFileName("$resourcesPath/icon/${if (isForward) "FORWARD" else "BILIBILI_LOGO"}.svg"))
                drawBadge(
                    if (isForward) "转发动态" else "动态",
                    font,
                    Color.makeRGB(0, 203, 255),
                    Color.WHITE,
                    120,
                    rrect,
                    TOP_LEFT,
                    svg.makeImage(quality.contentFontSize, quality.contentFontSize)
                )

                drawBadge(idStr, font, Color.WHITE, Color.makeRGB(72, 199, 240), 255, rrect, TOP_RIGHT)
            }

            var top = quality.cardMargin + quality.badgeHeight.toFloat()
            for (img in imgList) {

                if (debugMode) {
                    drawScaleWidthImageOutline(img, cardRect.width, quality.cardMargin.toFloat(), top, isForward)
                } else {
                    drawScaleWidthImage(img, cardRect.width, quality.cardMargin.toFloat(), top)
                }

                top += if (img.width > cardRect.width) {
                    (cardRect.width * img.height / img.width + 10).toInt()
                } else {
                    img.height + 10
                }
            }

        }
    }.makeImageSnapshot()


}

suspend fun DynamicItem.Modules.makeGeneral(time: String, link: String, isForward: Boolean = false): List<Image> {
    return mutableListOf<Image>().apply {
        add(if (isForward) moduleAuthor.drawForward(time) else moduleAuthor.drawGeneral(time, link))
        moduleDispute?.drawGeneral()?.let { add(it) }
        addAll(moduleDynamic.makeGeneral(isForward))
    }
}

suspend fun ModuleDynamic.makeGeneral(isForward: Boolean = false): List<Image> {
    return mutableListOf<Image>().apply {
        topic?.drawGeneral()?.let { add(it) }
        desc?.drawGeneral()?.let { add(it) }
        major?.makeGeneral(isForward)?.let { add(it) }
        additional?.makeGeneral()?.let { add(it) }
    }
}

suspend fun ModuleDynamic.Additional.makeGeneral(): Image? {
    return when (type) {
        "ADDITIONAL_TYPE_COMMON" -> {
            drawAdditionalCard(
                common!!.headText,
                common.cover,
                common.title,
                common.desc1,
                common.desc2
            )
        }
        "ADDITIONAL_TYPE_RESERVE" -> {
            drawAdditionalCard(
                when (reserve!!.stype){
                    1 -> "视频预约"
                    2 -> "直播预约"
                    4 -> "首映预告"
                    else -> "预约"
                },
                reserve.premiere?.cover,
                reserve.title,
                "${reserve.desc1.text}  ${reserve.desc2.text}",
                reserve.desc3?.text
            )
        }
        "ADDITIONAL_TYPE_VOTE" -> {
            drawAdditionalCard(
                "投票",
                null,
                vote!!.desc,
                "结束时间 ${vote.endTime.formatTime}",
                null
            )
        }
        "ADDITIONAL_TYPE_UGC" -> {
            drawAdditionalCard(
                ugc!!.headText,
                ugc.cover,
                ugc.title,
                "时长 ${ugc.duration}  ${ugc.descSecond}",
                null
            )
        }
        "ADDITIONAL_TYPE_GOODS" -> {
            drawAdditionalCard(
                goods!!.headText,
                goods.items[0].cover,
                goods.items[0].name,
                "${goods.items[0].price} 起",
                null
            )
        }
        else -> {
            logger.warning("未知类型附加卡片 $type")
            null
        }
    }
}

suspend fun ModuleDynamic.Major.makeGeneral(isForward: Boolean = false): Image? {
    return when (type) {
        "MAJOR_TYPE_ARCHIVE" -> {
            if (isForward) archive!!.drawSmall() else archive!!.drawGeneral()
        }
        "MAJOR_TYPE_DRAW" -> {
            draw!!.drawGeneral()
        }
        "MAJOR_TYPE_ARTICLE" -> {
            article!!.drawGeneral()
        }
        "MAJOR_TYPE_MUSIC" -> {
            music!!.drawGeneral()
        }
        "MAJOR_TYPE_LIVE" -> {
            null
        }
        "MAJOR_TYPE_LIVE_RCMD" -> {
            null
        }
        "MAJOR_TYPE_PGC" -> {
            null
        }
        "MAJOR_TYPE_COMMON" -> {
            null
        }
        "MAJOR_TYPE_NONE" -> {
            null
        }
        else -> {
            null
        }
    }
}

fun ModuleDispute.drawGeneral(): Image {
    return Surface.makeRasterN32Premul(10, 10).apply {
        canvas.apply {

            //TODO

        }
    }.makeImageSnapshot()
}


fun ModuleDynamic.Topic.drawGeneral(): Image {

    val lineCount = if (TextLine.make(name, font).width / cardContentRect.width > 1) 2 else 1
    val textCardHeight = (quality.contentFontSize + quality.lineSpace * 2) * lineCount

    val textCardRect = Rect.makeXYWH(
        quality.cardPadding.toFloat(),
        0f,
        cardContentRect.width,
        textCardHeight
    )

    return Surface.makeRasterN32Premul(cardRect.width.toInt(), textCardHeight.toInt()).apply {
        canvas.apply {
            var x = quality.cardPadding.toFloat()
            var y = quality.contentFontSize * 0.8f + quality.lineSpace
            try {
                //val svg = SVGDOM(Data.makeFromBytes(BiliBiliDynamic.getResourceAsStream("src/main/resources/icon/RICH_TEXT_NODE_TYPE_WEB.svg")!!.readBytes()))
                val svg = SVGDOM(Data.makeFromBytes(loadResourceBytes("icon/TOPIC.svg")))
                //val svg = SVGDOM(Data.makeFromFileName("$resourcesPath/icon/TOPIC.svg"))
                val iconSize = quality.contentFontSize
                drawImage(svg.makeImage(iconSize, iconSize), x, y - quality.contentFontSize * 0.9f)
                x += iconSize + quality.lineSpace
            } catch (e: Exception) {
                logger.warning("未找到类型为 TOPIC 的图标")
            }

            drawTextArea(name, textCardRect, x, y, font, linkPaint)
        }
    }.makeImageSnapshot()

}

suspend fun ModuleDynamic.Desc.drawGeneral(): Image {
    val paragraphStyle = ParagraphStyle().apply {
        alignment = Alignment.LEFT
        textStyle = titleTextStyle
    }

    val textParagraph = ParagraphBuilder(paragraphStyle, fonts).addText(text).build().layout(cardContentRect.width)

    val textCardHeight = (quality.contentFontSize + quality.lineSpace * 2) * textParagraph.lineNumber

    val textCardRect = Rect.makeXYWH(
        quality.cardPadding.toFloat(),
        0f,
        cardContentRect.width,
        textCardHeight
    )

    var x = textCardRect.left
    var y = quality.contentFontSize + quality.lineSpace

    return Surface.makeRasterN32Premul(cardRect.width.toInt(), textCardHeight.toInt()).apply {
        canvas.apply {
            this@drawGeneral.richTextNodes.forEach {
                when (it.type) {
                    "RICH_TEXT_NODE_TYPE_TEXT" -> {
                        val point = drawTextArea(it.text, textCardRect, x, y, font, generalPaint)
                        x = point.x
                        y = point.y
                    }
                    "RICH_TEXT_NODE_TYPE_EMOJI" -> {
                        val img = getOrDownloadImage(it.emoji!!.iconUrl, CacheType.EMOJI)

                        val emojiSize = TextLine.make("🙂", font).height

                        if (x + emojiSize > textCardRect.right) {
                            x = textCardRect.left
                            y += emojiSize + quality.lineSpace
                        }
                        val srcRect = Rect.makeXYWH(0f, 0f, img.width.toFloat(), img.height.toFloat())
                        val tarRect = Rect.makeXYWH(x, y - emojiSize * 0.8f, emojiSize, emojiSize)
                        drawImageRect(
                            img,
                            srcRect,
                            tarRect,
                            FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST),
                            null,
                            true
                        )
                        x += emojiSize
                    }
                    "RICH_TEXT_NODE_TYPE_WEB",
                    "RICH_TEXT_NODE_TYPE_VOTE",
                    "RICH_TEXT_NODE_TYPE_LOTTERY",
                    "RICH_TEXT_NODE_TYPE_BV" -> {
                        try {
                            val svg = SVGDOM(Data.makeFromBytes(loadResourceBytes("icon/${it.type}.svg")))
                            //val svg = SVGDOM(Data.makeFromFileName("$resourcesPath/icon/${it.type}.svg"))
                            val iconSize = quality.contentFontSize
                            drawImage(svg.makeImage(iconSize, iconSize), x, y - quality.contentFontSize * 0.9f)
                            x += iconSize
                        } catch (e: Exception) {
                            logger.warning("未找到类型为 ${it.type} 的图标")
                        }

                        val point = drawTextArea(it.text, textCardRect, x, y, font, linkPaint)
                        x = point.x
                        y = point.y
                    }
                    else -> {
                        val point = drawTextArea(it.text, textCardRect, x, y, font, linkPaint)
                        x = point.x
                        y = point.y
                    }
                }
            }
        }
    }.makeImageSnapshot(IRect.makeXYWH(0, 0, cardRect.width.toInt(), ceil(y + quality.lineSpace * 2).toInt()))!!
}

suspend fun drawAdditionalCard(
    label: String,
    cover: String?,
    title: String,
    desc1: String,
    desc2: String?
): Image {

    val paragraphStyle = ParagraphStyle().apply {
        maxLinesCount = 1
        ellipsis = "..."
        alignment = Alignment.LEFT
        textStyle = titleTextStyle.apply {
            fontSize = quality.titleFontSize * 0.8f
        }
    }

    val height = if (cover != null || desc2 != null)
        quality.additionalCardHeight.toFloat()
    else
        quality.additionalCardHeight * 0.7f


    val additionalCardRect = RRect.makeXYWH(
        quality.cardPadding.toFloat(),
        quality.subTitleFontSize + quality.cardPadding + 1f,
        cardContentRect.width,
        height,
        quality.cardArc
    )

    return Surface.makeRasterN32Premul(
        cardRect.width.toInt(),
        (height + quality.subTitleFontSize + quality.cardPadding * 2f).toInt()
    ).apply {
        canvas.apply {
            drawCard(additionalCardRect)
            drawRectShadowAntiAlias(additionalCardRect.inflate(1f), 5f, 5f, 15f, 0f, Color.makeARGB(30, 0, 0, 0))

            val labelTextLine = TextLine.make(label, font.makeWithSize(quality.subTitleFontSize))
            drawTextLine(labelTextLine, additionalCardRect.left + 8, quality.subTitleFontSize, Paint().apply {
                color = Color.BLACK
                alpha = 130
            })

            var x = quality.cardPadding.toFloat()

            if (cover != null) {
                val img = getOrDownloadImage(cover, CacheType.IMAGES)
                val imgRect = RRect.makeXYWH(
                    quality.cardPadding.toFloat(),
                    quality.subTitleFontSize + quality.cardPadding + 1f,
                    quality.additionalCardHeight.toFloat() * img.width / img.height,
                    quality.additionalCardHeight.toFloat(),
                    quality.cardArc
                ).inflate(-1f) as RRect
                drawImageRRect(img, imgRect)
                x += imgRect.width
            }

            x += quality.cardPadding

            val titleParagraph = ParagraphBuilder(paragraphStyle, fonts).addText(title).build().layout(cardContentRect.width - x)
            paragraphStyle.apply {
                textStyle = descTextStyle.apply {
                    fontSize = quality.subTitleFontSize * 0.8f
                }
            }
            val desc1Paragraph = ParagraphBuilder(paragraphStyle, fonts).addText(desc1).build().layout(cardContentRect.width - x)
            val desc2Paragraph = desc2?.let { ParagraphBuilder(paragraphStyle, fonts).addText(it).build().layout(cardContentRect.width - x) }

            val top = (additionalCardRect.height - (titleParagraph.height * if (desc2==null) 2 else 3))/2

            var y = additionalCardRect.top + top
            titleParagraph.paint(this, x, y)

            y += titleParagraph.height
            desc1Paragraph.paint(this, x, y)

            if (desc2Paragraph != null){
                y += titleParagraph.height
                desc2Paragraph.paint(this, x, y)
            }
        }
    }.makeImageSnapshot()


}

suspend fun ModuleDynamic.Major.Archive.drawGeneral(): Image {

    val paragraphStyle = ParagraphStyle().apply {
        maxLinesCount = 2
        ellipsis = "..."
        alignment = Alignment.LEFT
        textStyle = titleTextStyle
    }

    val paragraphWidth = cardContentRect.width - quality.cardPadding

    val titleParagraph = ParagraphBuilder(paragraphStyle, fonts).addText(title).build().layout(paragraphWidth)

    paragraphStyle.apply {
        maxLinesCount = 3
        textStyle = descTextStyle
    }

    val descParagraph = ParagraphBuilder(paragraphStyle, fonts).addText(desc).build().layout(paragraphWidth)

    val videoCoverHeight = cardContentRect.width * 0.625f  // 封面比例 16:10
    val videoCardHeight = videoCoverHeight + titleParagraph.height + descParagraph.height + quality.cardPadding

    val videoCardRect = RRect.makeComplexXYWH(
        quality.cardPadding.toFloat(),
        quality.badgeHeight + 1f,
        cardContentRect.width,
        videoCardHeight,
        topTwoBadgeCardArc
    )

    return Surface.makeRasterN32Premul(
        cardRect.width.toInt(),
        videoCardHeight.toInt() + quality.badgeHeight + quality.cardPadding
    ).apply {
        canvas.apply {

            // 绘制卡片背景
            drawCard(videoCardRect)
            // 卡片阴影
            drawRectShadowAntiAlias(videoCardRect.inflate(1f), 5f, 5f, 15f, 0f, Color.makeARGB(30, 0, 0, 0))

            // 徽章
            if (imageConfig.badgeEnable) {
                drawBadge(badge.text, font, Color.WHITE, Color.makeRGB(251, 114, 153), 255, videoCardRect, TOP_LEFT)
                drawBadge(
                    "av$aid  |  $bvid",
                    font,
                    Color.WHITE,
                    Color.makeRGB(72, 199, 240),
                    255,
                    videoCardRect,
                    TOP_RIGHT
                )
            }
            // 封面
            val coverImg = getOrDownloadImage(cover, CacheType.IMAGES)
            val coverRRect = RRect.makeComplexXYWH(
                videoCardRect.left,
                videoCardRect.top,
                videoCardRect.width,
                videoCoverHeight,
                topTwoBadgeCardArc
            ).inflate(-1f) as RRect
            drawImageRRect(coverImg, coverRRect)

            // 封面遮罩
            val coverMaskRRect = RRect.makeComplexLTRB(
                coverRRect.left,
                coverRRect.bottom - videoCoverHeight * 0.2f,
                coverRRect.right,
                coverRRect.bottom,
                topTwoBadgeCardArc
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
            val playInfo = TextLine.make("${stat.play}观看 ${stat.danmaku}弹幕", font.makeWithSize(22f))

            val durationRRect = RRect.makeXYWH(
                coverMaskRRect.left + quality.cardPadding * 1.3f,
                coverRRect.bottom - quality.badgeHeight - quality.cardPadding,
                durationText.width + quality.badgePadding * 4,
                quality.badgeHeight.toFloat(),
                quality.badgeArc
            )
            drawRRect(durationRRect, Paint().apply {
                color = Color.BLACK
                alpha = 130
            })

            drawTextLine(
                durationText,
                durationRRect.left + quality.badgePadding * 2,
                durationRRect.textVertical(durationText),
                Paint().apply {
                    color = Color.WHITE
                })

            drawTextLine(
                playInfo,
                durationRRect.right + quality.badgePadding * 2,
                durationRRect.textVertical(playInfo),
                Paint().apply {
                    color = Color.WHITE
                })

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


suspend fun ModuleDynamic.Major.Archive.drawSmall(): Image {

    val paragraphStyle = ParagraphStyle().apply {
        maxLinesCount = 2
        ellipsis = "..."
        alignment = Alignment.LEFT
        textStyle = titleTextStyle
    }

    val videoCoverWidth = quality.smallCardHeight * 1.6f  // 封面比例 16:10
    val paragraphWidth = cardContentRect.width - quality.cardPadding - videoCoverWidth

    val titleParagraph = ParagraphBuilder(paragraphStyle, fonts).addText(title).build().layout(paragraphWidth)

    paragraphStyle.apply {
        maxLinesCount = if (titleParagraph.lineNumber == 1) 3 else 2
        textStyle = descTextStyle
    }

    val descParagraph = ParagraphBuilder(paragraphStyle, fonts).addText(desc).build().layout(paragraphWidth)

    val videoCardRect = RRect.makeComplexXYWH(
        quality.cardPadding.toFloat(),
        quality.badgeHeight + 1f,
        cardContentRect.width,
        quality.smallCardHeight.toFloat(),
        topTwoBadgeCardArc
    )

    return Surface.makeRasterN32Premul(
        cardRect.width.toInt(),
        quality.smallCardHeight + quality.badgeHeight + quality.cardPadding
    ).apply {
        canvas.apply {

            // 绘制卡片背景
            drawCard(videoCardRect)
            // 卡片阴影
            drawRectShadowAntiAlias(videoCardRect.inflate(1f), 5f, 5f, 15f, 0f, Color.makeARGB(30, 0, 0, 0))

            // 徽章
            if (imageConfig.badgeEnable) {
                drawBadge(
                    badge.text,
                    font,
                    Color.WHITE,
                    Color.makeRGB(251, 114, 153),
                    255,
                    videoCardRect,
                    TOP_LEFT
                )
                drawBadge(
                    "av$aid  |  $bvid",
                    font,
                    Color.WHITE,
                    Color.makeRGB(72, 199, 240),
                    255,
                    videoCardRect,
                    TOP_RIGHT
                )
            }
            // 封面
            val coverImg = getOrDownloadImage(cover, CacheType.IMAGES)
            val coverRRect = RRect.makeComplexXYWH(
                videoCardRect.left, videoCardRect.top, videoCoverWidth,
                quality.smallCardHeight.toFloat(), topTwoBadgeCardArc
            ).inflate(-1f) as RRect
            drawImageRRect(coverImg, coverRRect)

            val y = videoCardRect.top + (videoCardRect.height - titleParagraph.height - descParagraph.height) / 2
            titleParagraph.paint(
                this,
                quality.cardPadding * 1.5f + videoCoverWidth,
                 y
            )

            descParagraph.paint(
                this,
                quality.cardPadding * 1.5f + videoCoverWidth,
                y + titleParagraph.height
            )

            drawLabelCard(
                durationText, font.makeWithSize(quality.subTitleFontSize), Color.WHITE, Color.BLACK, 130,
                coverRRect.left + quality.badgePadding * 2, coverRRect.bottom - quality.subTitleFontSize - quality.badgePadding
            )

        }
    }.makeImageSnapshot()
}

suspend fun ModuleDynamic.Major.Draw.drawGeneral(): Image {

    var drawItemWidth = 0f
    var drawItemHeight = 0f
    var drawItemSpace = quality.drawSpace * 2
    var drawItemNum = 0

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
        (drawItemHeight * items.size / drawItemNum + drawItemSpace).toInt()
    ).apply {
        canvas.apply {

            var x = quality.cardPadding.toFloat()
            var y = quality.drawSpace.toFloat()

            items.forEachIndexed { index, drawItem ->
                val img = getOrDownloadImage(drawItem.src, CacheType.IMAGES)
                val dstRect = RRect.makeXYWH(x, y, drawItemWidth, drawItemHeight, quality.cardArc)

                drawRRect(dstRect, Paint().apply {
                    color = Color.WHITE
                    alpha = 160
                    mode = PaintMode.FILL
                    isAntiAlias = true
                })

                drawImageClip(img, dstRect)

                drawRRect(dstRect, Paint().apply {
                    color = Color.WHITE
                    mode = PaintMode.STROKE
                    strokeWidth = 2f
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


suspend fun ModuleDynamic.Major.Article.drawGeneral(): Image {

    val paragraphStyle = ParagraphStyle().apply {
        maxLinesCount = 2
        ellipsis = "..."
        alignment = Alignment.LEFT
        textStyle = titleTextStyle
    }

    val paragraphWidth = cardContentRect.width - quality.cardPadding

    val titleParagraph = ParagraphBuilder(paragraphStyle, fonts).addText(title).build().layout(paragraphWidth)

    paragraphStyle.apply {
        maxLinesCount = 3
        textStyle = descTextStyle
    }

    val descParagraph = ParagraphBuilder(paragraphStyle, fonts).addText(desc).build().layout(paragraphWidth)

    val articleCoverHeight = cardContentRect.width * 0.29375f  // 封面比例 16:4.7
    val articleCardHeight = articleCoverHeight + titleParagraph.height + descParagraph.height + quality.cardPadding

    val articleCardRect = RRect.makeComplexXYWH(
        quality.cardPadding.toFloat(),
        quality.badgeHeight + 1f,
        cardContentRect.width,
        articleCardHeight,
        topTwoBadgeCardArc
    )

    return Surface.makeRasterN32Premul(
        cardRect.width.toInt(),
        articleCardHeight.toInt() + quality.badgeHeight + quality.cardPadding
    ).apply {
        canvas.apply {

            // 绘制卡片背景
            drawCard(articleCardRect)
            // 卡片阴影
            drawRectShadowAntiAlias(articleCardRect.inflate(1f), 5f, 5f, 15f, 0f, Color.makeARGB(30, 0, 0, 0))

            // 徽章
            if (imageConfig.badgeEnable) {
                drawBadge("专栏", font, Color.WHITE, Color.makeRGB(251, 114, 153), 255, articleCardRect, TOP_LEFT)
                drawBadge(
                    "cv$id",
                    font,
                    Color.WHITE,
                    Color.makeRGB(72, 199, 240),
                    255,
                    articleCardRect,
                    TOP_RIGHT
                )
            }
            // 封面
            val coverImg = getOrDownloadImage(covers[0], CacheType.IMAGES)
            val coverRRect = RRect.makeComplexXYWH(
                articleCardRect.left,
                articleCardRect.top,
                articleCardRect.width,
                articleCoverHeight,
                topTwoBadgeCardArc
            ).inflate(-1f) as RRect
            drawImageRRect(coverImg, coverRRect)

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

    val titleParagraph = ParagraphBuilder(paragraphStyle, fonts).addText(title).build().layout(paragraphWidth)

    paragraphStyle.apply {
        textStyle = descTextStyle
    }

    val descParagraph = ParagraphBuilder(paragraphStyle, fonts).addText(label).build().layout(paragraphWidth)

    val musicCardRect = RRect.makeComplexXYWH(
        quality.cardPadding.toFloat(),
        quality.badgeHeight + 1f,
        cardContentRect.width,
        musicCardHeight,
        topTwoBadgeCardArc
    )

    return Surface.makeRasterN32Premul(
        cardRect.width.toInt(),
        musicCardHeight.toInt() + quality.badgeHeight + quality.cardPadding
    ).apply {
        canvas.apply {

            // 绘制卡片背景
            drawCard(musicCardRect)
            // 卡片阴影
            drawRectShadowAntiAlias(musicCardRect.inflate(1f), 5f, 5f, 15f, 0f, Color.makeARGB(30, 0, 0, 0))

            // 徽章
            if (imageConfig.badgeEnable) {
                drawBadge("音乐", font, Color.WHITE, Color.makeRGB(251, 114, 153), 255, musicCardRect, TOP_LEFT)
                drawBadge(
                    "au$id",
                    font,
                    Color.WHITE,
                    Color.makeRGB(72, 199, 240),
                    255,
                    musicCardRect,
                    TOP_RIGHT
                )
            }
            // 封面
            val coverImg = getOrDownloadImage(cover, CacheType.IMAGES)
            val coverRRect = RRect.makeComplexXYWH(
                musicCardRect.left,
                musicCardRect.top,
                musicCardHeight,
                musicCardHeight,
                topTwoBadgeCardArc
            ).inflate(-1f) as RRect
            drawImageRRect(coverImg, coverRRect)

            titleParagraph.paint(
                this,
                musicCardHeight + quality.cardMargin * 2,
                (quality.badgeHeight + quality.cardMargin * 2).toFloat()
            )

            descParagraph.paint(
                this,
                musicCardHeight + quality.cardMargin * 2,
                quality.badgeHeight + quality.cardMargin * 2 + titleParagraph.height
            )

        }
    }.makeImageSnapshot()

}


fun Rect.textVertical(text: TextLine) =
    bottom - (height - text.capHeight) / 2


fun Canvas.drawCard(rrect: RRect, bgColor: Int = Color.WHITE, bgAlpha: Int = 160) {
    // alpha = 120
    drawRRect(rrect, Paint().apply {
        color = bgColor
        alpha = bgAlpha
        mode = PaintMode.FILL
        isAntiAlias = true
    })

    drawRRect(rrect, Paint().apply {
        color = Color.WHITE
        mode = PaintMode.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    })

}

sealed class RichText(
    val text: String
) {
    data class Text(
        val value: String
    ) : RichText(value)

    data class Emoji(
        val value: String
    ) : RichText(value)
}

fun Canvas.drawTextArea(text: String, rect: Rect, textX: Float, textY: Float, font: Font, paint: Paint): Point {
    var x = textX
    var y = textY

    val textNode = mutableListOf<RichText>()
    var index = 0

    emojiRegex.findAll(text).forEach {
        if (index != it.range.first) {
            textNode.add(RichText.Text(text.substring(index, it.range.first)))
        }
        textNode.add(RichText.Emoji(it.value))
        index = it.range.last + 1
    }

    if (index != text.length) {
        textNode.add(RichText.Text(text.substring(index, text.length)))
    }

    textNode.forEach {
        when (it) {
            is RichText.Text -> {
                for (point in it.value.codePoints()) {
                    val c = String(intArrayOf(point), 0, intArrayOf(point).size)
                    if (c == "\n") {
                        x = rect.left
                        y += quality.contentFontSize + quality.lineSpace
                    } else {
                        val charLine = TextLine.make(c, font)
                        if (x + charLine.width > rect.right) {
                            x = rect.left
                            y += quality.contentFontSize + quality.lineSpace
                        }
                        drawTextLine(charLine, x, y, paint)
                        x += charLine.width
                    }
                }
            }
            is RichText.Emoji -> {
                val tl = TextLine.make(it.value, font)
                if (x + tl.width > rect.right) {
                    x = rect.left
                    y += tl.height + quality.lineSpace
                }
                drawTextLine(tl, x, y, paint)
                x += tl.width
            }
        }
    }

    return Point(x, y)
}


suspend fun DynamicItem.makeCardBg(height: Int, block: (Canvas) -> Unit): Image {

    //val imageWidth = 800
    val imageHeight = 930
    //val imageHeight = 1330
    //val imageHeight = 2000
    val imageRect = Rect.makeXYWH(0f, 0f, quality.imageWidth.toFloat(), height.toFloat())
    val cardRect = Rect.makeLTRB(
        quality.cardMargin.toFloat(),
        quality.cardMargin.toFloat() + quality.badgeHeight,
        quality.imageWidth.toFloat() - quality.cardMargin.toFloat(),
        height - quality.cardMargin.toFloat()
    )

    return Surface.makeRasterN32Premul(imageRect.width.toInt(), height).apply {
        canvas.apply {

            drawRect(imageRect, Paint().apply {
                shader = Shader.makeLinearGradient(
                    Point(imageRect.left, imageRect.top),
                    Point(imageRect.right, imageRect.bottom),
//                    intArrayOf(
//                        0xFFD16BA5.toInt(), 0xFFC777B9.toInt(), 0xFFBA83CA.toInt(), 0xFFAA8FD8.toInt(),
//                        0xFF9A9AE1.toInt(), 0xFF8AA7EC.toInt(), 0xFF79B3F4.toInt(), 0xFF69BFF8.toInt(),
//                        0xFF52CFFE.toInt(), 0xFF41DFFF.toInt(), 0xFF46EEFA.toInt(), 0xFF5FFBF1.toInt()
//                    )

                    // H：色相   S：30   B：100
                    //intArrayOf(
                    //    0xFFffb2ff.toInt(),0xFFffb2e5.toInt(), 0xFFffb2cc.toInt(), 0xFFffb2b2.toInt(), 0xFFffccb2.toInt()
                    //)
                    generateLinearGradient(listOf(0xFFffb2cc.toInt(), 0xFFffb2b2.toInt()))
                )
            })
            val rrect = RRect.makeLTRB(
                cardRect.left,
                cardRect.top,
                cardRect.right,
                cardRect.bottom,
                0f,
                0f,
                quality.cardArc,
                quality.cardArc
            )


            //drawCard(rrect)
            //
            //drawRectShadowAntiAlias(rrect.inflate(1f), 6f, 6f, 25f, 0f, Color.makeARGB(70, 0, 0, 0))
            //
            //drawBadge("动态", font, Color.makeRGB(0, 203, 255), Color.WHITE, 120, cardRect, TOP_LEFT)
            //drawBadge(this@makeCardBg.idStr, font, Color.WHITE, Color.makeRGB(72, 199, 240), 255, cardRect, TOP_RIGHT)

            block(this)

            //drawImage(this@makeCardBg.modules.moduleAuthor.drawGeneral(this@makeCardBg.timeStr), cardRect.left, cardRect.top)

            //drawTextLine(TextLine.make("\uD80C\uDC9A\uD80C\uDE16\uD80C\uDDCB\uD80C\uDC9D\uD80C\uDF9B\uD80C\uDDF9", font), quality.cardMargin.toFloat(), quality.cardMargin+quality.cardPadding+120f, Paint())

            //drawImage(this@makeCardBg.modules.moduleDynamic.desc!!.drawGeneral(), quality.cardMargin.toFloat(), quality.cardMargin+quality.cardPadding+120f)

            //drawImage(dynamic.modules.moduleDynamic.major?.draw!!.drawGeneral(), quality.cardMargin.toFloat(), quality.cardMargin+quality.cardPadding+160f)

            //drawImage(dynamic.modules.moduleDynamic.major.archive!!.drawSmall(), quality.cardMargin.toFloat(), quality.cardMargin+quality.cardPadding+650f)

            //drawImage(dynamic.modules.moduleDynamic.major?.article!!.drawGeneral(), quality.cardMargin.toFloat(), quality.cardMargin+quality.cardPadding+160f)

            //drawImage(dynamic.modules.moduleDynamic.major?.music!!.drawGeneral(), quality.cardMargin.toFloat(), quality.cardMargin+quality.cardPadding+160f)

        }

    }.makeImageSnapshot()
}

suspend fun ModuleAuthor.drawForward(time: String): Image {
    return Surface.makeRasterN32Premul(
        quality.imageWidth - quality.cardMargin * 2,
        (quality.faceSize + quality.cardPadding).toInt()
    ).apply {
        canvas.apply {

            val faceSize = quality.faceSize * 0.6f
            drawAvatar(face, "", officialVerify?.type, faceSize, quality.verifyIconSize * 0.8f, true)

            val textLineName = TextLine.make(name, font.makeWithSize(quality.nameFontSize))
            val textLineTime = TextLine.make(time, font.makeWithSize(quality.subTitleFontSize))

            var x = faceSize + quality.cardPadding * 2.5f
            var y = ((faceSize - quality.nameFontSize) / 2) + quality.nameFontSize + quality.cardPadding

            drawTextLine(textLineName, x, y, Paint().apply { color = Color.makeRGB(251, 114, 153) })

            y -= (quality.nameFontSize - quality.subTitleFontSize) / 2
            x += textLineName.width + quality.cardPadding
            drawTextLine(textLineTime, x, y, Paint().apply { color = Color.makeRGB(156, 156, 156) })

        }
    }.makeImageSnapshot()
}

suspend fun ModuleAuthor.drawGeneral(time: String, link: String): Image {
    return Surface.makeRasterN32Premul(
        quality.imageWidth - quality.cardMargin * 2,
        (quality.faceSize + quality.cardPadding * 2f).toInt()
    ).apply surface@{
        canvas.apply {

            drawAvatar(face, pendant?.image, officialVerify?.type, quality.faceSize, quality.verifyIconSize)

            val textLineName = TextLine.make(name, font.makeWithSize(quality.nameFontSize))
            val textLineTime = TextLine.make(time, font.makeWithSize(quality.subTitleFontSize))

            var x = quality.faceSize + quality.cardPadding * 3f
            var y =
                ((quality.faceSize - (quality.nameFontSize + textLineTime.height)) / 2) + quality.nameFontSize + (quality.cardPadding * 1.2f)

            drawTextLine(textLineName, x, y, Paint().apply { color = Color.makeRGB(251, 114, 153) })

            y += textLineTime.height
            drawTextLine(textLineTime, x, y, Paint().apply { color = Color.makeRGB(156, 156, 156) })

            drawOrnament(decorate, link)
        }
    }.makeImageSnapshot()
}

suspend fun Canvas.drawOrnament(decorate: ModuleAuthor.Decorate?, link: String) {

    when (imageConfig.cardOrnament) {
        "fanCard" -> {
            if (decorate != null) {
                val fanImg = getOrDownloadImage(decorate.cardUrl, CacheType.USER)
                val srcFRect = Rect(0f, 0f, fanImg.width.toFloat(), fanImg.height.toFloat())

                val cardHeight = when (decorate.type) {
                    1, 2 -> quality.fanCardHeight * 0.6f
                    else -> quality.fanCardHeight
                }

                val cardWidth = fanImg.width * cardHeight / fanImg.height

                val y = ((quality.faceSize - cardHeight) / 2) + quality.cardPadding
                val tarFRect = Rect.makeXYWH(
                    cardContentRect.right - cardWidth - 20f,
                    y,
                    cardWidth,
                    cardHeight
                )

                drawImageRect(
                    fanImg,
                    srcFRect,
                    tarFRect,
                    FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST),
                    null,
                    true
                )

                if (decorate.type == 3 && decorate.fan?.numStr != "") {
                    val textLineFan = TextLine.make(decorate.fan?.numStr, fansCardFont)
                    drawTextLine(
                        textLineFan,
                        tarFRect.right - textLineFan.width * 2,
                        tarFRect.bottom - (cardHeight - fansCardFont.size) / 2,
                        Paint().apply { color = Color.makeRGB(decorate.fan!!.color) }
                    )
                }
            }
        }
        "qrCode" -> {
            //TODO
        }
    }
}


suspend fun Canvas.drawAvatar(
    face: String,
    pendant: String?,
    verifyType: Int?,
    faceSize: Float,
    verifyIconSize: Float,
    isForward: Boolean = false
) {

    val faceImg = getOrDownloadImage(face, CacheType.USER)

    val hasPendant = pendant != null && pendant != ""

    var tarFaceRect = RRect.makeXYWH(
        quality.cardPadding * if (isForward) 1.5f else 1.8f,
        quality.cardPadding * if (isForward) 1f else 1.2f,
        faceSize,
        faceSize,
        faceSize / 2
    )
    if (!hasPendant) {
        tarFaceRect = tarFaceRect.inflate(quality.noPendantFaceInflate) as RRect
        drawCircle(
            tarFaceRect.left + tarFaceRect.width / 2,
            tarFaceRect.top + tarFaceRect.width / 2,
            tarFaceRect.width / 2 + quality.noPendantFaceInflate / 2,
            Paint().apply { color = Color.WHITE; alpha = 160 })
    }

    drawImageRRect(faceImg, tarFaceRect)

    if (hasPendant) {
        val pendantImg = getOrDownloadImage(pendant!!, CacheType.USER)

        val srcPendantRect = Rect(0f, 0f, pendantImg.width.toFloat(), pendantImg.height.toFloat())
        val tarPendantRect = Rect.makeXYWH(
            tarFaceRect.left + tarFaceRect.width / 2 - quality.pendantSize / 2,
            tarFaceRect.top + tarFaceRect.height / 2 - quality.pendantSize / 2,
            quality.pendantSize, quality.pendantSize
        )
        drawImageRect(
            pendantImg,
            srcPendantRect,
            tarPendantRect,
            FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST),
            null,
            true
        )
    }

    val verifyIcon = when (verifyType) {
        0 -> "PERSONAL_OFFICIAL_VERIFY"
        1 -> "ORGANIZATION_OFFICIAL_VERIFY"
        else -> ""
    }

    if (verifyIcon != "") {
        //val svg = SVGDOM(Data.makeFromFileName("$resourcesPath/icon/$verifyIcon.svg"))
        //val svg = SVGDOM(Data.makeFromFileName(loadResource("icon/$verifyIcon.svg")))
        val svg = SVGDOM(Data.makeFromBytes(loadResourceBytes("icon/$verifyIcon.svg")))
        drawImage(
            svg.makeImage(verifyIconSize, verifyIconSize),
            tarFaceRect.right - verifyIconSize,
            tarFaceRect.bottom - verifyIconSize
        )
    }
}


fun Canvas.drawBadge(
    text: String,
    font: Font,
    fontColor: Int,
    bgColor: Int,
    bgAlpha: Int,
    cardRect: Rect,
    position: Position,
    icon: Image? = null
) {

    val textLine = TextLine.make(text, font)

    val badgeWidth = textLine.width + quality.badgePadding * 8 + (icon?.width?:0)

    val rrect = when (position) {
        TOP_LEFT -> RRect.makeXYWH(
            cardRect.left, cardRect.top - quality.badgeHeight, badgeWidth,
            quality.badgeHeight.toFloat(), quality.badgeArc, quality.badgeArc, 0f, 0f
        )
        TOP_RIGHT -> RRect.makeXYWH(
            cardRect.right - badgeWidth, cardRect.top - quality.badgeHeight, badgeWidth,
            quality.badgeHeight.toFloat(), quality.badgeArc, quality.badgeArc, 0f, 0f
        )
        BOTTOM_LEFT -> RRect.makeXYWH(
            cardRect.left, cardRect.bottom + quality.badgeHeight, badgeWidth,
            quality.badgeHeight.toFloat(), 0f, 0f, quality.badgeArc, quality.badgeArc
        )
        BOTTOM_RIGHT -> RRect.makeXYWH(
            cardRect.right - badgeWidth, cardRect.bottom + quality.badgeHeight, badgeWidth,
            quality.badgeHeight.toFloat(), 0f, 0f, quality.badgeArc, quality.badgeArc
        )
        else -> throw Exception("Bad Badge Position!")
    }

    drawCard(rrect, bgColor, bgAlpha)

    var x = rrect.left + quality.badgePadding * 4
    if (icon != null){
        x -= quality.badgePadding
        drawImage(icon, x, rrect.top + (quality.badgeHeight - icon.height)/2)
        x += icon.width + quality.badgePadding * 2
    }

    drawTextLine(
        textLine,
        x,
        rrect.bottom - (quality.badgeHeight - textLine.capHeight) / 2,
        Paint().apply { color = fontColor })

}

fun Canvas.drawLabelCard(
    text: String,
    font: Font,
    fontColor: Int,
    bgColor: Int,
    bgAlpha: Int,
    x: Float,
    y: Float
): Float {
    val textLine = TextLine.make(text, font)

    val rrect = RRect.makeXYWH(
        x, y - textLine.capHeight,
        textLine.width + quality.badgePadding * 4,
        quality.badgePadding * 2f + textLine.capHeight,
        quality.badgeArc
    )

    drawRRect(rrect, Paint().apply {
        color = bgColor
        alpha = bgAlpha
        mode = PaintMode.FILL
        isAntiAlias = true
    })

    drawTextLine(
        textLine,
        rrect.left + quality.badgePadding * 2,
        rrect.bottom - quality.badgePadding,
        Paint().apply { color = fontColor })

    return rrect.width
}

