package top.colter.mirai.plugin.bilibili

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import top.colter.mirai.plugin.bilibili.data.*
import top.colter.mirai.plugin.bilibili.utils.ImgUtils
import top.colter.mirai.plugin.bilibili.utils.ImgUtils.decode
import top.colter.mirai.plugin.bilibili.utils.decode
import top.colter.mirai.plugin.bilibili.utils.json
import top.colter.miraiplugin.utils.translate.trans
import java.awt.image.BufferedImage
import java.io.InputStream
import java.net.URL
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> String.decode(): T = json.decodeFromString(this)

val DynamicInfo.did get() = describe.dynamicId
val DynamicInfo.uid get() = describe.uid
val DynamicInfo.profile get() = describe.profile?.decode<UserProfile>()
val DynamicInfo.uname get() = profile?.user?.uname
val DynamicInfo.timestamp get() = describe.timestamp
val DynamicInfo.type get() = describe.type
val DynamicInfo.originType get() = describe.originType
val DynamicInfo.time: String
    get() = DateTimeFormatter.ofPattern("yyyy年MM月dd日  HH:mm:ss")
        .format(LocalDateTime.ofEpochSecond(describe.timestamp, 0, OffsetDateTime.now().offset))

fun String.content(type: Int, dynamicInfo: DynamicInfo): String {
    return when (type) {
        DynamicType.REPLY -> decode<DynamicReply>().getContent(dynamicInfo)
        DynamicType.PICTURE -> decode<DynamicPicture>().getContent(dynamicInfo)
        DynamicType.TEXT -> decode<DynamicText>().getContent(dynamicInfo)
        DynamicType.VIDEO -> decode<DynamicVideo>().getContent(dynamicInfo)
        DynamicType.ARTICLE -> decode<DynamicArticle>().getContent(dynamicInfo)
        DynamicType.MUSIC -> decode<DynamicMusic>().getContent(dynamicInfo)
        DynamicType.EPISODE -> decode<DynamicEpisode>().getContent(dynamicInfo)
        DynamicType.DELETE -> "源动态已被作者删除"
        DynamicType.SKETCH -> decode<DynamicSketch>().getContent(dynamicInfo)
        DynamicType.LIVE, DynamicType.LIVE_ING -> decode<DynamicLive>().getContent(dynamicInfo)
        DynamicType.LIVE_END -> "直播结束了"
        else -> {
            dynamicInfo.link = "https://t.bilibili.com/${dynamicInfo.did}"
            "不支持此类型${type}"
        }
    }
}

fun DynamicReply.getContent(dynamicInfo: DynamicInfo): String {
    if (dynamicInfo.link == "") {
        dynamicInfo.link = "https://t.bilibili.com/${dynamicInfo.did}"
    }
    return buildString {
        appendLine(detail.content)
        appendLine("〓 转发 〓 ${originUser?.user?.uname} 〓")
        append(origin.content(detail.originType, dynamicInfo))
    }
}

fun DynamicPicture.getContent(dynamicInfo: DynamicInfo): String {
    if (dynamicInfo.link == "") {
        dynamicInfo.link = "https://t.bilibili.com/${dynamicInfo.did}"
    }
    dynamicInfo.images.addAll(detail.pictures.map { it.source })
    return detail.description
}

fun DynamicText.getContent(dynamicInfo: DynamicInfo): String {
    if (dynamicInfo.link == "") {
        dynamicInfo.link = "https://t.bilibili.com/${dynamicInfo.did}"
    }
    return detail.content
}

fun DynamicVideo.getContent(dynamicInfo: DynamicInfo): String {
    if (dynamicInfo.link == "") {
        dynamicInfo.link = "https://www.bilibili.com/video/av$aid"
    }
    dynamicInfo.images.add(cover)
    return buildString {
        appendLine("视频: $title")
        if (dynamic != "") {
            append("动态: $dynamic")
        }
    }
}

fun DynamicArticle.getContent(dynamicInfo: DynamicInfo): String {
    if (dynamicInfo.link == "") {
        dynamicInfo.link = "https://www.bilibili.com/read/cv$id"
    }
    if (bannerUrl != "") {
        dynamicInfo.images.add(bannerUrl)
    } else {
        dynamicInfo.images.add(images[0])
    }
    return buildString {
        appendLine("专栏: $title")
        append("简介: $summary")
    }
}

fun DynamicMusic.getContent(dynamicInfo: DynamicInfo): String {
    if (dynamicInfo.link == "") {
        dynamicInfo.link = "https://www.bilibili.com/audio/au$id"
    }
    if (cover != "") {
        dynamicInfo.images.add(cover)
    }
    return buildString {
        appendLine("歌: $title")
        if (intro != "") {
            append("动态: $intro")
        }
    }
}

fun DynamicEpisode.getContent(dynamicInfo: DynamicInfo): String {
    if (dynamicInfo.link == "") {
        dynamicInfo.link = "https://t.bilibili.com/${dynamicInfo.did}"
    }
    if (cover != "") {
        dynamicInfo.images.add(cover)
    }
    return buildString {
        append("${season.type}: ${season.title}")
    }
}

fun DynamicSketch.getContent(dynamicInfo: DynamicInfo): String {
    if (dynamicInfo.link == "") {
        dynamicInfo.link = "https://t.bilibili.com/${dynamicInfo.did}"
    }
    return buildString {
        appendLine("${detail.description}: ${detail.title}")
        append("动态: ${vest.content}")
    }
}

fun DynamicLive.getContent(dynamicInfo: DynamicInfo): String {
    if (dynamicInfo.link == "") {
        dynamicInfo.link = "https://live.bilibili.com/$roomId"
    }
    if (cover != "") {
        dynamicInfo.images.add(cover)
    }
    return buildString {
        append("直播: $title")
    }
}



fun String.buildContent(type: Int, dynamicInfo: DynamicInfo): List<BufferedImage> {
    return when (type) {
        DynamicType.REPLY -> decode<DynamicReply>().bufferedImages(dynamicInfo)
        DynamicType.PICTURE -> decode<DynamicPicture>().bufferedImages(dynamicInfo)
        DynamicType.TEXT -> decode<DynamicText>().bufferedImages(dynamicInfo)
        DynamicType.VIDEO -> decode<DynamicVideo>().bufferedImages(dynamicInfo)
        DynamicType.ARTICLE -> decode<DynamicArticle>().bufferedImages(dynamicInfo)
        DynamicType.MUSIC -> decode<DynamicMusic>().bufferedImages(dynamicInfo)
        DynamicType.EPISODE -> decode<DynamicEpisode>().bufferedImages(dynamicInfo)
        DynamicType.DELETE -> listOf(ImgUtils.infoContent("源动态已被作者删除"))
        DynamicType.SKETCH -> decode<DynamicSketch>().bufferedImages(dynamicInfo)
        DynamicType.LIVE, DynamicType.LIVE_ING -> listOf(ImgUtils.infoContent("直播"))
        DynamicType.LIVE_END -> listOf(ImgUtils.infoContent("直播结束了"))
        else -> {
            if (dynamicInfo.link == "") {
                dynamicInfo.link = "https://t.bilibili.com/${dynamicInfo.did}"
            }
            listOf(ImgUtils.infoContent("不支持此类型动态 type:${type}"))
        }
    }
}


fun DynamicReply.bufferedImages(dynamicInfo: DynamicInfo): List<BufferedImage> {
    val biList = mutableListOf<BufferedImage>()
    val emojiList: MutableList<EmojiDetails> = mutableListOf()
    dynamicInfo.display.emojiInfo?.emojiDetails?.let { emojiList.addAll(it) }
    dynamicInfo.display.origin?.emojiInfo?.emojiDetails?.let { emojiList.addAll(it) }

    ImgUtils.textContent(detail.content, emojiList)?.let { biList.add(it) }
    if (dynamicInfo.link == "") {
        dynamicInfo.content = "转发动态"
        dynamicInfo.link = "https://t.bilibili.com/${dynamicInfo.did}"
    }
    val biL = origin.buildContent(detail.originType, dynamicInfo)
    biList.add(ImgUtils.buildReplyImageMessage(biL, originUser?.user))

    return biList.toList()
}

fun DynamicPicture.bufferedImages(dynamicInfo: DynamicInfo): List<BufferedImage> {
    val biList = mutableListOf<BufferedImage>()
    ImgUtils.textContent(detail.description, dynamicInfo.display.emojiInfo?.emojiDetails)?.let { biList.add(it) }
    biList.add(ImgUtils.imageContent(detail.pictures))
    if (dynamicInfo.link == "") {
        dynamicInfo.content = "动态"
        dynamicInfo.link = "https://t.bilibili.com/${dynamicInfo.did}"
    }
    return biList.toList()
}

fun DynamicText.bufferedImages(dynamicInfo: DynamicInfo): List<BufferedImage> {
    val biList = mutableListOf<BufferedImage>()
    ImgUtils.textContent(detail.content, dynamicInfo.display.emojiInfo?.emojiDetails)?.let { biList.add(it) }
    if (dynamicInfo.link == "") {
        dynamicInfo.content = "动态"
        dynamicInfo.link = "https://t.bilibili.com/${dynamicInfo.did}"
    }
    return biList.toList()
}

fun DynamicVideo.bufferedImages(dynamicInfo: DynamicInfo): List<BufferedImage> {
    val biList = mutableListOf<BufferedImage>()
    ImgUtils.textContent(dynamic, dynamicInfo.display.emojiInfo?.emojiDetails)?.let { biList.add(it) }
    if (dynamicInfo.type == DynamicType.REPLY){
        biList.add(ImgUtils.videoContentOld(cover, title, description, "视频"))
    }else{
        biList.add(ImgUtils.videoContent(cover, title, description, "视频"))
    }
    if (dynamicInfo.link == "") {
        dynamicInfo.content = "视频"
        dynamicInfo.link = "https://www.bilibili.com/video/av$aid"
    }
    return biList.toList()
}

fun DynamicArticle.bufferedImages(dynamicInfo: DynamicInfo): List<BufferedImage> {
    val biList = mutableListOf<BufferedImage>()
    biList.add(ImgUtils.articleContent(images, title, summary))
    if (dynamicInfo.link == "") {
        dynamicInfo.content = "专栏"
        dynamicInfo.link = "https://www.bilibili.com/read/cv$id"
    }
    return biList.toList()
}

fun DynamicMusic.bufferedImages(dynamicInfo: DynamicInfo): List<BufferedImage> {
    val biList = mutableListOf<BufferedImage>()
    ImgUtils.textContent(intro, dynamicInfo.display.emojiInfo?.emojiDetails)?.let { biList.add(it) }
    biList.add(ImgUtils.musicContent(cover, title, type))
    if (dynamicInfo.link == "") {
        dynamicInfo.content = "音乐"
        dynamicInfo.link = "https://www.bilibili.com/audio/au$id"
    }
    return biList.toList()
}

fun DynamicSketch.bufferedImages(dynamicInfo: DynamicInfo): List<BufferedImage> {
    val biList = mutableListOf<BufferedImage>()
    ImgUtils.textContent(vest.content, dynamicInfo.display.emojiInfo?.emojiDetails)?.let { biList.add(it) }
    biList.add(ImgUtils.musicContent(detail.cover, detail.title, detail.description, false))
    if (dynamicInfo.link == "") {
        dynamicInfo.content = "动态"
        dynamicInfo.link = "https://t.bilibili.com/${dynamicInfo.did}"
    }
    return biList.toList()
}

fun DynamicEpisode.bufferedImages(dynamicInfo: DynamicInfo): List<BufferedImage> {
    val biList = mutableListOf<BufferedImage>()
    biList.add(ImgUtils.videoContent(cover, season.title, description, "番剧"))
    if (dynamicInfo.link == "") {
        dynamicInfo.content = "动态"
        dynamicInfo.link = "https://t.bilibili.com/${dynamicInfo.did}"
    }
    return biList.toList()
}


suspend fun DynamicInfo.buildTextDynamic(contact: Contact): Message {
    val content = card.content(describe.type, this)
    var resMessage: Message = buildString {
        appendLine("〓 $uname 〓")
        appendLine(content)
        if (BiliPluginConfig.baiduTranslate["enable"] == "true") {
            val tran = trans(content)
            if (tran != null) {
                appendLine("〓 翻译 〓")
                appendLine(tran)
            }
        }
        appendLine("时间: $time")
        appendLine(link)
    }.toPlainText()
    if (this.images.isNotEmpty()) {
        this.images.map {
            resMessage += getImageMessage(it, contact)
        }
    }
    return resMessage
}

suspend fun DynamicInfo.buildImageDynamic(contact: Contact, color: String): Message {
    val biList = card.buildContent(describe.type, this)
    val file = ImgUtils.buildImageMessage(biList, profile, time, color, "dynamic/${uid}/${did}.png")
    val msg = BiliPluginConfig.pushTemplate.replace("{name}",uname!!).replace("{uid}",uid.toString())
        .replace("{type}",content).replace("{time}",time).replace("{link}",link)
    return (file.uploadAsImage(contact) + msg)
}

//suspend fun DynamicInfo.buildScreenshotDynamic(contact: Contact, color: String):Message {
//    val link = "https://t.bilibili.com/${this.did}"
//    val tran = trans(card.content(describe.type, this))?: ""
//    val file = getScreenshot("$link?tab=3","dynamic/${uid}/${did}.png",time,color,true,tran)
//    return  (file.uploadAsImage(contact) + link.toPlainText())
//}

suspend fun getImageMessage(url: String, contact: Contact): Message {
    var inputStream: InputStream? = null
    return try {
        inputStream = URL(url).openConnection().getInputStream()
        inputStream.uploadAsImage(contact)
    } catch (e: Exception) {
        "获取图片失败".toPlainText()
    } finally {
        inputStream?.close()
    }
}

suspend fun DynamicInfo.build(contact: Contact, color: String): Message {
    return when (BiliPluginConfig.pushMode) {
        // 截图模式
        1 -> buildImageDynamic(contact, color)
        // 文字模式
        else -> buildTextDynamic(contact)
    }
}
