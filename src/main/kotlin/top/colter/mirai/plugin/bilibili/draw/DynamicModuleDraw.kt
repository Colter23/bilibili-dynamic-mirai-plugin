package top.colter.mirai.plugin.bilibili.draw

import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.api.twemoji
import top.colter.mirai.plugin.bilibili.data.ModuleAuthor
import top.colter.mirai.plugin.bilibili.data.ModuleDispute
import top.colter.mirai.plugin.bilibili.data.ModuleDynamic
import top.colter.mirai.plugin.bilibili.utils.CacheType
import top.colter.mirai.plugin.bilibili.utils.FontUtils
import top.colter.mirai.plugin.bilibili.utils.formatTime
import top.colter.mirai.plugin.bilibili.utils.getOrDownloadImage
import top.colter.mirai.plugin.bilibili.utils.translate.trans
import java.util.stream.Collectors
import kotlin.math.abs
import kotlin.math.ceil


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
                when (reserve!!.stype) {
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
            drawRectShadowAntiAlias(additionalCardRect.inflate(1f), theme.smallCardShadow)

            val labelTextLine = TextLine.make(label, font.makeWithSize(quality.subTitleFontSize))
            drawTextLine(labelTextLine, additionalCardRect.left + 8, quality.subTitleFontSize, Paint().apply {
                color = theme.subTitleColor
            })

            var x = quality.cardPadding.toFloat()

            if (cover != null) {
                getOrDownloadImage(cover, CacheType.OTHER)?.let {img ->
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
            }

            x += quality.cardPadding

            val titleParagraph =
                ParagraphBuilder(paragraphStyle, FontUtils.fonts).addText(title).build()
                    .layout(cardContentRect.width - x)
            paragraphStyle.apply {
                textStyle = descTextStyle.apply {
                    fontSize = quality.subTitleFontSize * 0.8f
                }
            }
            val desc1Paragraph =
                ParagraphBuilder(paragraphStyle, FontUtils.fonts).addText(desc1).build()
                    .layout(cardContentRect.width - x)
            val desc2Paragraph = desc2?.let {
                ParagraphBuilder(paragraphStyle, FontUtils.fonts).addText(it).build().layout(cardContentRect.width - x)
            }

            val top = (additionalCardRect.height - (titleParagraph.height * if (desc2 == null) 2 else 3)) / 2

            var y = additionalCardRect.top + top
            titleParagraph.paint(this, x, y)

            y += titleParagraph.height
            desc1Paragraph.paint(this, x, y)

            if (desc2Paragraph != null) {
                y += titleParagraph.height
                desc2Paragraph.paint(this, x, y)
            }
        }
    }.makeImageSnapshot()
}

suspend fun ModuleDispute.drawGeneral(): Image {
    val lineCount = if (TextLine.make(title, font).width / cardContentRect.width > 1) 2 else 1
    val textCardHeight = (quality.contentFontSize + quality.lineSpace * 2) * lineCount

    val textCardRect = Rect.makeXYWH(
        quality.cardPadding.toFloat(),
        0f,
        cardContentRect.width,
        textCardHeight
    )

    return Surface.makeRasterN32Premul(cardRect.width.toInt(), textCardHeight.toInt()).apply {
        canvas.apply {
            drawRRect(textCardRect.toRRect(5f), Paint().apply { color = Color.makeRGB(255, 241, 211) })

            var x = quality.cardPadding.toFloat() + 10
            var y = quality.contentFontSize * 0.8f + quality.lineSpace
            try {
                val svg = loadSVG("icon/DISPUTE.svg")
                val iconSize = quality.contentFontSize
                drawImage(svg.makeImage(iconSize, iconSize), x, y - quality.contentFontSize * 0.9f)
                x += iconSize + quality.lineSpace
            } catch (e: Exception) {
                logger.warning("未找到类型为 DISPUTE 的图标")
            }

            drawTextArea(title, textCardRect, x, y, font, Paint().apply { color = Color.makeRGB(231, 139, 31) })
        }
    }.makeImageSnapshot()
}

suspend fun ModuleDynamic.Topic.drawGeneral(): Image {

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
                val svg = loadSVG("icon/TOPIC.svg")
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

suspend fun ModuleDynamic.ContentDesc.drawGeneral(): Image {
    val paragraphStyle = ParagraphStyle().apply {
        alignment = Alignment.LEFT
        textStyle = titleTextStyle
    }

    val traCutLineNode = ModuleDynamic.ContentDesc.RichTextNode(
        "RICH_TEXT_NODE_TYPE_TEXT",
        BiliConfig.translateConfig.cutLine,
        BiliConfig.translateConfig.cutLine
    )

    val tra = trans(text)

    val textParagraph =
        ParagraphBuilder(paragraphStyle, FontUtils.fonts).addText("$text${traCutLineNode.text}$tra").build()
            .layout(cardContentRect.width)

    val textCardHeight = (quality.contentFontSize + quality.lineSpace * 2) * (textParagraph.lineNumber + 2)

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
            val nodes = if (tra != null) {
                richTextNodes.plus(traCutLineNode).plus(
                    ModuleDynamic.ContentDesc.RichTextNode(
                        "RICH_TEXT_NODE_TYPE_TEXT", tra, tra
                    )
                )
            } else {
                richTextNodes
            }
            nodes.forEach {
                when (it.type) {
                    "RICH_TEXT_NODE_TYPE_TEXT" -> {
                        val text = it.text.replace("\r\n", "\n").replace("\r", "\n")
                        val point = drawTextArea(text, textCardRect, x, y, font, generalPaint)
                        x = point.x
                        y = point.y
                    }

                    "RICH_TEXT_NODE_TYPE_EMOJI" -> {
                        getOrDownloadImage(it.emoji!!.iconUrl, CacheType.EMOJI)?.let { img ->
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
                    }

                    "RICH_TEXT_NODE_TYPE_WEB",
                    "RICH_TEXT_NODE_TYPE_VOTE",
                    "RICH_TEXT_NODE_TYPE_LOTTERY",
                    "RICH_TEXT_NODE_TYPE_BV" -> {
                        try {
                            val svg = loadSVG("icon/${it.type}.svg")
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

suspend fun Canvas.drawTextArea(text: String, rect: Rect, textX: Float, textY: Float, font: Font, paint: Paint): Point {
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

    for (node in textNode) {
        when (node) {
            is RichText.Text -> {
                for (point in node.value.codePoints()) {
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
                if (emojiTypeface != null) {
                    val tl = TextLine.make(node.value, emojiFont)
                    if (x + tl.width > rect.right) {
                        x = rect.left
                        y += tl.height + quality.lineSpace
                    }
                    drawTextLine(tl, x, y, paint)
                    x += tl.width
                }else {
                    val emoji = node.value.codePoints().mapToObj { it.toString(16) }.collect(Collectors.joining("-"))
                    val emojiSize = TextLine.make("🙂", font).height

                    var emojiImg: Image? = null
                    try {
                        emojiImg = getOrDownloadImage(twemoji(emoji), CacheType.EMOJI)
                    } catch (_: Exception) { }
                    try {
                        val e = emoji.split("-")
                        val et = if (e.last() == "fe0f") {
                            e.dropLast(1)
                        } else {
                            e.plus("fe0f")
                        }.joinToString("-")
                        emojiImg = getOrDownloadImage(twemoji(et), CacheType.EMOJI)
                    } catch (_: Exception) { }

                    if (x + emojiSize > rect.right) {
                        x = rect.left
                        y += emojiSize + quality.lineSpace
                    }
                    if (emojiImg != null) {
                        val srcRect = Rect.makeXYWH(0f, 0f, emojiImg.width.toFloat(), emojiImg.height.toFloat())
                        val tarRect = Rect.makeXYWH(x, y - emojiSize * 0.8f, emojiSize * 0.9f, emojiSize * 0.9f)
                        drawImageRect(
                            emojiImg,
                            srcRect,
                            tarRect,
                            FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST),
                            null,
                            true
                        )
                    }
                    x += emojiSize
                }
            }
        }
    }

    return Point(x, y)
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

            drawTextLine(textLineName, x, y, Paint().apply { color = theme.nameColor })

            y -= (quality.nameFontSize - quality.subTitleFontSize) / 2
            x += textLineName.width + quality.cardPadding
            drawTextLine(textLineTime, x, y, Paint().apply { color = theme.subTitleColor })

        }
    }.makeImageSnapshot()
}

suspend fun ModuleAuthor.drawGeneral(time: String, link: String, themeColor: Int): Image {
    return Surface.makeRasterN32Premul(
        quality.imageWidth - quality.cardMargin * 2,
        quality.pendantSize.toInt()
    ).apply surface@{
        canvas.apply {
            drawAvatar(face, pendant?.image, officialVerify?.type, quality.faceSize, quality.verifyIconSize)

            val textLineName = TextLine.make(name, font.makeWithSize(quality.nameFontSize))
            val textLineTime = TextLine.make(time, font.makeWithSize(quality.subTitleFontSize))

            val x = quality.faceSize + quality.cardPadding * 3.2f
            val space = (quality.pendantSize - quality.nameFontSize - quality.subTitleFontSize) / 3
            var y = quality.nameFontSize + space * 1.25f

            drawTextLine(textLineName, x, y, Paint().apply { color = theme.nameColor })

            y += quality.subTitleFontSize + space * 0.5f
            drawTextLine(textLineTime, x, y, Paint().apply { color = theme.subTitleColor })

            drawOrnament(decorate, link, themeColor)
        }
    }.makeImageSnapshot()
}

suspend fun Canvas.drawOrnament(decorate: ModuleAuthor.Decorate?, link: String?, qrCodeColor: Int?) {

    when (BiliConfig.imageConfig.cardOrnament) {
        "FanCard" -> {
            if (decorate != null) {
                getOrDownloadImage(decorate.cardUrl, CacheType.USER)?.let {fanImg ->
                    val srcFRect = Rect(0f, 0f, fanImg.width.toFloat(), fanImg.height.toFloat())

                    val cardHeight = when (decorate.type) {
                        1, 2 -> quality.ornamentHeight * 0.6f
                        else -> quality.ornamentHeight
                    }

                    val cardWidth = fanImg.width * cardHeight / fanImg.height

                    val y = ((quality.faceSize - cardHeight + quality.contentSpace) / 2)
                    val tarFRect = Rect.makeXYWH(
                        cardContentRect.right - cardWidth - abs(y),
                        y + quality.cardPadding,
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
        }

        "QrCode" -> {
            val qrCodeImg = qrCode(link!!, quality.ornamentHeight.toInt(), qrCodeColor!!)
            val y = ((quality.faceSize - qrCodeImg.height + quality.contentSpace) / 2)
            val tarFRect = Rect.makeXYWH(
                cardContentRect.right - qrCodeImg.width - abs(y),
                y + quality.cardPadding,
                qrCodeImg.width.toFloat(),
                qrCodeImg.height.toFloat()
            )
            val srcFRect = Rect(0f, 0f, qrCodeImg.width.toFloat(), qrCodeImg.height.toFloat())
            drawImageRect(
                qrCodeImg,
                srcFRect,
                tarFRect,
                FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST),
                Paint(),
                true
            )
        }
    }
}