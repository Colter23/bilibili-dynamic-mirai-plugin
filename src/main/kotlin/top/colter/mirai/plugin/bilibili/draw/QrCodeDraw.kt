package top.colter.mirai.plugin.bilibili.draw

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageConfig
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.jetbrains.skia.*
import org.jetbrains.skia.svg.SVGDOM
import org.jetbrains.skiko.toBitmap
import top.colter.mirai.plugin.bilibili.utils.loadResourceBytes

val pointColor = 0xFF000000
val bgColor = 0xFFFFFFFF

fun loginQrCode(url: String): Image {
    val qrCodeWriter = QRCodeWriter()

    val bitMatrix = qrCodeWriter.encode(
        url, BarcodeFormat.QR_CODE, 250, 250,
        mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H
        )
    )

    val config = MatrixToImageConfig(pointColor.toInt(), bgColor.toInt())

    return Surface.makeRasterN32Premul(250, 250).apply {
        canvas.apply {

            val img = Image.makeFromBitmap(MatrixToImageWriter.toBufferedImage(bitMatrix, config).toBitmap())
            drawImage(img, 0f, 0f)

            drawCircle(125f, 125f, 35f, Paint().apply {
                color = Color.WHITE
            })
            drawCircle(125f, 125f, 30f, Paint().apply {
                color = Color.makeRGB(2, 181, 218)
            })

            val svg = SVGDOM(Data.makeFromBytes(loadResourceBytes("icon/BILIBILI_LOGO.svg")))
            drawImage(svg.makeImage(40f, 40f), 105f, 105f, Paint().apply {
                colorFilter = ColorFilter.makeBlend(Color.WHITE, BlendMode.SRC_ATOP)
            })

        }
    }.makeImageSnapshot()
}


fun qrCode(url: String, width: Int, color: Int): Image {
    val qrCodeWriter = QRCodeWriter()

    val bitMatrix = qrCodeWriter.encode(
        url, BarcodeFormat.QR_CODE, width, width,
        mapOf(
            EncodeHintType.MARGIN to 0
        )
    )

    val c = Color.getRGB(color)
    val cc = c[0] + c[1] + c[2]
    val ccc = if (cc > 382) {
        val hsb = rgb2hsb(c[0], c[1], c[2])
        hsb[1] = if (hsb[1] + 0.25f > 1f) 1f else hsb[1] + 0.25f
        val rgb = hsb2rgb(hsb[0], hsb[1], hsb[2])
        Color.makeRGB(rgb[0], rgb[1], rgb[2])
    } else {
        color
    }

    val config = MatrixToImageConfig(ccc, Color.makeARGB(0, 255, 255, 255))

    return Image.makeFromBitmap(MatrixToImageWriter.toBufferedImage(bitMatrix, config).toBitmap())
}

