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

object LoginQrCodeDraw {

    val pointColor = 0xFF000000
    val bgColor = 0xFFFFFFFF

    fun qrCode(url: String): Image {
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
}

