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
import top.colter.mirai.plugin.bilibili.data.DynamicType.*
import top.colter.mirai.plugin.bilibili.data.ModuleAuthor
import top.colter.mirai.plugin.bilibili.data.ModuleDynamic
import top.colter.mirai.plugin.bilibili.draw.Position.*
import top.colter.mirai.plugin.bilibili.utils.FontUtils.fonts
import top.colter.mirai.plugin.bilibili.utils.FontUtils.loadTypeface
import java.io.File

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
    Font(mainTypeface, quality.mainFontSize)
}


val titleTextStyle = TextStyle().apply {
    fontSize = quality.mainFontSize
    color = Color.makeRGB(49, 49, 49)
    typeface = mainTypeface
}
val descTextStyle = TextStyle().apply {
    fontSize = quality.subFontSize
    color = Color.makeRGB(102, 102, 102)
    typeface = mainTypeface
}

val topTwoBadgeCardArc = floatArrayOf(0f, 0f, quality.cardArc, quality.cardArc)

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



object DynamicDraw {
//    val quality: Quality
//    val font: Typeface
//
//    init {
//        val imageQualityLevel = 1
//        quality = Quality.level(imageQualityLevel)
//
//        font = Typeface.makeFromFile("E:/Desktop/资源/字体/HarmonyOS Sans/HarmonyOS_Sans_SC/HarmonyOS_Sans_SC_Medium.ttf")
//
//    }

    fun makeDynamic(){



    }

}

fun DynamicItem.draw(){

    when(this.type){
        DYNAMIC_TYPE_WORD -> {}
        DYNAMIC_TYPE_DRAW -> {}
        DYNAMIC_TYPE_ARTICLE -> {}
        DYNAMIC_TYPE_FORWARD -> {}
        DYNAMIC_TYPE_AV -> {}
        DYNAMIC_TYPE_MUSIC -> {}
        DYNAMIC_TYPE_LIVE -> {}
        DYNAMIC_TYPE_LIVE_RCMD -> {}
        DYNAMIC_TYPE_PGC -> {}
        DYNAMIC_TYPE_COMMON_SQUARE -> {}
        DYNAMIC_TYPE_NONE -> {}
    }

}

fun ModuleAuthor.draw(){
//    makeAvatar()
}

fun ModuleDynamic.draw(): List<Image> {
    val imgs = mutableListOf<Image>()
    topic?.draw()?.let { imgs.add(it) }
    desc?.draw()?.let { imgs.add(it) }
    major?.draw()?.let { imgs.add(it) }
    additional?.draw()?.let { imgs.add(it) }
    return imgs.toList()
}

fun ModuleDynamic.Topic.draw(): Image? {
    return null
}

fun ModuleDynamic.Desc.draw(): Image?{
    return null
}

fun ModuleDynamic.Additional.draw(): Image? {
    return when (type){
        "ADDITIONAL_TYPE_COMMON" -> {null}
        "ADDITIONAL_TYPE_RESERVE" -> {null}
        "ADDITIONAL_TYPE_VOTE" -> {null}
        "ADDITIONAL_TYPE_UGC" -> {null}
        "ADDITIONAL_TYPE_GOODS" -> {null}
        else -> {null}
    }
}

fun ModuleDynamic.Major.draw(): Image? {
    return when (type){
        "MAJOR_TYPE_ARCHIVE" -> {
            archive!!.makeVideoContent()
        }
        "MAJOR_TYPE_DRAW" -> {
            null
        }
        "MAJOR_TYPE_ARTICLE" -> {null}
        "MAJOR_TYPE_MUSIC" -> {null}
        "MAJOR_TYPE_LIVE" -> {null}
        "MAJOR_TYPE_LIVE_RCMD" -> {null}
        "MAJOR_TYPE_PGC" -> {null}
        "MAJOR_TYPE_COMMON" -> {null}
        "MAJOR_TYPE_NONE" -> {null}
        else -> {null}
    }
}


suspend fun ModuleDynamic.Desc.makeTextContent(): Image {

    //val textRect = cardRect.inflate(-1f * quality.cardPadding)

    return Surface.makeRasterN32Premul(cardRect.width.toInt(), 200).apply {
        canvas.apply {
            val linkPaint = Paint().apply {
                color = Color.makeRGB(23, 139, 207)
                isAntiAlias = true
            }
            val generalPaint = Paint().apply {
                color = Color.makeRGB(34, 34, 34)
                isAntiAlias = true
            }

            var x = quality.cardPadding.toFloat()
            var y = quality.mainFontSize

            val nodes = this@makeTextContent.richTextNodes
            nodes.forEach {

                //val text = TextLine.make(it.text, font)

                if (it.type == "RICH_TEXT_NODE_TYPE_TEXT"){
                    val point = drawTextArea(it.text,cardContentRect, x, y, font, generalPaint)
                    x = point.x
                    y = point.y
                }else if (it.type == "RICH_TEXT_NODE_TYPE_EMOJI"){
                    val img = Image.makeFromEncoded(HttpClient(OkHttp).get<ByteArray>(it.emoji?.iconUrl!!))
                    val emojiSize = quality.mainFontSize * 1.4f
                    if (x + emojiSize > cardContentRect.right){
                        x = cardContentRect.left
                        y += emojiSize + quality.lineSpace
                    }
                    val srcRect = Rect.makeXYWH(0f, 0f, img.width.toFloat(), img.height.toFloat())
                    val tarRect = Rect.makeXYWH(x, y - quality.mainFontSize*1.1f, emojiSize, emojiSize)
                    drawImageRect(img, srcRect, tarRect, FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST), null, true)
                    x += emojiSize
                }else{
                    if (it.type == "RICH_TEXT_NODE_TYPE_WEB"){
                        //val svg = SVGDOM(Data.makeFromBytes(BiliBiliDynamic.getResourceAsStream("src/main/resources/icon/RICH_TEXT_NODE_TYPE_WEB.svg")!!.readBytes()))
                        val svg = SVGDOM(Data.makeFromFileName("src/main/resources/icon/${it.type}.svg"))
                        val iconSize = quality.mainFontSize * 1.2f
                        drawImage(svg.makeImage(iconSize,iconSize), x, y - quality.mainFontSize)
                        x += iconSize
                    }

                    val point = drawTextArea(it.text, cardContentRect, x, y, font, linkPaint)
                    x = point.x
                    y = point.y
                }

                //x += text.width

            }
        }
    }.makeImageSnapshot()


}



fun ModuleDynamic.Major.Archive.makeVideoContent(): Image {

    val paragraphStyle = ParagraphStyle().apply {
        maxLinesCount = 2
        ellipsis = "..."
        alignment = Alignment.LEFT
        textStyle = titleTextStyle
    }

    val paragraphWidth = cardContentRect.width - quality.cardPadding

    val titleParagraph = ParagraphBuilder(paragraphStyle, fonts).addText(title).build().layout(paragraphWidth)

    paragraphStyle.apply {
        textStyle = descTextStyle
    }

    val descParagraph = ParagraphBuilder(paragraphStyle, fonts).addText(desc).build().layout(paragraphWidth)

    val videoCoverHeight = cardContentRect.width * 0.625f  // 封面比例 16:10
    val videoCardHeight = videoCoverHeight + titleParagraph.height + descParagraph.height + quality.cardPadding * 1.5f

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
            drawBadge(quality, this@makeVideoContent.badge.text, font, Color.WHITE, Color.makeRGB(251, 114, 153), 255, videoCardRect, TOP_LEFT)
            drawBadge(quality, "av${this@makeVideoContent.aid}  |  ${this@makeVideoContent.bvid}", font, Color.WHITE, Color.makeRGB(72, 199, 240), 255, videoCardRect, TOP_RIGHT)

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

            val durationText = TextLine.make(this@makeVideoContent.durationText, font)
            val playInfo = TextLine.make("${this@makeVideoContent.stat.play}观看 ${this@makeVideoContent.stat.danmaku}弹幕", font.makeWithSize(22f))

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

            titleParagraph.paint(this, quality.cardPadding*1.5f, quality.badgeHeight + videoCoverHeight + quality.cardPadding / 2)

            descParagraph.paint(this, quality.cardPadding*1.5f, quality.badgeHeight + videoCoverHeight + quality.cardPadding + titleParagraph.height)

        }
    }.makeImageSnapshot()
}

fun Rect.textVertical(text: TextLine) =
    bottom - (height - text.capHeight) / 2


fun Canvas.drawCard(rrect: RRect){
    // alpha = 120
    drawRRect(rrect, Paint().apply {
        color = Color.WHITE
        alpha = 160
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

const val emojiCharacter = "(?:[\\uD83C\\uDF00-\\uD83D\\uDDFF]|[\\uD83E\\uDD00-\\uD83E\\uDDFF]|[\\uD83D\\uDE00-\\uD83D\\uDE4F]|[\\uD83D\\uDE80-\\uD83D\\uDEFF]|[\\u2600-\\u26FF]\\uFE0F?|[\\u2700-\\u27BF]\\uFE0F?|\\u24C2\\uFE0F?|[\\uD83C\\uDDE6-\\uD83C\\uDDFF]{1,2}|[\\uD83C\\uDD70\\uD83C\\uDD71\\uD83C\\uDD7E\\uD83C\\uDD7F\\uD83C\\uDD8E\\uD83C\\uDD91-\\uD83C\\uDD9A]\\uFE0F?|[\\u0023\\u002A\\u0030-\\u0039]\\uFE0F?\\u20E3|[\\u2194-\\u2199\\u21A9-\\u21AA]\\uFE0F?|[\\u2B05-\\u2B07\\u2B1B\\u2B1C\\u2B50\\u2B55]\\uFE0F?|[\\u2934\\u2935]\\uFE0F?|[\\u3030\\u303D]\\uFE0F?|[\\u3297\\u3299]\\uFE0F?|[\\uD83C\\uDE01\\uD83C\\uDE02\\uD83C\\uDE1A\\uD83C\\uDE2F\\uD83C\\uDE32-\\uD83C\\uDE3A\\uD83C\\uDE50\\uD83C\\uDE51]\\uFE0F?|[\\u203C\\u2049]\\uFE0F?|[\\u25AA\\u25AB\\u25B6\\u25C0\\u25FB-\\u25FE]\\uFE0F?|[\\u00A9\\u00AE]\\uFE0F?|[\\u2122\\u2139]\\uFE0F?|\\uD83C\\uDC04\\uFE0F?|\\uD83C\\uDCCF\\uFE0F?|[\\u231A\\u231B\\u2328\\u23CF\\u23E9-\\u23F3\\u23F8-\\u23FA]\\uFE0F?)(?:[\\uD83C\\uDFFB-\\uD83C\\uDFFF]|[\\uD83E\\uDDB0-\\uD83E\\uDDB3])?"

val emojiRegex = "${emojiCharacter}(?:\\u200D${emojiCharacter})*".toRegex()

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
                for (c in it.value){
                    val charLine = TextLine.make(c.toString(), font)
                    if (x + charLine.width > rect.right){
                        x = rect.left
                        y += charLine.height + quality.lineSpace
                    }
                    drawTextLine(charLine, x, y, paint)
                    x += charLine.width
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

val paint = Paint().apply {
    color = Color.BLACK
    isAntiAlias = true
}
suspend fun makeCardBg(dynamic: DynamicItem){

    //val imageWidth = 800
    val imageHeight = 930
    val imageRect = Rect.makeXYWH(0f,0f,quality.imageWidth.toFloat(), imageHeight.toFloat())
    val cardRect = Rect.makeLTRB(
        quality.cardMargin.toFloat(),
        quality.cardMargin.toFloat() + quality.badgeHeight,
        quality.imageWidth.toFloat() - quality.cardMargin.toFloat(),
        imageHeight.toFloat() - quality.cardMargin.toFloat()
    )

    //val typeface = Typeface.makeFromFile("E:/Desktop/资源/字体/HarmonyOS Sans/HarmonyOS_Sans_SC/HarmonyOS_Sans_SC_Medium.ttf")
    //val font = Font(typeface, 25f)
    //val paint = Paint().apply {
    //    color = Color.BLACK
    //    isAntiAlias = true
    //}

    Surface.makeRasterN32Premul(imageRect.width.toInt(), imageRect.height.toInt()).apply {
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
                    intArrayOf(
                        0xFFffb2ff.toInt(),0xFFffb2e5.toInt(), 0xFFffb2cc.toInt(), 0xFFffb2b2.toInt(), 0xFFffccb2.toInt()
                    )
                )
            })
            val rrect = RRect.makeLTRB(cardRect.left, cardRect.top, cardRect.right, cardRect.bottom, 0f,0f,quality.cardArc,quality.cardArc)


            drawCard(rrect)


            drawRectShadowAntiAlias(rrect.inflate(1f), 6f, 6f, 25f, 0f, Color.makeARGB(70, 0, 0, 0))


            drawBadge(quality, "动态", font, Color.makeRGB(0, 203, 255), Color.WHITE, 120, cardRect, TOP_LEFT)
            drawBadge(quality, "650036681957703732", font, Color.WHITE, Color.makeRGB(72, 199, 240), 255, cardRect, TOP_RIGHT)


            drawImage(makeAuthorHeader(dynamic, quality), cardRect.left, cardRect.top)

            drawImage(dynamic.modules.moduleDynamic.desc!!.makeTextContent(), quality.cardMargin.toFloat(), quality.cardMargin+quality.cardPadding+200f)


            //drawTextArea("🔌每天晚😶‍🌫️🧑🏾‍🎤上10点开👩🏻‍🏫👩🏻‍⚕️始直播，周三除🙂😑🙃外！🥬", cardContentRect,600f, 200f, font, paint)

            drawImage(dynamic.modules.moduleDynamic.major?.archive!!.makeVideoContent(), quality.cardMargin.toFloat(), quality.cardMargin+quality.cardPadding+250f)


        }

    }.saveImage("dynamic.png")
}

fun Surface.saveImage(path: String) = File(path).writeBytes(makeImageSnapshot().encodeToData()!!.bytes)


fun makeAuthorHeader(dynamic: DynamicItem, quality: Quality): Image {
    return Surface.makeRasterN32Premul(quality.imageWidth-quality.cardMargin*2,200).apply surface@{
        canvas.apply {


            drawImage(makeAvatar(quality, "asd","asd",0), 0f,0f)


            val textLineName = TextLine.make("猫芒ベル_Official", font.makeWithSize(30f))
            drawTextLine(textLineName, 150f, 60f,Paint().apply { color = Color.makeRGB(251, 114, 153) })

            val textLineTime = TextLine.make("2022年04月20日 22:17:20", font.makeWithSize(20f))
            drawTextLine(textLineTime, 150f, 90f, Paint().apply { color = Color.makeRGB(156, 156, 156) })


            val fan = Image.makeFromEncoded(File("D:/Desktop/bilibili动态/d73dd984b8b55e56ac6bdad583a754d147ebd0fa.png").readBytes())
            //val fan = Image.makeFromEncoded(File("D:/Desktop/bilibili动态/5ebada630d1897124a9f33dd2d5c9566d02fcc72.png").readBytes())
            val srcFRect = Rect(0f,0f,fan.width.toFloat(),fan.height.toFloat())

            // 100 300
            val tarWidth = 300
            val tarFRect = Rect.makeXYWH(this@surface.width-tarWidth-20f, 20f,
                tarWidth.toFloat(),
                (tarWidth * fan.height / fan.width).toFloat()
            )
            drawImageRect(fan, srcFRect, tarFRect, FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST), null, true)


            val typeface1 = Typeface.makeFromFile("D:/Desktop/bilibili动态/fansCard.ttf")
            val font1 = Font(typeface1, 25f)
            val textLineFan = TextLine.make("001107",font1.makeWithSize(20f))
            drawTextLine(textLineFan, this@surface.width-200f, 70f, Paint().apply { color = Color.makeRGB(213, 122, 255) })

        }
    }.makeImageSnapshot()
}

fun makeAvatar(quality: Quality, face: String, pendant: String, verifyType: Int): Image {
    val avatarSize = (quality.cardPadding*4+quality.faceSize).toInt()
    return Surface.makeRasterN32Premul(avatarSize, avatarSize).apply {
        canvas.apply {
            val faceImg = Image.makeFromEncoded(File("D:/Desktop/bilibili动态/625896a6d3a355f3925b8da02f30917e986822b0.jpg").readBytes())

            var tarFaceRect = RRect.makeXYWH(
                quality.cardPadding*2f,
                quality.cardPadding*1.5f,
                quality.faceSize,
                quality.faceSize,
                quality.faceSize/2
            )
             if (pendant == ""){
                 tarFaceRect = tarFaceRect.inflate(quality.noPendantFaceInflate) as RRect
                 drawCircle(tarFaceRect.left+tarFaceRect.width/2,tarFaceRect.top+tarFaceRect.width/2,tarFaceRect.width/2+2,Paint().apply { color = Color.WHITE; alpha = 160 })
            }

            drawImageRRect(faceImg, tarFaceRect)


            if (pendant != ""){
                //val pand = Image.makeFromEncoded(File("D:/Desktop/d8f6dec3bd0bcdb09fcfd99fba620aa7da91dd8e.png").readBytes())
                //val pand = Image.makeFromEncoded(File("D:/Desktop/1cdf174c75dd6493f3c8f0797e972b69e3293870.png").readBytes())
                val pendantImg = Image.makeFromEncoded(File("D:/Desktop/d3587e6f3b534499fc08a71296bafa74a159fa33.png").readBytes())

                val srcPendantRect = Rect(0f,0f,pendantImg.width.toFloat(),pendantImg.height.toFloat())
                val tarPendantRect = Rect.makeXYWH(
                    tarFaceRect.left + tarFaceRect.width/2 - quality.pendantSize/2,
                    tarFaceRect.top + tarFaceRect.height/2 - quality.pendantSize/2,
                    quality.pendantSize, quality.pendantSize)
                drawImageRect(pendantImg, srcPendantRect, tarPendantRect, FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST), null, true)
            }


            val svg = SVGDOM(Data.makeFromFileName("D:/Desktop/bilibili动态/个人认证.svg"))
            drawImage(svg.makeImage(quality.verifyIconSize,quality.verifyIconSize), tarFaceRect.right-quality.verifyIconSize, tarFaceRect.bottom-quality.verifyIconSize)
        }
    }.makeImageSnapshot()
}


fun SVGDOM.makeImage(width: Float, height: Float): Image{
    setContainerSize(width, height)
    return Surface.makeRasterN32Premul(width.toInt(), height.toInt()).apply { render(canvas) }.makeImageSnapshot()
}



fun Canvas.drawRectShadowAntiAlias(r: Rect, dx: Float, dy: Float, blur: Float, spread: Float, color: Int): Canvas {
    val insides = r.inflate(-1f)
    if (!insides.isEmpty) {
        save()
        if (insides is RRect) clipRRect(insides, ClipMode.DIFFERENCE, true)
        else clipRect(insides, ClipMode.DIFFERENCE, true)
        drawRectShadowNoclip(r, dx, dy, blur, spread, color)
        restore()
    } else drawRectShadowNoclip(r, dx, dy, blur, spread, color)
    return this
}

fun Canvas.drawImageRRect(image: Image, srcRect: Rect, rRect: RRect) {
    save()
    clipRRect(rRect, true)
    drawImageRect(image, srcRect, rRect, FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST), null, true)
    restore()
}
fun Canvas.drawImageRRect(image: Image, rRect: RRect) =
    drawImageRRect(image, Rect(0f,0f,image.width.toFloat(),image.height.toFloat()), rRect)


fun Canvas.drawBadge(textLine: TextLine, font: Font, rrect: RRect){
    //val textLine = TextLine.make(text, font)
    //val rrect3 = RRect.makeXYWH(cardRect.right-textLine.width-30, cardRect.top-35, textLine.width+30, 35f, 5f,5f,0f,0f)
    drawRRect(rrect, Paint().apply {
        color = Color.makeRGB(72, 199, 240)
        mode = PaintMode.FILL
        isAntiAlias = true
    })
    drawRRect(rrect, Paint().apply {
        color = Color.WHITE
        mode = PaintMode.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    })
    //drawTextLine(textLine,cardRect.right-textLine.width-15,cardRect.top-8, paint.apply { color = Color.WHITE })
    drawTextLine(textLine, rrect.left + font.size/2, rrect.bottom-(rrect.height-textLine.capHeight)/2, Paint().apply { color = Color.WHITE })
}


fun Canvas.drawBadge(quality: Quality, text: String, font: Font, fontColor: Int, bgColor: Int, bgAlpha: Int, cardRect: Rect, position: Position){

    val textLine = TextLine.make(text, font)

    val badgeWidth = textLine.width + quality.badgePadding*8
    //val badgeHeight = textLine.height + quality.badgePadding*2

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

    drawRRect(rrect, Paint().apply {
        color = bgColor
        mode = PaintMode.FILL
        alpha = bgAlpha
        isAntiAlias = true
    })
    drawRRect(rrect, Paint().apply {
        color = Color.WHITE
        mode = PaintMode.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    })

    drawTextLine(textLine, rrect.left + quality.badgePadding*4, rrect.bottom - (quality.badgeHeight-textLine.capHeight)/2, Paint().apply { color = fontColor })

}