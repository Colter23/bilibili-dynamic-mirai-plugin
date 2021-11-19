package top.colter.mirai.plugin.bilibili.utils

import kotlinx.serialization.decodeFromString
import net.mamoe.mirai.utils.error
import top.colter.mirai.plugin.bilibili.*
import top.colter.mirai.plugin.bilibili.data.*
import top.colter.miraiplugin.utils.translate.trans
import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URL
import java.util.*
import javax.imageio.ImageIO

//object ImgUtils : CoroutineScope by PluginMain.childScope("ImageTasker"){
object ImgUtils{
    private val renderingHints = RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    private var font: Font

    private const val imgWidth = 800
    private const val contentMargin = 30

    private val emojiMap: MutableMap<String,BufferedImage> = mutableMapOf()

    init{
        renderingHints[RenderingHints.KEY_RENDERING] = RenderingHints.VALUE_RENDER_QUALITY

        // 初始化字体
        if (BiliPluginConfig.font.indexOf('.')!=-1){

            val fontList = BiliPluginConfig.font.split(".")

            val file = PluginMain.resolveDataFile("font/${BiliPluginConfig.font}")
            if (fontList.last()=="ttf"){
                font = Font.createFont(Font.TRUETYPE_FONT,file)
            }else{
                PluginMain.logger.error{ "不支持的字体类型" }
                font = Font("Microsoft Yahei", Font.PLAIN, 20)
            }
        }else {
//            val os = System.getProperty("os.name").lowercase(Locale.getDefault())
            font = Font(BiliPluginConfig.font, Font.PLAIN, 20)
        }
    }

    inline fun <reified T> String.decode(): T = json.decodeFromString(this)

    fun hex2Color(hex: String): Color {
        return if (hex.startsWith("#")){
            Color(Integer.valueOf(hex.substring(1,3),16),Integer.valueOf(hex.substring(3,5),16),Integer.valueOf(hex.substring(5),16))
        }else{
            Color.BLACK
        }
    }

    fun buildImageMessage(biList: List<BufferedImage>, profile: UserProfile?, time: String, color: String, fileStr: String): File {
        var height = 130
        biList.forEach { height+=it.height }

        val bi = BufferedImage(imgWidth, height, BufferedImage.TYPE_INT_ARGB)
        val g2 = bi.createGraphics()
        g2.setRenderingHints(renderingHints)

        background(g2, height, hex2Color(color))
        val user = profile?.user!!
        val pendant = profile.pendant?.image?: ""
        header(g2, user.uname,time,user.face,pendant)

        var g2Y = 110
        biList.forEach {
            g2.drawImage(it, 0, g2Y, null)
            g2Y += it.height
        }

        g2.dispose()

        val file = PluginMain.resolveDataFile(fileStr)
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        ImageIO.write(bi, "png", file)
        return file
    }

    fun buildReplyImageMessage(biList: List<BufferedImage>,user: UserInfo?): BufferedImage {
        var height = if (user == null) 20 else 80
        biList.forEach { height+=it.height }

        val bi = BufferedImage(imgWidth, height, BufferedImage.TYPE_INT_ARGB)
        val g2 = bi.createGraphics()
        g2.setRenderingHints(renderingHints)

        g2.color = Color(245, 245, 245)
        g2.fillRoundRect(0,0,imgWidth,height, 10,10)

        var g2Y = if (user == null) 20 else 55
        if (user != null){
            g2.clip = Ellipse2D.Double(25.0, 10.0, 40.0, 40.0)
            g2.drawImage(ImageIO.read(URL(imgApi(user.face,40,40))),25,10,null)
            g2.clip = null
            g2.stroke = BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.color = Color.WHITE
            g2.drawOval(25,10,40,40)

            g2.color = Color(0, 161, 214)
            g2.font = font.deriveFont(25f)
            g2.drawString(user.uname, 80, 40)
        }else{
            g2Y = 0
        }

        biList.forEach {
            g2.drawImage(it, 0, g2Y, null)
            g2Y += it.height
        }

        g2.dispose()

        val newBi = BufferedImage(imgWidth, height, BufferedImage.TYPE_INT_ARGB)
        val newG2 = newBi.createGraphics()
        newG2.setRenderingHints(renderingHints)
        newG2.drawImage(bi, 20,20,imgWidth-40,height*(imgWidth-40)/imgWidth,null)
        newG2.dispose()
        return newBi
    }

    fun buildLiveImageMessage(title: String, coverUrl: String, uname: String, faceUrl: String, color: String, fileStr: String): File {
        val bi = BufferedImage(imgWidth, 570, BufferedImage.TYPE_INT_ARGB)
        val g2 = bi.createGraphics()
        g2.setRenderingHints(renderingHints)

        background(g2, 570, hex2Color(color))
        header(g2, title,uname,faceUrl,"",-20,false)

        g2.color = Color(251, 114, 153)
        g2.fillRoundRect(722, 50,52,27,5,5)
        g2.color = Color.WHITE
        g2.font = font.deriveFont(20f)
        g2.drawString("直播", 728, 70)

        val margin = 10
        val rw = imgWidth-margin*2
        val rh = 570-margin*2
        g2.clip = RoundRectangle2D.Double(margin-1.0,margin-1.0, rw+1.0, rh+1.0, 20.0, 20.0)
        g2.drawImage(ImageIO.read(URL(imgApi(coverUrl,780,440))),10,120,780,440,null)
//        g2.drawImg(coverUrl, 10,120,780,440)
        g2.dispose()

        val file = PluginMain.resolveDataFile(fileStr)
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        ImageIO.write(bi, "png", file)
        return file
    }

    private fun background(bgG2: Graphics2D, height: Int, color: Color) {
        bgG2.color = color
        bgG2.fillRect(0,0,imgWidth,height)

        val margin = 10
        val rw = imgWidth-margin*2
        val rh = height-margin*2
        bgG2.color = Color.WHITE
        bgG2.fillRoundRect(margin,margin, rw, rh, 20, 20)
        bgG2.color = Color(238, 238, 238)
        bgG2.drawRoundRect(margin-1,margin-1, rw+1, rh+1, 20, 20)

    }

    private fun header(headerG2: Graphics2D, rowOne: String, rowTwo: String, faceUrl: String, pendentUrl: String = "", offset:Int=0, c: Boolean = true) {

        val x = 50 + offset
        headerG2.clip = Ellipse2D.Double(x.toDouble(), 30.0, 60.0, 60.0)
        headerG2.drawImg(faceUrl,x,30,60,60)
        headerG2.clip = null
        headerG2.stroke = BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        headerG2.color = Color.WHITE
        headerG2.drawOval(x,30,60,60)

        if (pendentUrl != "")
            headerG2.drawImg(pendentUrl,30+offset,10,100,100)

        headerG2.color = if(c) Color(251, 114, 153) else Color.BLACK
        headerG2.font = font.deriveFont(30f)
        headerG2.writeText(rowOne, 130+offset,60,600,1)
//        headerG2.drawString(name, 130, 60)
        headerG2.font = font.deriveFont(20f)
        headerG2.color = Color(148, 147, 147)
        headerG2.drawString(rowTwo, 130+offset, 90)
    }


    fun textContent(text: String, emojiList: List<EmojiDetails>? = null): BufferedImage? {
        if (text == "") return null

        val textBi =  BufferedImage(800, 2000, BufferedImage.TYPE_INT_ARGB)
        val textG2 = textBi.createGraphics()
        textG2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP)
        textG2.color = Color.BLACK
        textG2.font = font.deriveFont(25f)

        emojiList?.forEach {
            if (!emojiMap.containsKey(it.emojiName)){
                emojiMap[it.emojiName] = ImageIO.read(URL(imgApi(it.url,30,30)))
            }
        }

        val tran = trans(text)
        var msgText = text
        if (tran != null){
            msgText += "\n\n〓〓 翻译 〓〓\n$tran"
        }

        var textX = 35
        var textY = 35
        var emojiStart = 0
        var emojiFlag = false
        for((i, c) in msgText.withIndex()){
            val cs = c.toString()
            if (c == '[') {
                emojiStart = i
                emojiFlag = true
            }else if (c == '\n'){
                textX = 35
                textY += 35
            }else if (c == ']'){
                val emojiText = msgText.substring(emojiStart, i+1)
                textX += try{
                    val emoji = emojiMap[emojiText] ?: throw Exception()
                    textG2.drawImage(emoji, textX, textY-23, null)
                    35
                }catch (e: Exception){
                    textG2.drawString("\uD83D\uDE13", textX+2, textY)
                    textG2.getStrWidth("\uD83D\uDE13",4)
                }
                emojiFlag = false
            }else if (!emojiFlag){
                if (textX > 740){
                    textX = 35
                    textY += 35
                }
                if (cs.matches("[\u4e00-\u9fa5]".toRegex())){
                    textG2.drawString(cs, ++textX, textY)
                    textX++
                }else textG2.drawString(cs, textX, textY)
                textX += textG2.getStrWidth(cs)
            }
        }

        textG2.dispose()
        return textBi.getSubimage(0,0,textBi.width,textY+15)
    }

    fun imageContent(pictures: List<DynamicPictureInfo>): BufferedImage {
        val imgBi = BufferedImage(imgWidth, 2000, BufferedImage.TYPE_INT_ARGB)
        val imgG2 = imgBi.createGraphics()
        imgG2.setRenderingHints(renderingHints)

        val picArc = 20
        var picH = 20
        val picPadding = 10
        var picX = contentMargin

        val imgRowCount = if (pictures.size>=3) 3 else pictures.size
        var picWidth = ((imgWidth-contentMargin*2)-picPadding*(imgRowCount-1))/imgRowCount
        var picHeight = picWidth
        for ((i,pic) in pictures.withIndex()){
             if (pictures.size == 1){
                 if (pic.width < picWidth){
                     picHeight = if (pic.height > picWidth) picWidth else pic.height
                     picWidth = pic.width
                 }else if (pic.height*picWidth/pic.width > picWidth) {
                    picHeight = picWidth
                }else {
                    picHeight = pic.height*picWidth/pic.width
                }
            }
            imgG2.clip = RoundRectangle2D.Double(
                picX.toDouble(), picH.toDouble(), picWidth.toDouble(), picHeight.toDouble(),
                picArc.toDouble(), picArc.toDouble())
            imgG2.drawImg(pic.source, picX, picH, picWidth, picHeight)
            imgG2.clip = null
            imgG2.stroke = BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            imgG2.color = Color.WHITE
            imgG2.drawRoundRect(picX,picH,picWidth,picHeight,picArc,picArc)
            picX += (picPadding+picWidth)
            if(i%3 == 2) {
                picH += if(i!=pictures.size-1) picPadding+picHeight else 0
                picX = contentMargin
            }
        }
        picH += picHeight
        imgG2.dispose()
        return imgBi.getSubimage(0,0,imgBi.width, picH+20)
    }

    fun videoContent(coverUrl: String, title: String, desc: String, tag: String = ""): BufferedImage {
        val videoBi = BufferedImage(imgWidth, 170, BufferedImage.TYPE_INT_ARGB)
        val videoG2 = videoBi.createGraphics()
        videoG2.setRenderingHints(renderingHints)

        val picArc = 10
        val cardWidth = imgWidth-contentMargin*2
        val cardHeight = 150
        val imgW = 240

        videoG2.color = Color.WHITE
        videoG2.fillRoundRect(contentMargin, 10,cardWidth,cardHeight, picArc, picArc)
        videoG2.clip = RoundRectangle2D.Double(contentMargin.toDouble(), 10.0,
            imgW.toDouble(), cardHeight.toDouble(), picArc.toDouble(), picArc.toDouble())
        videoG2.drawImg(coverUrl, contentMargin, 10,imgW,cardHeight)
        videoG2.clip = null
        videoG2.stroke = BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        videoG2.drawRoundRect(contentMargin, 10,imgW,cardHeight,picArc,picArc)

        if (tag != ""){
            videoG2.color = Color(251, 114, 153)
            videoG2.fillRoundRect(214, 20,45,22,5,5)
            videoG2.color = Color.WHITE
            videoG2.font = font.deriveFont(16f)
            videoG2.drawString(tag, 220, 36)
        }

        videoG2.color = Color.BLACK
        videoG2.font = font.deriveFont( 20f)
        val textY = videoG2.writeText(title, 285, 45, 465, 2)

        videoG2.color = Color(148, 147, 147)
        videoG2.font = font.deriveFont(16f)
        videoG2.writeText(desc, 285, textY+25, 465, 3)

        videoG2.stroke = BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        videoG2.color = Color(229, 233, 239)
        videoG2.drawRoundRect(contentMargin, 10,cardWidth,cardHeight,picArc,picArc)

        videoG2.dispose()
        return videoBi
    }

    fun articleContent(imageUrls: List<String>,title: String, desc: String): BufferedImage {
        val articleBi = BufferedImage(imgWidth, 300, BufferedImage.TYPE_INT_ARGB)
        val articleG2 = articleBi.createGraphics()
        articleG2.setRenderingHints(renderingHints)

        val picArc = 10
        val cardWidth = imgWidth-contentMargin*2
        val cardHeight = 275
        val imgHeight = 170

        articleG2.color = Color.WHITE
        articleG2.clip = RoundRectangle2D.Double(contentMargin.toDouble(), 10.0,
            cardWidth.toDouble(), cardHeight.toDouble(), picArc.toDouble(), picArc.toDouble())
        articleG2.fillRect(contentMargin, 10,cardWidth,cardHeight)
        if (imageUrls.size == 3){
            var imgX = contentMargin
            val imgW = cardWidth/3-4
            imageUrls.forEach {
                articleG2.drawImg(it, imgX, 10,imgW,imgHeight)
                imgX += cardWidth/3+2
            }
        }else{
            articleG2.drawImage(ImageIO.read(URL(imgApi(imageUrls[0],cardWidth,imgHeight))),contentMargin,10,cardWidth,imgHeight,null)
        }
        articleG2.clip = null
        articleG2.stroke = BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        articleG2.color = Color.WHITE
        articleG2.drawRoundRect(contentMargin, 10,cardWidth,cardHeight,picArc,picArc)

        articleG2.color = Color.BLACK
        articleG2.font = font.deriveFont( 20f)
        val textY = articleG2.writeText(title, 45, 210, cardWidth-30, 1)

        articleG2.font = font.deriveFont( 16f)
        articleG2.color = Color(148, 147, 147)
        articleG2.writeText(desc, 45, textY+25, cardWidth-30, 3)

        articleG2.stroke = BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        articleG2.color = Color(229, 233, 239)
        articleG2.drawRoundRect(contentMargin, 10,cardWidth,cardHeight,picArc,picArc)

        articleG2.dispose()
        return articleBi
    }

    fun musicContent(coverUrl: String, title: String, desc: String, isMusic: Boolean = true): BufferedImage {
        val musicBi = BufferedImage(imgWidth, 120, BufferedImage.TYPE_INT_ARGB)
        val musicG2 = musicBi.createGraphics()
        musicG2.setRenderingHints(renderingHints)

        val picArc = 10
        val cardWidth = imgWidth-contentMargin*2
        val cardHeight = 100

        musicG2.color = Color.WHITE
        musicG2.clip = RoundRectangle2D.Double(contentMargin.toDouble(), 10.0,
            cardWidth.toDouble(), cardHeight.toDouble(), picArc.toDouble(), picArc.toDouble())
        musicG2.fillRect(contentMargin, 10,cardWidth,cardHeight)
        musicG2.drawImg(coverUrl, contentMargin, 10,cardHeight,cardHeight)
        if (isMusic){
            // (╬▔皿▔)╯ 离谱
            val playImgByte = Base64.getDecoder().decode("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")
            val playImg = ImageIO.read(ByteArrayInputStream(playImgByte))
            musicG2.drawImage(playImg, contentMargin+7, 20,null)
        }
        musicG2.clip = null
        musicG2.stroke = BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        musicG2.color = Color.WHITE
        musicG2.drawRoundRect(contentMargin, 10,cardWidth,cardHeight,picArc,picArc)

        musicG2.color = Color.BLACK
        musicG2.font = font.deriveFont( 20f)
        val textY = musicG2.writeText(title, 155, 50, 470, 1)

        musicG2.font = font.deriveFont(16f)
        musicG2.color = Color(148, 147, 147)
        musicG2.writeText(desc, 155, textY+30, 470, 1)

        musicG2.stroke = BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        musicG2.color = Color(229, 233, 239)
        musicG2.drawRoundRect(contentMargin+1, 10+1,cardWidth-5,cardHeight-2,picArc,picArc)

        musicG2.dispose()
        return musicBi
    }

    fun infoContent(text: String): BufferedImage {
        val infoBi = BufferedImage(imgWidth, 30, BufferedImage.TYPE_INT_ARGB)
        val infoG2 = infoBi.createGraphics()
        infoG2.setRenderingHints(renderingHints)
        infoG2.font = font.deriveFont( 25f)
        infoG2.color = Color.BLACK
        infoG2.drawString(text, contentMargin, 10)
        infoG2.dispose()
        return infoBi
    }

    fun Graphics2D.writeText(t: String, x: Int, y: Int, rowL: Int, rowCount: Int): Int {
        var rowLength = 0
        var textX = x
        var textY = y
        var textRow = 1
        val text = t.replace("\n"," ")

        for (c in text){
            val l = getStrWidth(c.toString())
            rowLength += l
            if (rowLength >= rowL){
                if (textRow == rowCount){
                    drawString("...", textX, textY)
                    break
                }else{
                    drawString(c.toString(), textX, textY)
                }
                rowLength = 0
                textX = x
                textY += font.size+3
                textRow++
            }else{
                drawString(c.toString(), textX, textY)
                textX += l
            }
        }
        return textY
    }

    fun Graphics2D.getStrWidth(str: String, plus: Int = 0): Int {
        return font.getStringBounds(str, fontRenderContext).width.toInt() + plus
    }

    fun Graphics2D.drawImg(url: String,x: Int,y: Int,w: Int,h: Int){
        runCatching {
            drawImage(ImageIO.read(URL(imgApi(url,w,h))),x,y,null)
        }.onFailure {
            println(it.message)
        }
    }

    fun imgApi(imgUrl: String, width: Int, height: Int): String {
        return "${imgUrl}@${width}w_${height}h_1e_1c.png"
    }
}
