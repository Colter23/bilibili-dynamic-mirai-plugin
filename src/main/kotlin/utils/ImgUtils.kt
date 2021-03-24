package top.colter.mirai.plugin.utils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import top.colter.mirai.plugin.PluginConfig
import top.colter.mirai.plugin.PluginConfig.BPI
import top.colter.mirai.plugin.PluginConfig.basePath
import top.colter.mirai.plugin.PluginData
import top.colter.mirai.plugin.PluginData.runPath
import top.colter.mirai.plugin.PluginMain
import top.colter.mirai.plugin.bean.Dynamic
import top.colter.mirai.plugin.bean.User
import top.colter.myplugin.translate.TransApi
import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.*
import java.net.URL
import java.text.SimpleDateFormat
import javax.imageio.ImageIO
import kotlin.math.ceil
import java.io.IOException

import java.io.ByteArrayInputStream

import java.io.ByteArrayOutputStream

suspend fun simpleBuildMessageImage(msg: String, uid: String): String?{
    val dynamic = Dynamic()
    dynamic.content = msg
    dynamic.timestamp = System.currentTimeMillis()/1000
    dynamic.did = "测试ID:000000000000000000"
    dynamic.info = "000000000000000000"
    dynamic.isDynamic = true
    return buildMessageImage(dynamic,uid);
}

/**
 * 构建动态信息图片
 */
suspend fun buildMessageImage(dynamic: Dynamic, uid: String): String? {
    var msg = dynamic.content
    //文本翻译
    if (PluginConfig.baiduTranslate["enable"]=="true"){
        if (PluginConfig.baiduTranslate["SECURITY_KEY"]!=""){
            try {
                val api = TransApi(PluginConfig.baiduTranslate["APP_ID"]!!, PluginConfig.baiduTranslate["SECURITY_KEY"]!!)
                val resMsg = JSON.parseObject(api.getTransResult(msg, "auto", "zh"))
                if (resMsg.getString("from")!="zh") {
                    msg += "\n\n翻译: \n"
                    for (item in resMsg.getJSONArray("trans_result")){
                        msg+=(item as JSONObject).getString("dst")
                        msg+="\n"
                    }
                }
            }catch (e: Exception){
                PluginMain.logger.error("Baidu translation failure! 百度翻译失败!")
            }
        }else{
            PluginMain.logger.error("Baidu translation API not configured! 未配置百度翻译API")
        }
    }


    //统计最终图片所需的高度
    var height = 0
    //获取一张基础背景图
    val bg = ImageIO.read(File("$runPath$basePath/img/template/${uid}.png"))

    //构建一张画布以供裁剪
    val bi = BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB)
    val g2 : Graphics2D = bi.graphics as Graphics2D
    g2.drawImage(bg, 0, 0, null) //画入背景
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP)

    //最终图片片段列表
    val biList = mutableListOf<BufferedImage>()

    //裁剪图片头 并写入时间信息
    val topBi = bi.getSubimage(0, 0, 1920, 370);
    height += 370
    biList.add(topBi)
    val topG2 : Graphics2D = topBi.graphics as Graphics2D
    topG2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP)

    topG2.font = Font("微软雅黑", Font.BOLD, 70)
    topG2.color = Color(148, 147, 147)
    val timestamp :Long = dynamic.timestamp*1000
    topG2.drawString(SimpleDateFormat("yyyy.MM.dd  HH:mm:ss").format(timestamp), 500, 300)

    //加一条空白区域
    height += 70
    biList.add(bi.getSubimage(0, 400, 1920, 70))

    //处理动态内容
    val stringList = mutableListOf<String>()
    val msgText = msg

    val textBi =  BufferedImage(1920, 70, BufferedImage.TYPE_INT_RGB)
    val textG2 : Graphics2D = textBi.graphics as Graphics2D
    textG2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP)
    textG2.font = Font("微软雅黑", Font.PLAIN, 60)

    // 文字换行处理
    var l = 0
    var tin = false
    var start = 0
    var fl = 0
    for((i, c) in msgText.withIndex()){
        if (c == '\n'){
            stringList.add(msgText.substring(start, i))
            start = i+1
            l = 0
        }
        if (c == '[') tin = true
        if (!tin) fl = textG2.font.getStringBounds(c.toString(), textG2.fontRenderContext).width.toInt()
        if (c == ']'){
            tin = false
            l += 65
        }
        if (l+fl>1680){
            stringList.add(msgText.substring(start, i + 1))
            start = i+1
            fl = 0
            l = 0
        }else{
            l += fl
            fl = 0
        }
    }
    stringList.add(msgText.substring(start))

    //构建动态内容图片片段
    for (index in 1..stringList.size){
        val centerBi =  BufferedImage(1920, 70, BufferedImage.TYPE_INT_RGB)
        val centerG2 : Graphics2D = centerBi.graphics as Graphics2D
        centerG2.drawImage(bi.getSubimage(0, 470, 1920, 70), 0, 0, null)
        centerG2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP)

        centerG2.font = Font("微软雅黑", Font.PLAIN, 60)
        centerG2.color = Color(87, 87, 87)

        height += 70
        biList.add(centerBi)

        var start = 0
        var end = 0
        var text = stringList[index - 1]
        var x = 100
        //解析b站表情 [tv_doge]
        while (text.indexOf('[')!=-1){
            start = text.indexOf('[')
            end = text.indexOf(']')+1
            val tempText = text.substring(0, start)
            val tempSimp = text.substring(start, end)
            text = text.substring(end)

            if (start != 0){
                centerG2.drawString(tempText, x, 50)
                //计算字符串像素长度 用于绘制表情
                x += centerG2.font.getStringBounds(tempText, centerG2.fontRenderContext).width.toInt()
            }
            try{
                val emoji = PluginMain.emojiMap[tempSimp]
                val reEmoji = emoji?.getScaledInstance(65, 65, java.awt.Image.SCALE_DEFAULT)
                centerG2.drawImage(reEmoji, x, 0, null)
            }catch (e: Exception){

            }
            x+=65
        }
        centerG2.drawString(text, x, 50)
    }

    //如动态有图片则添加图片
    try{
        if (dynamic.pictures != null && dynamic.pictures?.size!=0){
            for (imgSrc in dynamic.pictures!!){
                val img = ImageIO.read(URL(imgSrc))
                val reHeight = (img.height*1720.0)/img.width
                val reImg = img.getScaledInstance(1720, reHeight.toInt(), java.awt.Image.SCALE_DEFAULT)
                val row : Int = ceil(reHeight / 70.0).toInt()
                val imgBi = BufferedImage(1920, row * 70, BufferedImage.TYPE_INT_RGB)
                val imgG2 = imgBi.graphics as Graphics2D
                for (index in 1..row) {
                    imgG2.drawImage(bi.getSubimage(0, 470, 1920, 70), 0, (index - 1) * 70, null)
                }
                imgG2.drawImage(reImg, 100, 30, null)
                height += row*70
                biList.add(imgBi)
            }
        }
    }catch (e: Exception){
        PluginMain.logger.error("绘制图片失败")
    }

    //内容底部留空
    height += 70
    biList.add(bi.getSubimage(0, 400, 1920, 70))

    //构建底部图片片段 写入动态ID
    val bottomBi = bi.getSubimage(0, 995, 1920, 85)
    val bottomG2 : Graphics2D = bottomBi.graphics as Graphics2D
    bottomG2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP)
    height += 85
    biList.add(bottomBi)
    bottomG2.font = Font("微软雅黑", Font.BOLD, 45)
    bottomG2.color = Color(217, 217, 217)
    bottomG2.drawString(dynamic.info, 80, 39)

    //构建最终图片
    val endBi = BufferedImage(1920, height, BufferedImage.TYPE_INT_RGB)
    val endG2 : Graphics2D = endBi.graphics as Graphics2D
    var preY = 0
    for (bi in biList){
        endG2.drawImage(bi, 0, preY, null)
        preY += bi.height
    }

    //把图片写入文件
    if (PluginConfig.dynamic["saveDynamicImage"]=="true") {
        var path = ""
        path = if (dynamic.isDynamic){
            "$runPath$basePath/img/dynamic/${uid}/${dynamic.did}.png"
        }else{
            "$runPath$basePath/img/live/${uid}/${SimpleDateFormat("yyMMdd-HHmm").format(timestamp)}.png"
        }
        val file = File(path)
        if (!file.parentFile.exists()){
            file.parentFile.mkdirs()
        }
        try{
            ImageIO.write(endBi, "PNG", file)
        }catch (e: Exception){
            PluginMain.logger.error("储存图片失败")
        }
    }
    return getImageIdByBi(endBi)
}

/**
 * 生成模板图
 */
suspend fun generateImg(uid: String, name: String, face: String, pendant: String, hex: String): String? {
    if (hex == ""){
        try {
            ImageIO.read(File("$runPath$basePath/img/template/$uid.png"))
            return simpleBuildMessageImage("订阅成功",uid)
        }catch (e:Exception){}
    }

    val bi = BufferedImage(1920, 1080, BufferedImage.TYPE_4BYTE_ABGR)
    val g2 : Graphics2D = bi.graphics as Graphics2D
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP)

    val faceImg = ImageIO.read(URL(face))
    if (hex == ""){
        g2.color = getImageAvgRGB(faceImg)
    }else{
        var hex16 = hex
        if (hex[0] == '#'){
            hex16 = hex.substring(1)
        }

        g2.color = Color(
            Integer.parseInt(hex16.substring(0, 2), 16),
            Integer.parseInt(hex16.substring(2, 4), 16),
            Integer.parseInt(hex16.substring(4), 16)
        )
    }
    g2.fillRect(0,0,1920,1080)

    val margin = 35
    g2.color = Color(254, 247, 249)
    g2.fillRoundRect(margin,margin,1920-margin*2,1080-margin*2,20,20)

    g2.font = Font("汉仪汉黑W", Font.PLAIN, 130)
    g2.color = Color(61, 61, 61)
    g2.drawString(name, 480, 200)

    g2.font = Font("微软雅黑", Font.BOLD, 45)
    g2.color = Color(220, 220, 220)
    g2.drawString("@Colter", 1685, 1035)

    var width = 195
    if (pendant==""){
        width = 240
    }

    val reImg = scaleByPercentage(faceImg,width,width)

    val faceBi = BufferedImage(width, width, BufferedImage.TYPE_4BYTE_ABGR)
    val faceG2 : Graphics2D = faceBi.graphics as Graphics2D
    faceG2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    val shape = Ellipse2D.Double(0.0, 0.0, width.toDouble(), width.toDouble())
    faceG2.clip = shape
    faceG2.drawImage(reImg, 0, 0, null)
    faceG2.dispose();

    if (pendant==""){
        g2.drawImage(faceBi, 200, 80, null)
    }else{
        g2.drawImage(faceBi, 200, 100, null)
    }

    if (pendant!=""){
        val pendantImg = ImageIO.read(URL(pendant))
        val rePendant = scaleByPercentage(pendantImg,330,330)
        g2.drawImage(rePendant, 130, 40, null)
    }

    val file = File("$runPath$basePath/img/template/$uid.png")
    if (!file.parentFile.exists()){
        file.parentFile.mkdirs()
    }
    //把图片写入文件
    try{
        ImageIO.write(bi, "PNG", file)
    }catch (e: Exception){
        PluginMain.logger.error("储存图片失败")
    }

//    g2.font = Font("汉仪汉黑W", Font.PLAIN, 200)
//    g2.color = Color(61, 61, 61)
//    g2.drawString("订阅成功", 400, 400)

    return getImageIdByBi(bi)
}

/**
 * 获取图片主题色
 */
fun getImageAvgRGB(image: Image): Color {

//    val scaledImage = image.getScaledInstance(image.getWidth(null)*0.3.toInt(),image.getHeight(null)*0.3.toInt(), Image.SCALE_SMOOTH)
    val scaledImage = image.getScaledInstance(100,100, Image.SCALE_SMOOTH)

    val bi = BufferedImage(100, 100, BufferedImage.TYPE_4BYTE_ABGR)
    val g2 : Graphics2D = bi.graphics as Graphics2D
    g2.drawImage(scaledImage,0,0,null)

    val w = bi.width
    val h = bi.height
    val dots = floatArrayOf(0.15f, 0.35f, 0.5f, 0.7f, 0.85f)
    var R = 0
    var G = 0
    var B = 0
    for (dw in dots) {
        for (dh in dots) {
            val rgbVal = bi.getRGB((w * dw).toInt(), (h * dh).toInt())
            val color = Color(rgbVal)
            R += color.red
            G += color.green
            B += color.blue
        }
    }
    R += 2550
    G += 2550
    B += 2550
    val cn = dots.size * dots.size + 10
    return Color(R / cn, G / cn, B / cn)
}

suspend fun getImageIdByBi(bi : BufferedImage):String?{
    val input = biToIs(bi)
    val er = input?.toExternalResource()
    return try {
        er?.let { PluginMain.bot.getGroup(PluginConfig.adminGroup)?.uploadImage(it)?.imageId }
    } catch (e: Exception) {
        PluginMain.logger.error("上传图片失败")
        null
    } finally {
        input?.close()
        er?.close()
    }
}

/**
 * 将BufferedImage转换为InputStream
 * @param image
 * @return
 */
fun biToIs(image: BufferedImage?): InputStream? {
    val os = ByteArrayOutputStream()
    try {
        ImageIO.write(image, "PNG", os)
        return ByteArrayInputStream(os.toByteArray())
    } catch (e: IOException) {
        PluginMain.logger.error("BufferedImage转换失败")
    }
    return null
}

/**
 * 缩放图片
 */
fun scaleByPercentage(inputImage: BufferedImage, newWidth: Int, newHeight: Int): BufferedImage? {
    // 获取原始图像透明度类型
    try {
        val type = inputImage.colorModel.transparency
        val width = inputImage.width
        val height = inputImage.height
        // 开启抗锯齿
        val renderingHints = RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        // 使用高质量压缩
        renderingHints[RenderingHints.KEY_RENDERING] = RenderingHints.VALUE_RENDER_QUALITY
        val img = BufferedImage(newWidth, newHeight, type)
        val graphics2d = img.createGraphics()
        graphics2d.setRenderingHints(renderingHints)
        graphics2d.drawImage(inputImage, 0, 0, newWidth, newHeight, 0, 0, width, height, null)
        graphics2d.dispose()
        return img
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return null
}