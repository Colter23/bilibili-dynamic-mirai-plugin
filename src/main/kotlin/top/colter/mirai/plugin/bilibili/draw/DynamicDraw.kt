package top.colter.mirai.plugin.bilibili.draw

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skia.svg.SVGDOM
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.data.DynamicItem
import top.colter.mirai.plugin.bilibili.data.DynamicType.DYNAMIC_TYPE_FORWARD
import top.colter.mirai.plugin.bilibili.data.ModuleAuthor
import top.colter.mirai.plugin.bilibili.data.ModuleDispute
import top.colter.mirai.plugin.bilibili.data.ModuleDynamic
import top.colter.mirai.plugin.bilibili.draw.Position.*
import top.colter.mirai.plugin.bilibili.utils.FontUtils.fonts
import top.colter.mirai.plugin.bilibili.utils.FontUtils.loadTypeface
import top.colter.mirai.plugin.bilibili.utils.formatTime
import top.colter.mirai.plugin.bilibili.utils.plusOrNull
import java.io.File
import kotlin.math.ceil

internal val logger by BiliBiliDynamic::logger

private val quality: Quality by lazy {
//    Quality.level(imageConfig.quality)
    Quality.level(1)
}

private val cardRect: Rect by lazy {
    Rect.makeLTRB(quality.cardMargin.toFloat(), 0f, quality.imageWidth - quality.cardMargin.toFloat(), 0f)
}

private val cardContentRect: Rect by lazy {
    cardRect.inflate(-1f * quality.cardPadding)
}

private val mainTypeface: Typeface by lazy {
    loadTypeface("E:/Desktop/资源/字体/HarmonyOS Sans/HarmonyOS_Sans_SC/HarmonyOS_Sans_SC_Medium.ttf")
}

private val font: Font by lazy {
    Font(mainTypeface, quality.contentFontSize)
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

val topTwoBadgeCardArc = floatArrayOf(0f, 0f, quality.cardArc, quality.cardArc)

val linkPaint = Paint().apply {
    color = Color.makeRGB(23, 139, 207)
    isAntiAlias = true
}
val generalPaint = Paint().apply {
    color = Color.makeRGB(34, 34, 34)
    isAntiAlias = true
}



enum class Position{
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

suspend fun DynamicItem.makeDrawDynamic(): String{

    val dynamic = drawDynamic()
    //val forward = orig?.drawDynamic()

    val path = "dynamic.png"
    makeCardBg(path,  dynamic.height){
        it.drawImage(dynamic, 0f, 0f)
    }
    return path
}

suspend fun DynamicItem.drawDynamic(isForward: Boolean = false): Image{

    val imgList = modules.makeGeneral(formatTime, isForward).plusOrNull(
        orig?.drawDynamic(type == DYNAMIC_TYPE_FORWARD)
    )

    val height = imgList.sumOf {
        if (it.width > cardRect.width){
            (cardRect.width * it.height / it.width + 10).toInt()
        }else{
            it.height + 10
        }
    }

    val margin = if (isForward) quality.cardPadding * 2 else quality.cardMargin * 2

    return Surface.makeRasterN32Premul(
        (cardRect.width + margin).toInt(),
        height + quality.badgeHeight + margin
    ).apply {
        canvas.apply {

            val rrect = RRect.makeXYWH(
                margin / 2f,
                quality.badgeHeight + margin / 2f,
                cardRect.width,
                height.toFloat(),
                0f,0f,quality.cardArc,quality.cardArc)

            drawCard(rrect)

            if (isForward){
                drawRectShadowAntiAlias(rrect.inflate(1f), 5f, 5f, 15f, 0f, Color.makeARGB(30, 0, 0, 0))
            }else {
                drawRectShadowAntiAlias(rrect.inflate(1f), 6f, 6f, 25f, 0f, Color.makeARGB(70, 0, 0, 0))
            }



            drawBadge("动态", font, Color.makeRGB(0, 203, 255), Color.WHITE, 120, rrect, TOP_LEFT)
            drawBadge(this@drawDynamic.idStr, font, Color.WHITE, Color.makeRGB(72, 199, 240), 255, rrect, TOP_RIGHT)

            var top = quality.cardMargin + quality.badgeHeight.toFloat()
            for (img in imgList){

                drawScaleWidthImage(img, cardRect.width, quality.cardMargin.toFloat(), top)
                //drawScaleWidthImageOutline(img, cardRect.width, quality.cardMargin.toFloat(), top, isForward)

                top += img.height + 10
            }

        }
    }.makeImageSnapshot()


}


object DynamicDraw {

    suspend fun makeDynamic(dynamic: DynamicItem){

        val imgList = dynamic.modules.moduleDynamic.makeGeneral()
        val result = ArrayList<Image>(imgList.size + 1)
        result.add(dynamic.modules.moduleAuthor.drawGeneral(dynamic.formatTime))
        result.addAll(imgList)

        val height = result.sumOf { it.height + 10 }

        //val path = dynamicCachePath.resolve("${dynamic.modules.moduleAuthor.mid}/${dynamic.idStr}.png").pathString
        val path = "dynamic.png"

        dynamic.makeCardBg(
            path,
            height + quality.cardMargin + quality.badgeHeight + quality.cardPadding
        ){
            var top = quality.cardMargin + quality.badgeHeight.toFloat()
            for (img in result){
                it.drawImage(img, quality.cardMargin.toFloat(), top)
                top += img.height + 10
            }
        }

    }

}

suspend fun DynamicItem.Modules.makeGeneral(time: String, isForward: Boolean = false): List<Image>{
    return mutableListOf<Image>().apply {
        add(if (isForward) moduleAuthor.drawForward(time) else moduleAuthor.drawGeneral(time))
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

fun ModuleDynamic.Additional.makeGeneral(): Image? {
    return drawAdditionalCard()
    //return when (type){
    //    "ADDITIONAL_TYPE_COMMON" -> {null}
    //    "ADDITIONAL_TYPE_RESERVE" -> {null}
    //    "ADDITIONAL_TYPE_VOTE" -> {null}
    //    "ADDITIONAL_TYPE_UGC" -> {null}
    //    "ADDITIONAL_TYPE_GOODS" -> {null}
    //    else -> {null}
    //}
}

suspend fun ModuleDynamic.Major.makeGeneral(isForward: Boolean = false): Image? {
    return when (type){
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
        "MAJOR_TYPE_LIVE" -> {null}
        "MAJOR_TYPE_LIVE_RCMD" -> {null}
        "MAJOR_TYPE_PGC" -> {null}
        "MAJOR_TYPE_COMMON" -> {null}
        "MAJOR_TYPE_NONE" -> {null}
        else -> {null}
    }
}

fun ModuleDispute.drawGeneral(): Image {
    return Surface.makeRasterN32Premul(10,10).apply {
        canvas.apply {



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
                val svg = SVGDOM(Data.makeFromFileName("src/main/resources/icon/TOPIC.svg"))
                val iconSize = quality.contentFontSize
                drawImage(svg.makeImage(iconSize,iconSize), x, y - quality.contentFontSize * 0.8f)
                x += iconSize
            }catch (e: Exception){
                logger.warning("未找到类型为 TOPIC 的图标")
            }

            drawTextArea(this@drawGeneral.name, textCardRect, x, y, font, linkPaint)
        }
    }.makeImageSnapshot()

}

suspend fun ModuleDynamic.Desc.drawGeneral(): Image{
    val paragraphStyle = ParagraphStyle().apply {
        alignment = Alignment.LEFT
        textStyle = titleTextStyle
    }

    val textParagraph = ParagraphBuilder(paragraphStyle, fonts).addText(text).build().layout(cardContentRect.width)

    //val lineCount = (TextLine.make(text, font).width / cardContentRect.width).toInt() + 1

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
                when (it.type){
                    "RICH_TEXT_NODE_TYPE_TEXT" -> {
                        val point = drawTextArea(it.text, textCardRect, x, y, font, generalPaint)
                        x = point.x
                        y = point.y
                    }
                    "RICH_TEXT_NODE_TYPE_EMOJI" -> {
                        val img = Image.makeFromEncoded(HttpClient(OkHttp).get<ByteArray>(it.emoji?.iconUrl!!))

                        val emojiSize = TextLine.make("🙂", font).height

                        if (x + emojiSize > textCardRect.right){
                            x = textCardRect.left
                            y += emojiSize + quality.lineSpace
                        }
                        val srcRect = Rect.makeXYWH(0f, 0f, img.width.toFloat(), img.height.toFloat())
                        val tarRect = Rect.makeXYWH(x, y - emojiSize * 0.8f, emojiSize, emojiSize)
                        drawImageRect(img, srcRect, tarRect, FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST), null, true)
                        x += emojiSize
                    }
                    "RICH_TEXT_NODE_TYPE_WEB",
                    "RICH_TEXT_NODE_TYPE_VOTE",
                    "RICH_TEXT_NODE_TYPE_LOTTERY",
                    "RICH_TEXT_NODE_TYPE_BV" -> {
                        try {
                            //val svg = SVGDOM(Data.makeFromBytes(BiliBiliDynamic.getResourceAsStream("src/main/resources/icon/RICH_TEXT_NODE_TYPE_WEB.svg")!!.readBytes()))
                            val svg = SVGDOM(Data.makeFromFileName("src/main/resources/icon/${it.type}.svg"))
                            val iconSize = quality.contentFontSize
                            drawImage(svg.makeImage(iconSize,iconSize), x, y - quality.contentFontSize * 0.9f)
                            x += iconSize
                        }catch (e: Exception){
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

fun ModuleDynamic.Additional.drawAdditionalCard(): Image{

    val additionalCardRect = RRect.makeXYWH(
        quality.cardPadding.toFloat(),
        quality.subFontSize + quality.cardPadding + 1f,
        cardContentRect.width,
        quality.additionalCardHeight.toFloat(),
        quality.cardArc
    )

    return Surface.makeRasterN32Premul(cardRect.width.toInt(),
        (quality.additionalCardHeight + quality.subFontSize + quality.cardPadding * 2f).toInt()
    ).apply {
        canvas.apply {
            drawCard(additionalCardRect)
            drawRectShadowAntiAlias(additionalCardRect.inflate(1f), 5f, 5f, 15f, 0f, Color.makeARGB(30, 0, 0, 0))

            val img = Image.makeFromEncoded(File("D:/Desktop/bilibili动态/625896a6d3a355f3925b8da02f30917e986822b0.jpg").readBytes())
            val imgRect = RRect.makeXYWH(
                quality.cardPadding.toFloat(),
                quality.subFontSize + quality.cardPadding + 1f,
                quality.additionalCardHeight.toFloat(),
                quality.additionalCardHeight.toFloat(),
                quality.cardArc
            ).inflate(-1f) as RRect
            drawImageRRect(img, imgRect)

            val labelTextLine = TextLine.make("直播预约", font.makeWithSize(quality.subFontSize))
            drawTextLine(labelTextLine, additionalCardRect.left + 8, quality.subFontSize, Paint().apply {
                color = Color.BLACK
                alpha = 130
            })

            var y = quality.subFontSize + quality.cardPadding * 2.8f
            val titleTextLine = TextLine.make("直播预约：玥玥春日新衣发布", font)
            drawTextLine(titleTextLine, imgRect.right + quality.cardPadding, y, Paint())

            y += quality.cardPadding * 1.2f

            val desc1TextLine = TextLine.make("04-17 20:00 直播 2623人预约", font.makeWithSize(quality.subFontSize))
            drawTextLine(desc1TextLine, imgRect.right + quality.cardPadding, y, Paint().apply {
                color = Color.BLACK
                alpha = 130
            })

            y += quality.cardPadding * 1.2f

            val desc2TextLine = TextLine.make("预约有奖：新衣立牌*3份、玥玥钥匙扣*3份", font.makeWithSize(quality.subFontSize))
            drawTextLine(desc2TextLine, imgRect.right + quality.cardPadding, y, Paint().apply {
                color = Color.makeRGB(0, 203, 255)
            })

            val buttonRect = RRect.makeXYWH(
                additionalCardRect.right - quality.cardPadding - 90f,
                quality.subFontSize + quality.cardPadding + quality.cardPadding * 1.5f,
                90f,
                35f,
                5f
            )
            drawRRect(buttonRect, Paint().apply {
                color = Color.makeRGB(0, 203, 255)
            })

            val buttonTextLine = TextLine.make("去观看", font.makeWithSize(quality.subFontSize))
            drawTextLine(buttonTextLine, buttonRect.left + quality.cardPadding, buttonRect.bottom - 11, Paint().apply {
                color = Color.WHITE
            })

        }
    }.makeImageSnapshot()


}


fun ModuleDynamic.Major.Archive.drawGeneral(): Image {

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

    return Surface.makeRasterN32Premul(cardRect.width.toInt(), videoCardHeight.toInt() + quality.badgeHeight + quality.cardPadding).apply {
        canvas.apply {

            // 绘制卡片背景
            drawCard(videoCardRect)
            // 卡片阴影
            drawRectShadowAntiAlias(videoCardRect.inflate(1f), 5f, 5f, 15f, 0f, Color.makeARGB(30, 0, 0, 0))

            // 徽章
            drawBadge(this@drawGeneral.badge.text, font, Color.WHITE, Color.makeRGB(251, 114, 153), 255, videoCardRect, TOP_LEFT)
            drawBadge("av${this@drawGeneral.aid}  |  ${this@drawGeneral.bvid}", font, Color.WHITE, Color.makeRGB(72, 199, 240), 255, videoCardRect, TOP_RIGHT)

            // 封面
            val cover = Image.makeFromEncoded(File("D:/Desktop/bilibili动态/f1d0fb9dbee0066997546a1951f90aeccc95808c.jpg").readBytes())
            val coverRRect = RRect.makeComplexXYWH(videoCardRect.left , videoCardRect.top, videoCardRect.width, videoCoverHeight, topTwoBadgeCardArc).inflate(-1f) as RRect
            drawImageRRect(cover, coverRRect)

            // 封面遮罩
            val coverMaskRRect = RRect.makeComplexLTRB(coverRRect.left, coverRRect.bottom - videoCoverHeight * 0.2f, coverRRect.right, coverRRect.bottom, topTwoBadgeCardArc)
            drawRRect(coverMaskRRect, Paint().apply {
                color = Color.BLACK
                alpha = 120
                shader = Shader.makeLinearGradient(
                    Point(coverMaskRRect.left, coverMaskRRect.bottom),
                    Point(coverMaskRRect.left, coverMaskRRect.top),
                    intArrayOf(0xFF000000.toInt(), 0x00000000.toInt())
                )
            })

            val durationText = TextLine.make(this@drawGeneral.durationText, font)
            val playInfo = TextLine.make("${this@drawGeneral.stat.play}观看 ${this@drawGeneral.stat.danmaku}弹幕", font.makeWithSize(22f))

            val durationRRect = RRect.makeXYWH(coverMaskRRect.left + quality.cardPadding * 1.3f, coverRRect.bottom - quality.badgeHeight - quality.cardPadding, durationText.width + quality.badgePadding * 4, quality.badgeHeight.toFloat(), quality.badgeArc)
            drawRRect(durationRRect, Paint().apply {
                color = Color.BLACK
                alpha = 130
            })

            drawTextLine(durationText, durationRRect.left + quality.badgePadding * 2, durationRRect.textVertical(durationText), Paint().apply {
                color = Color.WHITE
            })

            drawTextLine(playInfo, durationRRect.right + quality.badgePadding * 2, durationRRect.textVertical(playInfo), Paint().apply {
                color = Color.WHITE
            })

            titleParagraph.paint(this, quality.cardPadding * 1.5f, quality.badgeHeight + videoCoverHeight + quality.cardPadding / 2)

            descParagraph.paint(this, quality.cardPadding * 1.5f, quality.badgeHeight + videoCoverHeight + quality.cardPadding / 2 + titleParagraph.height)

        }
    }.makeImageSnapshot()
}


fun ModuleDynamic.Major.Archive.drawSmall(): Image {

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
        maxLinesCount = 2
        textStyle = descTextStyle
    }

    val descParagraph = ParagraphBuilder(paragraphStyle, fonts).addText(desc).build().layout(paragraphWidth)

    //val videoCoverHeight = cardContentRect.width * 0.625f  // 封面比例 16:10
    //val videoCardHeight = videoCoverHeight + titleParagraph.height + descParagraph.height + quality.cardPadding * 1.5f

    val videoCardRect = RRect.makeComplexXYWH(
        quality.cardPadding.toFloat(),
        quality.badgeHeight + 1f,
        cardContentRect.width,
        quality.smallCardHeight.toFloat(),
        topTwoBadgeCardArc
    )

    return Surface.makeRasterN32Premul(cardRect.width.toInt(), quality.smallCardHeight + quality.badgeHeight + quality.cardPadding).apply {
        canvas.apply {

            // 绘制卡片背景
            drawCard(videoCardRect)
            // 卡片阴影
            drawRectShadowAntiAlias(videoCardRect.inflate(1f), 5f, 5f, 15f, 0f, Color.makeARGB(30, 0, 0, 0))

            // 徽章
            drawBadge(this@drawSmall.badge.text, font, Color.WHITE, Color.makeRGB(251, 114, 153), 255, videoCardRect, TOP_LEFT)
            drawBadge("av${this@drawSmall.aid}  |  ${this@drawSmall.bvid}", font, Color.WHITE, Color.makeRGB(72, 199, 240), 255, videoCardRect, TOP_RIGHT)

            // 封面
            val cover = Image.makeFromEncoded(File("D:/Desktop/bilibili动态/f1d0fb9dbee0066997546a1951f90aeccc95808c.jpg").readBytes())
            val coverRRect = RRect.makeComplexXYWH(videoCardRect.left , videoCardRect.top, videoCoverWidth,
                quality.smallCardHeight.toFloat(), topTwoBadgeCardArc).inflate(-1f) as RRect
            drawImageRRect(cover, coverRRect)

            titleParagraph.paint(this, quality.cardPadding * 1.5f + videoCoverWidth, quality.badgeHeight + quality.cardPadding / 2f)

            descParagraph.paint(this, quality.cardPadding * 1.5f + videoCoverWidth, quality.badgeHeight + quality.cardPadding / 2f + titleParagraph.height)


            //val typeText = TextLine.make("投稿视频", font)
            //val durationText = TextLine.make(this@drawSmall.durationText, font)
            //val playInfoText = TextLine.make("${this@drawSmall.stat.play}观看 ${this@drawSmall.stat.danmaku}弹幕", font.makeWithSize(
            //    quality.subFontSize))

            //val typeRRect = RRect.makeXYWH(
            //    coverRRect.right + quality.cardPadding * 1.3f,
            //    titleParagraph.height + descParagraph.height + quality.badgeHeight + quality.cardPadding,
            //    typeText.width + quality.badgePadding * 4,
            //    quality.badgeHeight.toFloat(),
            //    quality.badgeArc
            //)
            var x = coverRRect.right + quality.cardPadding * 1.3f
            var y = videoCardRect.bottom - quality.cardPadding * 1.3f
            x += drawLabelCard("投稿视频", font.makeWithSize(quality.subFontSize), Color.WHITE, Color.makeRGB(251, 114, 153), 255, x, y) + quality.cardPadding

            //drawRRect(typeRRect, Paint().apply {
            //    color = Color.BLACK
            //    alpha = 130
            //})
            //drawTextLine(typeText, typeRRect.left + quality.badgePadding * 2, typeRRect.textVertical(typeText), Paint().apply {
            //    color = Color.WHITE
            //})


            //val durationRRect = typeRRect.offsetR(typeRRect.width + quality.badgePadding * 4, 0f)

            x += drawLabelCard(this@drawSmall.durationText, font.makeWithSize(quality.subFontSize), Color.WHITE, Color.BLACK, 130,
                x, y) + quality.cardPadding
            //drawRRect(durationRRect, Paint().apply {
            //    color = Color.BLACK
            //    alpha = 130
            //})
            //drawTextLine(durationText, durationRRect.left + quality.badgePadding * 2, durationRRect.textVertical(durationText), Paint().apply {
            //    color = Color.WHITE
            //})


            //val playInfoRRect = typeRRect.offsetR(durationRRect.width + quality.badgePadding * 4, 0f)
            x += drawLabelCard("${this@drawSmall.stat.play}观看 ${this@drawSmall.stat.danmaku}弹幕", font.makeWithSize(quality.subFontSize), Color.WHITE, Color.makeRGB(26, 198, 255), 255,
                x, y
            ) + quality.cardPadding
            //drawRRect(playInfoRRect, Paint().apply {
            //    color = Color.BLACK
            //    alpha = 130
            //})
            //drawTextLine(playInfoText, playInfoRRect.right + quality.badgePadding * 2, playInfoRRect.textVertical(playInfoText), Paint().apply {
            //    color = Color.WHITE
            //})


        }
    }.makeImageSnapshot()
}

suspend fun ModuleDynamic.Major.Draw.drawGeneral(): Image {

    var drawItemWidth = 0f
    var drawItemHeight = 0f
    var drawItemSpace = quality.drawSpace * 2
    var drawItemNum = 0

    when (items.size){
        1 -> {
            drawItemWidth = if (items[0].width > cardContentRect.width / 2){
                cardContentRect.width
            } else {
                items[0].width * 2f
            }
            val drawHeight = items[0].height.toFloat() / items[0].width.toFloat() * drawItemWidth
            drawItemHeight = if (drawHeight > drawItemWidth * 2){
                drawItemWidth * 2
            }else{
                drawHeight
            }
        }
        2, 4 -> {
            drawItemWidth = (cardContentRect.width - quality.drawSpace) / 2
            drawItemHeight = drawItemWidth
            if (items.size >= 3){
                drawItemSpace += quality.drawSpace
            }
            drawItemNum = 2
        }
        3, in 5..9 -> {
            drawItemWidth = (cardContentRect.width - quality.drawSpace * 2) / 3
            drawItemHeight = drawItemWidth
            drawItemSpace += if (items.size <= 6){
                quality.drawSpace
            }else{
                quality.drawSpace * 2
            }
            drawItemNum = 3
        }
    }

    return Surface.makeRasterN32Premul(cardRect.width.toInt(), (drawItemHeight * items.size / drawItemNum + drawItemSpace).toInt()).apply {
        canvas.apply {

            var x = quality.cardPadding.toFloat()
            var y = quality.drawSpace.toFloat()

            items.forEachIndexed { index, drawItem ->

                val img = Image.makeFromEncoded(HttpClient(OkHttp).get<ByteArray>(drawItem.src))

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

                if ((index + 1) % drawItemNum == 0){
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

    return Surface.makeRasterN32Premul(cardRect.width.toInt(), articleCardHeight.toInt() + quality.badgeHeight + quality.cardPadding).apply {
        canvas.apply {

            // 绘制卡片背景
            drawCard(articleCardRect)
            // 卡片阴影
            drawRectShadowAntiAlias(articleCardRect.inflate(1f), 5f, 5f, 15f, 0f, Color.makeARGB(30, 0, 0, 0))

            // 徽章
            drawBadge("专栏", font, Color.WHITE, Color.makeRGB(251, 114, 153), 255, articleCardRect, TOP_LEFT)
            drawBadge("cv${this@drawGeneral.id}", font, Color.WHITE, Color.makeRGB(72, 199, 240), 255, articleCardRect, TOP_RIGHT)

            // 封面
            val cover = Image.makeFromEncoded(HttpClient(OkHttp).get<ByteArray>(covers[0]))
            val coverRRect = RRect.makeComplexXYWH(articleCardRect.left , articleCardRect.top, articleCardRect.width, articleCoverHeight, topTwoBadgeCardArc).inflate(-1f) as RRect
            drawImageRRect(cover, coverRRect)

            titleParagraph.paint(this, quality.cardPadding * 1.5f, quality.badgeHeight + articleCoverHeight + quality.cardPadding / 2)

            descParagraph.paint(this, quality.cardPadding * 1.5f, quality.badgeHeight + articleCoverHeight + quality.cardPadding / 2 + titleParagraph.height)

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

    return Surface.makeRasterN32Premul(cardRect.width.toInt(), musicCardHeight.toInt() + quality.badgeHeight + quality.cardPadding).apply {
        canvas.apply {

            // 绘制卡片背景
            drawCard(musicCardRect)
            // 卡片阴影
            drawRectShadowAntiAlias(musicCardRect.inflate(1f), 5f, 5f, 15f, 0f, Color.makeARGB(30, 0, 0, 0))

            // 徽章
            drawBadge("音乐", font, Color.WHITE, Color.makeRGB(251, 114, 153), 255, musicCardRect, TOP_LEFT)
            drawBadge("au${this@drawGeneral.id}", font, Color.WHITE, Color.makeRGB(72, 199, 240), 255, musicCardRect, TOP_RIGHT)

            // 封面
            val cover = Image.makeFromEncoded(HttpClient(OkHttp).get<ByteArray>(cover))
            val coverRRect = RRect.makeComplexXYWH(musicCardRect.left , musicCardRect.top, musicCardHeight, musicCardHeight, topTwoBadgeCardArc).inflate(-1f) as RRect
            drawImageRRect(cover, coverRRect)

            titleParagraph.paint(this, musicCardHeight + quality.cardMargin * 2, (quality.badgeHeight + quality.cardMargin * 2).toFloat())

            descParagraph.paint(this, musicCardHeight + quality.cardMargin * 2, quality.badgeHeight + quality.cardMargin * 2 + titleParagraph.height)

        }
    }.makeImageSnapshot()

}


fun Rect.textVertical(text: TextLine) =
    bottom - (height - text.capHeight) / 2


fun Canvas.drawCard(rrect: RRect, bgColor: Int = Color.WHITE, bgAlpha: Int = 160){
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
    ):RichText(value)

    data class Emoji(
        val value: String
    ):RichText(value)
}

fun Canvas.drawTextArea(text: String,rect: Rect, textX: Float, textY: Float, font: Font, paint: Paint ): Point {
    var x = textX
    var y = textY

    val textNode = mutableListOf<RichText>()
    var index = 0

    emojiRegex.findAll(text).forEach {
        if (index != it.range.first){
            textNode.add(RichText.Text(text.substring(index, it.range.first)))
        }
        textNode.add(RichText.Emoji(it.value))
        index = it.range.last + 1
    }

    if (index != text.length){
        textNode.add(RichText.Text(text.substring(index, text.length)))
    }

    textNode.forEach {
        when (it){
            is RichText.Text -> {
                for (point in it.value.codePoints()){
                    val c = String(intArrayOf(point), 0, intArrayOf(point).size)
                    if (c == "\n"){
                        x = rect.left
                        y += quality.contentFontSize + quality.lineSpace
                    }else{
                        val charLine = TextLine.make(c, font)
                        if (x + charLine.width > rect.right){
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
                if (x + tl.width > rect.right){
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


suspend fun DynamicItem.makeCardBg(path: String, height: Int, block: (Canvas) -> Unit){

    //val imageWidth = 800
    val imageHeight = 930
    //val imageHeight = 1330
    //val imageHeight = 2000
    val imageRect = Rect.makeXYWH(0f,0f,quality.imageWidth.toFloat(), height.toFloat())
    val cardRect = Rect.makeLTRB(
        quality.cardMargin.toFloat(),
        quality.cardMargin.toFloat() + quality.badgeHeight,
        quality.imageWidth.toFloat() - quality.cardMargin.toFloat(),
        height - quality.cardMargin.toFloat()
    )

    Surface.makeRasterN32Premul(imageRect.width.toInt(), height).apply {
        canvas.apply {

            drawRect(imageRect, Paint().apply {
                shader = Shader.makeLinearGradient(
                    Point(imageRect.left,imageRect.top),
                    Point(imageRect.right,imageRect.bottom),
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
            val rrect = RRect.makeLTRB(cardRect.left, cardRect.top, cardRect.right, cardRect.bottom, 0f,0f,quality.cardArc,quality.cardArc)


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

    }.saveImage(path)
}


fun generateLinearGradient(colors: List<Int>): IntArray{
    return if (colors.size == 1){
        val hsb = rgb2hsb(Color.getR(colors[0]), Color.getG(colors[0]), Color.getB(colors[0]))
        hsb[1] = 0.3f
        hsb[2] = 1f
        val linearLayerCount = 3
        val linearLayerStep = 40
        val llc = if (linearLayerCount % 2 == 0) linearLayerCount + 1 else linearLayerCount
        val ia = IntArray(llc)
        hsb[0] = (hsb[0] + linearLayerCount / 2 * linearLayerStep) % 360
        repeat(llc) {
            val c = hsb2rgb(hsb[0], hsb[1], hsb[2])
            ia[it] = Color.makeRGB(c[0], c[1], c[2])
            hsb[0] = if (hsb[0] - linearLayerStep < 0) hsb[0] + 360 - linearLayerStep else hsb[0] - linearLayerStep
        }
        ia
    }else{
        val llc = colors.size
        val ia = IntArray(llc)
        repeat(llc) {
            val hsb = rgb2hsb(Color.getR(colors[it]), Color.getG(colors[it]), Color.getB(colors[it]))
            hsb[1] = 0.3f
            hsb[2] = 1f
            val c = hsb2rgb(hsb[0], hsb[1], hsb[2])
            ia[it] = Color.makeRGB(c[0], c[1], c[2])
        }
        ia
    }
}

fun ModuleAuthor.drawForward(time: String): Image {
    return Surface.makeRasterN32Premul(quality.imageWidth-quality.cardMargin*2,
        (quality.pendantSize ).toInt()
    ).apply{
        canvas.apply {

            drawAvatar(this@drawForward.face, "", this@drawForward.officialVerify?.type, quality.faceSize * 0.7f, quality.verifyIconSize * 0.9f)

            val textLineName = TextLine.make(this@drawForward.name, font.makeWithSize(quality.nameFontSize))
            drawTextLine(textLineName, 100f, 60f,Paint().apply { color = Color.makeRGB(251, 114, 153) })

            val textLineTime = TextLine.make(time, font.makeWithSize(quality.subTitleFontSize))
            drawTextLine(textLineTime, textLineName.width + 120f, 60f, Paint().apply { color = Color.makeRGB(156, 156, 156) })

        }
    }.makeImageSnapshot()
}

fun ModuleAuthor.drawGeneral(time: String): Image {
    return Surface.makeRasterN32Premul(quality.imageWidth-quality.cardMargin*2,
        (quality.pendantSize ).toInt()
    ).apply surface@{
        canvas.apply {

            drawAvatar(this@drawGeneral.face, pendant?.image, officialVerify?.type, quality.faceSize, quality.verifyIconSize)

            val hasPendant = pendant != null && pendant.image != ""

            val textLineName = TextLine.make(name, font.makeWithSize(quality.nameFontSize))

            var x = quality.faceSize + quality.cardPadding * if (hasPendant) 3.5f else 3f
            var y = textLineName.height / 2 + quality.cardPadding * if (hasPendant) 2f else 1.5f
            println(x)
            println(y)

            // 150 60     150 90

            drawTextLine(textLineName, x, y, Paint().apply { color = Color.makeRGB(251, 114, 153) })


            val textLineTime = TextLine.make(time, font.makeWithSize(quality.subTitleFontSize))

            y += textLineTime.height
            println(y)
            drawTextLine(textLineTime, x, y, Paint().apply { color = Color.makeRGB(156, 156, 156) })


            val fan = Image.makeFromEncoded(File("D:/Desktop/bilibili动态/d73dd984b8b55e56ac6bdad583a754d147ebd0fa.png").readBytes())
            //val fan = Image.makeFromEncoded(File("D:/Desktop/bilibili动态/5ebada630d1897124a9f33dd2d5c9566d02fcc72.png").readBytes())
            val srcFRect = Rect(0f,0f,fan.width.toFloat(),fan.height.toFloat())

            // 100 300
            val tarWidth = 300
            val tarFRect = Rect.makeXYWH(width-tarWidth-20f, 20f,
                tarWidth.toFloat(),
                (tarWidth * fan.height / fan.width).toFloat()
            )
            drawImageRect(fan, srcFRect, tarFRect, FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST), null, true)


            val typeface1 = Typeface.makeFromFile("D:/Desktop/bilibili动态/fansCard.ttf")
            val font1 = Font(typeface1, 25f)
            val textLineFan = TextLine.make("001107",font1.makeWithSize(20f))
            drawTextLine(textLineFan, width-200f, 70f, Paint().apply { color = Color.makeRGB(213, 122, 255) })

        }
    }.makeImageSnapshot()
}


fun Canvas.drawAvatar(face: String, pendant: String?, verifyType: Int?, faceSize: Float, verifyIconSize: Float) {
    //val avatarSize = (quality.cardPadding * 4 + quality.faceSize).toInt()
    //return Surface.makeRasterN32Premul(avatarSize, avatarSize).apply {
    //    canvas.apply {

    val faceImg = Image.makeFromEncoded(File("D:/Desktop/bilibili动态/625896a6d3a355f3925b8da02f30917e986822b0.jpg").readBytes())

    val hasPendant = pendant != null && pendant != ""

    var tarFaceRect = RRect.makeXYWH(
        quality.cardPadding * if (hasPendant) 2f else 1.5f,
        quality.cardPadding * if (hasPendant) 1.5f else 1f,
        faceSize,
        faceSize,
        faceSize/2
    )
     if (!hasPendant){
         tarFaceRect = tarFaceRect.inflate(quality.noPendantFaceInflate) as RRect
         drawCircle(tarFaceRect.left+tarFaceRect.width/2,tarFaceRect.top+tarFaceRect.width/2,tarFaceRect.width/2+2,Paint().apply { color = Color.WHITE; alpha = 160 })
    }

    drawImageRRect(faceImg, tarFaceRect)

    if (hasPendant){
        //val pand = Image.makeFromEncoded(File("D:/Desktop/d8f6dec3bd0bcdb09fcfd99fba620aa7da91dd8e.png").readBytes())
        //val pand = Image.makeFromEncoded(File("D:/Desktop/1cdf174c75dd6493f3c8f0797e972b69e3293870.png").readBytes())
        val pendantImg = Image.makeFromEncoded(File("D:/Desktop/bilibili动态/5c004d76ba9f57c00d7972de411c3989efebe120.png").readBytes())

        val srcPendantRect = Rect(0f,0f,pendantImg.width.toFloat(),pendantImg.height.toFloat())
        val tarPendantRect = Rect.makeXYWH(
            tarFaceRect.left + tarFaceRect.width/2 - quality.pendantSize/2,
            tarFaceRect.top + tarFaceRect.height/2 - quality.pendantSize/2,
            quality.pendantSize, quality.pendantSize)
        drawImageRect(pendantImg, srcPendantRect, tarPendantRect, FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST), null, true)
    }

    val verifyIcon = when (verifyType){
        0 -> "PERSONAL_OFFICIAL_VERIFY"
        1 -> "ORGANIZATION_OFFICIAL_VERIFY"
        else -> ""
    }

    if (verifyIcon != ""){
        val svg = SVGDOM(Data.makeFromFileName("src/main/resources/icon/$verifyIcon.svg"))
        drawImage(svg.makeImage(verifyIconSize,verifyIconSize), tarFaceRect.right-verifyIconSize, tarFaceRect.bottom-verifyIconSize)
    }

        //}
    //}.makeImageSnapshot()
}


fun Canvas.drawBadge(text: String, font: Font, fontColor: Int, bgColor: Int, bgAlpha: Int, cardRect: Rect, position: Position){

    val textLine = TextLine.make(text, font)

    val badgeWidth = textLine.width + quality.badgePadding * 8

    val rrect = when (position){
        TOP_LEFT -> RRect.makeXYWH(cardRect.left, cardRect.top - quality.badgeHeight, badgeWidth,
            quality.badgeHeight.toFloat(), quality.badgeArc, quality.badgeArc, 0f, 0f)
        TOP_RIGHT -> RRect.makeXYWH(cardRect.right - badgeWidth, cardRect.top - quality.badgeHeight, badgeWidth,
            quality.badgeHeight.toFloat(), quality.badgeArc, quality.badgeArc, 0f, 0f)
        BOTTOM_LEFT -> RRect.makeXYWH(cardRect.left, cardRect.bottom + quality.badgeHeight, badgeWidth,
            quality.badgeHeight.toFloat(), 0f, 0f, quality.badgeArc, quality.badgeArc)
        BOTTOM_RIGHT -> RRect.makeXYWH(cardRect.right - badgeWidth, cardRect.bottom + quality.badgeHeight, badgeWidth,
            quality.badgeHeight.toFloat(), 0f, 0f, quality.badgeArc, quality.badgeArc)
        else -> throw Exception("Bad Badge Position!")
    }

    drawCard(rrect, bgColor, bgAlpha)

    drawTextLine(textLine, rrect.left + quality.badgePadding * 4, rrect.bottom - (quality.badgeHeight-textLine.capHeight) / 2, Paint().apply { color = fontColor })

}

fun Canvas.drawLabelCard(text: String, font: Font, fontColor: Int, bgColor: Int, bgAlpha: Int, x: Float, y: Float): Float {
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

    drawTextLine(textLine, rrect.left + quality.badgePadding * 2, rrect.bottom - quality.badgePadding, Paint().apply { color = fontColor })

    return rrect.width
}

