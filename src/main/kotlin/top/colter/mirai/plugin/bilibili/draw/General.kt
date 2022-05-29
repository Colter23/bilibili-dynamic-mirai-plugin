package top.colter.mirai.plugin.bilibili.draw

import org.jetbrains.skia.*
import org.jetbrains.skia.svg.SVGDOM
import java.io.File
import java.util.*


const val emojiCharacter = "(?:[\\uD83C\\uDF00-\\uD83D\\uDDFF]|[\\uD83E\\uDD00-\\uD83E\\uDDFF]|[\\uD83D\\uDE00-\\uD83D\\uDE4F]|[\\uD83D\\uDE80-\\uD83D\\uDEFF]|[\\u2600-\\u26FF]\\uFE0F?|[\\u2700-\\u27BF]\\uFE0F?|\\u24C2\\uFE0F?|[\\uD83C\\uDDE6-\\uD83C\\uDDFF]{1,2}|[\\uD83C\\uDD70\\uD83C\\uDD71\\uD83C\\uDD7E\\uD83C\\uDD7F\\uD83C\\uDD8E\\uD83C\\uDD91-\\uD83C\\uDD9A]\\uFE0F?|[\\u0023\\u002A\\u0030-\\u0039]\\uFE0F?\\u20E3|[\\u2194-\\u2199\\u21A9-\\u21AA]\\uFE0F?|[\\u2B05-\\u2B07\\u2B1B\\u2B1C\\u2B50\\u2B55]\\uFE0F?|[\\u2934\\u2935]\\uFE0F?|[\\u3030\\u303D]\\uFE0F?|[\\u3297\\u3299]\\uFE0F?|[\\uD83C\\uDE01\\uD83C\\uDE02\\uD83C\\uDE1A\\uD83C\\uDE2F\\uD83C\\uDE32-\\uD83C\\uDE3A\\uD83C\\uDE50\\uD83C\\uDE51]\\uFE0F?|[\\u203C\\u2049]\\uFE0F?|[\\u25AA\\u25AB\\u25B6\\u25C0\\u25FB-\\u25FE]\\uFE0F?|[\\u00A9\\u00AE]\\uFE0F?|[\\u2122\\u2139]\\uFE0F?|\\uD83C\\uDC04\\uFE0F?|\\uD83C\\uDCCF\\uFE0F?|[\\u231A\\u231B\\u2328\\u23CF\\u23E9-\\u23F3\\u23F8-\\u23FA]\\uFE0F?)(?:[\\uD83C\\uDFFB-\\uD83C\\uDFFF]|[\\uD83E\\uDDB0-\\uD83E\\uDDB3])?"

val emojiRegex = "${emojiCharacter}(?:\\u200D${emojiCharacter})*".toRegex()

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

fun Canvas.drawImageRRect(image: Image, srcRect: Rect, rRect: RRect, paint: Paint? = null) {
    save()
    clipRRect(rRect, true)
    drawImageRect(image, srcRect, rRect, FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST), paint, true)
    restore()
}
fun Canvas.drawImageRRect(image: Image, rRect: RRect, paint: Paint? = null) =
    drawImageRRect(image, Rect(0f,0f,image.width.toFloat(),image.height.toFloat()), rRect, paint)

fun SVGDOM.makeImage(width: Float, height: Float): Image{
    setContainerSize(width, height)
    return Surface.makeRasterN32Premul(width.toInt(), height.toInt()).apply { render(canvas) }.makeImageSnapshot()
}

fun Surface.saveImage(path: String) = File(path).writeBytes(makeImageSnapshot().encodeToData()!!.bytes)
//fun Surface.saveImage(path: java.nio.file.Path) = path.writeBytes(makeImageSnapshot().encodeToData()!!.bytes)

fun Canvas.drawScaleWidthImage(image: Image, width: Float, x: Float, y: Float, paint: Paint = Paint()){
    val src = Rect.makeXYWH(0f, 0f, image.width.toFloat(), image.height.toFloat())
    val dst = Rect.makeXYWH(x, y, width, width * image.height / image.width)
    drawImageRect(image, src, dst, FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST), paint, true)
}

fun Canvas.drawScaleWidthImageOutline(image: Image, width: Float, x: Float, y: Float, isForward: Boolean = false, paint: Paint = Paint()){
    drawScaleWidthImage(image, width, x, y, paint)
    val dst = Rect.makeXYWH(x, y, width, width * image.height / image.width).toRRect()
    drawRRect(dst, Paint().apply {
        color = if (isForward) Color.BLUE else Color.GREEN
        mode = PaintMode.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    })
}

fun Rect.toRRect() =
    RRect.makeLTRB(left, top, right, bottom, 0f)

fun Rect.toRRect(radius: Float) =
    RRect.makeLTRB(left, top, right, bottom, radius)

fun RRect.offsetR(dx: Float, dy: Float): RRect {
    return RRect.makeComplexLTRB(left + dx, top +dy, right +dx, bottom+dy, radii)
}

fun Canvas.drawImageClip(
    image: Image,
    dstRect: RRect,
    paint: Paint? = null
){
    val ratio = image.width.toFloat() / image.height.toFloat()

    val srcRect = if (dstRect.width / ratio < dstRect.height) {
        val imgW = dstRect.width * image.height / dstRect.height
        val offsetX = (image.width - imgW) / 2f
        Rect.makeXYWH(offsetX, 0f, imgW, image.height.toFloat())
    } else {
        val imgH = dstRect.height * image.width / dstRect.width
        val offsetY = (image.height - imgH) / 2
        Rect.makeXYWH(0f, offsetY, image.width.toFloat(), imgH)
    }

    drawImageRRect(image, srcRect, dstRect, paint)
}

fun rgb2hsb(rgbR: Int, rgbG: Int, rgbB: Int): FloatArray {

    val rgb = intArrayOf(rgbR, rgbG, rgbB)
    Arrays.sort(rgb)
    val max = rgb[2]
    val min = rgb[0]
    val hsbB = max / 255.0f
    val hsbS: Float = if (max == 0) 0f else (max - min) / max.toFloat()
    var hsbH = 0f
    if (max == rgbR && rgbG >= rgbB) {
        hsbH = (rgbG - rgbB) * 60f / (max - min) + 0
    } else if (max == rgbR && rgbG < rgbB) {
        hsbH = (rgbG - rgbB) * 60f / (max - min) + 360
    } else if (max == rgbG) {
        hsbH = (rgbB - rgbR) * 60f / (max - min) + 120
    } else if (max == rgbB) {
        hsbH = (rgbR - rgbG) * 60f / (max - min) + 240
    }
    return floatArrayOf(hsbH, hsbS, hsbB)
}

fun hsb2rgb(h: Float, s: Float, v: Float): IntArray {
    var r = 0f
    var g = 0f
    var b = 0f
    val i = (h / 60 % 6).toInt()
    val f = h / 60 - i
    val p = v * (1 - s)
    val q = v * (1 - f * s)
    val t = v * (1 - (1 - f) * s)
    when (i) {
        0 -> {
            r = v
            g = t
            b = p
        }
        1 -> {
            r = q
            g = v
            b = p
        }
        2 -> {
            r = p
            g = v
            b = t
        }
        3 -> {
            r = p
            g = q
            b = v
        }
        4 -> {
            r = t
            g = p
            b = v
        }
        5 -> {
            r = v
            g = p
            b = q
        }
        else -> {}
    }
    return intArrayOf((r * 255.0).toInt(), (g * 255.0).toInt(), (b * 255.0).toInt())
}

