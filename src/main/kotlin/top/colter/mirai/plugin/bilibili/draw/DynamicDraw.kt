package top.colter.mirai.plugin.bilibili.draw

import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.TextStyle
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.BiliConfig.imageConfig
import top.colter.mirai.plugin.bilibili.data.*
import top.colter.mirai.plugin.bilibili.data.DynamicType.DYNAMIC_TYPE_FORWARD
import top.colter.mirai.plugin.bilibili.data.DynamicType.DYNAMIC_TYPE_NONE
import top.colter.mirai.plugin.bilibili.draw.Position.*
import top.colter.mirai.plugin.bilibili.utils.*
import top.colter.mirai.plugin.bilibili.utils.FontUtils.loadTypeface
import top.colter.mirai.plugin.bilibili.utils.FontUtils.matchFamily


val logger by BiliBiliDynamic::logger

val quality: Quality by lazy {
    var quality: Quality?
    if (BiliImageQuality.customOverload) {
        quality = BiliImageQuality.customQuality
        logger.warning("图片分辨率配置已重载")
    } else {
        quality = BiliImageQuality.quality[imageConfig.quality]
        if (quality == null) {
            logger.error("未找到 ${imageConfig.quality} 的图片分辨率配置")
            quality = BiliImageQuality.quality.firstNotNullOf { it.value }
        }
    }
    quality.apply {
        badgeHeight = if (imageConfig.badgeEnable.enable) badgeHeight else 0
    }
}

val theme: Theme by lazy {
    var theme: Theme?
    if (BiliImageTheme.customOverload) {
        theme = BiliImageTheme.customTheme
        logger.warning("图片主题配置已重载")
    } else {
        theme = BiliImageTheme.theme[imageConfig.theme]
        if (theme == null) {
            logger.error("未找到 ${imageConfig.theme} 的图片主题配置")
            theme = BiliImageTheme.theme.firstNotNullOf { it.value }
        }
    }
    theme
}

val cardRect: Rect by lazy {
    Rect.makeLTRB(quality.cardMargin.toFloat(), 0f, quality.imageWidth - quality.cardMargin.toFloat(), 0f)
}

val cardContentRect: Rect by lazy {
    cardRect.inflate(-1f * quality.cardPadding)
}

val mainTypeface: Typeface by lazy {
    val mainFont = imageConfig.font.split(";").first().split(".").first()
    try {
        matchFamily(mainFont).matchStyle(FontStyle.NORMAL)!!
    } catch (e: Exception) {
        logger.error("加载主字体 $mainFont 失败")
        matchFamily("Source Han Sans").matchStyle(FontStyle.NORMAL)!!
    }
}

val font: Font by lazy {
    Font(mainTypeface, quality.contentFontSize)
}

val fansCardFont: Font by lazy {
    Font(loadTypeface(Data.makeFromBytes(loadResourceBytes("font/FansCard.ttf"))), quality.subTitleFontSize)
}

val titleTextStyle = TextStyle().apply {
    fontSize = quality.titleFontSize
    color = theme.titleColor
    typeface = mainTypeface
}
val descTextStyle = TextStyle().apply {
    fontSize = quality.descFontSize
    color = theme.descColor
    typeface = mainTypeface
}
val contentTextStyle = TextStyle().apply {
    fontSize = quality.contentFontSize
    color = theme.contentColor
    typeface = mainTypeface
}
val footerTextStyle = TextStyle().apply {
    fontSize = quality.footerFontSize
    color = theme.footerColor
    typeface = mainTypeface
}

val footerParagraphStyle = ParagraphStyle().apply {
    maxLinesCount = 2
    ellipsis = "..."
    alignment = BiliConfig.templateConfig.footer.footerAlign
    textStyle = footerTextStyle
}

val cardBadgeArc: FloatArray by lazy {
    val left = if (imageConfig.badgeEnable.left) 0f else quality.cardArc
    val right = if (imageConfig.badgeEnable.right) 0f else quality.cardArc
    floatArrayOf(left, right, quality.cardArc, quality.cardArc)
}

val linkPaint = Paint().apply {
    color = theme.linkColor
    isAntiAlias = true
}
val generalPaint = Paint().apply {
    color = theme.contentColor
    isAntiAlias = true
}


suspend fun DynamicItem.makeDrawDynamic(colors: List<Int>): String {
    val dynamic = drawDynamic(colors.first())
    val img = makeCardBg(dynamic.height, colors) {
        it.drawImage(dynamic, 0f, 0f)
    }
    return cacheImage(img, "$uid/$idStr.png", CacheType.DRAW_DYNAMIC)
}

suspend fun DynamicItem.drawDynamic(themeColor: Int, isForward: Boolean = false): Image {

    val orig = orig?.drawDynamic(themeColor, type == DYNAMIC_TYPE_FORWARD)

    var imgList = modules.makeGeneral(formatTime, link, type, themeColor, isForward)

    // 调整附加卡片顺序
    if (orig != null) {
        imgList = if (this.modules.moduleDynamic.additional != null) {
            val result = ArrayList<Image>(imgList.size + 1)
            result.addAll(imgList.subList(0, imgList.size - 1))
            result.add(orig)
            result.add(imgList.last())
            result
        } else {
            imgList.plus(orig)
        }
    }

    var height = imgList.sumOf {
        if (it.width > cardRect.width) {
            (cardRect.width * it.height / it.width + quality.contentSpace).toInt()
        } else {
            it.height + quality.contentSpace
        }
    }

    if (type == DynamicType.DYNAMIC_TYPE_WORD || type == DYNAMIC_TYPE_NONE) {
        height += quality.contentSpace * 2
    }

    val footerTemplate = BiliConfig.templateConfig.footer.dynamicFooter
    val footerParagraph = if (footerTemplate.isNotBlank() && !isForward){
        val footer = footerTemplate
            .replace("{name}", modules.moduleAuthor.name)
            .replace("{uid}", modules.moduleAuthor.mid.toString())
            .replace("{id}", did)
            .replace("{time}", formatTime)
            .replace("{type}", type.text)
        ParagraphBuilder(footerParagraphStyle, FontUtils.fonts).addText(footer).build().layout(cardRect.width)
    }else null

    val margin = if (isForward) quality.cardPadding * 2 else quality.cardMargin * 2

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

            drawCard(rrect)

            if (isForward) {
                drawRectShadowAntiAlias(rrect.inflate(1f), theme.smallCardShadow)
            } else {
                drawRectShadowAntiAlias(rrect.inflate(1f), theme.cardShadow)
            }

            if (imageConfig.badgeEnable.left) {
                val svg = loadSVG("icon/${if (isForward) "FORWARD" else "BILIBILI_LOGO"}.svg")
                drawBadge(
                    if (isForward) "转发动态" else "动态",
                    font,
                    theme.mainLeftBadge.fontColor,
                    theme.mainLeftBadge.bgColor,
                    rrect,
                    TOP_LEFT,
                    svg.makeImage(quality.contentFontSize, quality.contentFontSize)
                )
            }
            if (imageConfig.badgeEnable.right) {
                drawBadge(did, font, theme.mainRightBadge.fontColor, theme.mainRightBadge.bgColor, rrect, TOP_RIGHT)
            }

            var top = quality.cardMargin + quality.badgeHeight.toFloat()
            for (img in imgList) {

                //drawScaleWidthImageOutline(img, cardRect.width, quality.cardMargin.toFloat(), top, isForward)
                drawScaleWidthImage(img, cardRect.width, quality.cardMargin.toFloat(), top)

                top += if (img.width > cardRect.width) {
                    (cardRect.width * img.height / img.width + quality.contentSpace).toInt()
                } else {
                    img.height + quality.contentSpace
                }
            }

            footerParagraph?.paint(this, cardRect.left, rrect.bottom + quality.cardMargin / 2)

        }
    }.makeImageSnapshot()
}

suspend fun DynamicItem.Modules.makeGeneral(
    time: String,
    link: String,
    type: DynamicType,
    themeColor: Int,
    isForward: Boolean = false
): List<Image> {
    return mutableListOf<Image>().apply {
        if (type != DYNAMIC_TYPE_NONE)
            add(if (isForward) moduleAuthor.drawForward(time) else moduleAuthor.drawGeneral(time, link, themeColor))
        moduleDispute?.drawGeneral()?.let { add(it) }
        addAll(moduleDynamic.makeGeneral(isForward))
    }
}

fun Rect.textVertical(text: TextLine) =
    bottom - (height - text.capHeight) / 2

fun Canvas.drawCard(rrect: RRect, bgColor: Int = theme.cardBgColor) {
    drawRRect(rrect, Paint().apply {
        color = bgColor
        mode = PaintMode.FILL
        isAntiAlias = true
    })
    drawRRect(rrect, Paint().apply {
        color = theme.cardOutlineColors.first()
        mode = PaintMode.STROKE
        strokeWidth = quality.cardOutlineWidth
        isAntiAlias = true
        shader = Shader.makeSweepGradient(
            rrect.left + rrect.width / 2,
            rrect.top + rrect.height / 2,
            theme.cardOutlineColors
        )
    })
}

fun makeCardBg(height: Int, colors: List<Int>, block: (Canvas) -> Unit): Image {
    val imageRect = Rect.makeXYWH(0f, 0f, quality.imageWidth.toFloat(), height.toFloat())
    return Surface.makeRasterN32Premul(imageRect.width.toInt(), height).apply {
        canvas.apply {
            drawRect(imageRect, Paint().apply {
                shader = Shader.makeLinearGradient(
                    Point(imageRect.left, imageRect.top),
                    Point(imageRect.right, imageRect.bottom),
                    // H：色相   S：30   B：100
                    //generateLinearGradient(listOf(0xFFffb2cc.toInt(), 0xFFffb2b2.toInt()))
                    //generateLinearGradient(listOf(0xFFd3edfa.toInt()))
                    generateLinearGradient(colors)
                )
            })
            block(this)
        }
    }.makeImageSnapshot()
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
            Paint().apply { color = theme.faceOutlineColor })
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
        val svg = loadSVG("icon/$verifyIcon.svg")
        val size = if (hasPendant) verifyIconSize - quality.noPendantFaceInflate / 2 else verifyIconSize
        drawImage(
            svg.makeImage(size, size),
            tarFaceRect.right - size,
            tarFaceRect.bottom - size
        )
    }
}

fun Canvas.drawBadge(
    text: String,
    font: Font,
    fontColor: Int,
    bgColor: Int,
    cardRect: Rect,
    position: Position,
    icon: Image? = null
) {

    val textLine = TextLine.make(text, font)

    val badgeWidth = textLine.width + quality.badgePadding * 8 + (icon?.width ?: 0)

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
    }

    drawCard(rrect, bgColor)

    var x = rrect.left + quality.badgePadding * 4
    if (icon != null) {
        x -= quality.badgePadding
        drawImage(icon, x, rrect.top + (quality.badgeHeight - icon.height) / 2)
        x += icon.width + quality.badgePadding * 2
    }

    drawTextLine(
        textLine,
        x,
        rrect.bottom - (quality.badgeHeight - textLine.capHeight) / 2,
        Paint().apply { color = fontColor })

}

fun Canvas.drawLabelCard(
    textLine: TextLine,
    x: Float,
    y: Float,
    fontPaint: Paint,
    bgPaint: Paint
) {

    val rrect = RRect.makeXYWH(
        x,
        y,
        textLine.width + quality.badgePadding * 4,
        textLine.height,
        quality.badgeArc
    )
    drawRRect(rrect, bgPaint)

    drawTextLine(
        textLine,
        rrect.left + quality.badgePadding * 2,
        rrect.bottom - quality.badgePadding,
        //rrect.textVertical(textLine),
        fontPaint
    )
}

