package top.colter.mirai.plugin.bilibili

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

fun String.dynamicContent(type: Int): DynamicContent {
    return when (type) {
        DynamicType.REPLY -> decode<DynamicReply>()
        DynamicType.PICTURE -> decode<DynamicPicture>()
        DynamicType.TEXT -> decode<DynamicText>()
        DynamicType.VIDEO -> decode<DynamicVideo>()
        DynamicType.ARTICLE -> decode<DynamicArticle>()
        DynamicType.MUSIC -> decode<DynamicMusic>()
        DynamicType.EPISODE -> decode<DynamicEpisode>()
        DynamicType.SKETCH -> decode<DynamicSketch>()
        DynamicType.LIVE, DynamicType.LIVE_ING -> decode<DynamicLive>()
        else -> DynamicNull()
    }
}

fun DynamicContent.textContent(type: Int): String {
    val that = this
    return when (type) {
        DynamicType.REPLY -> buildString{
            val reply = that as DynamicReply
            val oType = reply.detail.originType
            append(reply.detail.content)
            append(reply.origin.dynamicContent(oType).textContent(oType))
        }
        DynamicType.TEXT -> (this as DynamicText).detail.content
        DynamicType.SKETCH -> (this as DynamicSketch).vest.content
        else -> ""
    }
}

fun DynamicContent.content(type: Int, dynamicInfo: DynamicInfo): String {
    return when (type) {
        DynamicType.REPLY -> (this as DynamicReply).getContent(dynamicInfo)
        DynamicType.PICTURE -> (this as DynamicPicture).getContent(dynamicInfo)
        DynamicType.TEXT -> (this as DynamicText).getContent(dynamicInfo)
        DynamicType.VIDEO -> (this as DynamicVideo).getContent(dynamicInfo)
        DynamicType.ARTICLE -> (this as DynamicArticle).getContent(dynamicInfo)
        DynamicType.MUSIC -> (this as DynamicMusic).getContent(dynamicInfo)
        DynamicType.EPISODE -> (this as DynamicEpisode).getContent(dynamicInfo)
        DynamicType.DELETE -> "源动态已被作者删除"
        DynamicType.SKETCH -> (this as DynamicSketch).getContent(dynamicInfo)
        DynamicType.LIVE, DynamicType.LIVE_ING -> (this as DynamicLive).getContent(dynamicInfo)
        DynamicType.LIVE_END -> "直播结束了"
        else -> {
            if (dynamicInfo.link == "") {
                dynamicInfo.link = "https://t.bilibili.com/${dynamicInfo.did}"
            }
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
        append(origin.dynamicContent(detail.originType).content(detail.originType, dynamicInfo))
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


suspend fun DynamicContent.buildContent(type: Int, dynamicInfo: DynamicInfo): MutableList<BufferedImage> {
    return when (type) {
        DynamicType.REPLY -> (this as DynamicReply).bufferedImages(dynamicInfo)
        DynamicType.PICTURE -> (this as DynamicPicture).bufferedImages(dynamicInfo)
        DynamicType.TEXT -> (this as DynamicText).bufferedImages(dynamicInfo)
        DynamicType.VIDEO -> (this as DynamicVideo).bufferedImages(dynamicInfo)
        DynamicType.ARTICLE -> (this as DynamicArticle).bufferedImages(dynamicInfo)
        DynamicType.MUSIC -> (this as DynamicMusic).bufferedImages(dynamicInfo)
        DynamicType.EPISODE -> (this as DynamicEpisode).bufferedImages(dynamicInfo)
        DynamicType.DELETE -> mutableListOf(ImgUtils.infoContent("源动态已被作者删除"))
        DynamicType.SKETCH -> (this as DynamicSketch).bufferedImages(dynamicInfo)
        DynamicType.LIVE, DynamicType.LIVE_ING -> mutableListOf(ImgUtils.infoContent("直播"))
        DynamicType.LIVE_END -> mutableListOf(ImgUtils.infoContent("直播结束了"))
        else -> {
            if (dynamicInfo.link == "") {
                dynamicInfo.link = "https://t.bilibili.com/${dynamicInfo.did}"
            }
            mutableListOf(ImgUtils.infoContent("不支持此类型动态 type:${type}"))
        }
    }
}


suspend fun DynamicReply.bufferedImages(dynamicInfo: DynamicInfo): MutableList<BufferedImage> {
    val biList = mutableListOf<BufferedImage>()
    val emojiList: MutableList<EmojiDetails> = mutableListOf()
    dynamicInfo.display.emojiInfo?.emojiDetails?.let { emojiList.addAll(it) }
    dynamicInfo.display.origin?.emojiInfo?.emojiDetails?.let { emojiList.addAll(it) }

    ImgUtils.textContent(detail.content, emojiList)?.let { biList.add(it) }
    if (dynamicInfo.link == "") {
        dynamicInfo.content = "转发动态"
        dynamicInfo.link = "https://t.bilibili.com/${dynamicInfo.did}"
        dynamicInfo.id = dynamicInfo.did.toString()
    }
    val biL = origin.dynamicContent(detail.originType).buildContent(detail.originType, dynamicInfo)
    biList.add(ImgUtils.buildReplyImageMessage(biL, originUser?.user))

    return biList
}

suspend fun DynamicPicture.bufferedImages(dynamicInfo: DynamicInfo): MutableList<BufferedImage> {
    val biList = mutableListOf<BufferedImage>()
    ImgUtils.textContent(detail.description, dynamicInfo.display.emojiInfo?.emojiDetails)?.let { biList.add(it) }
    biList.add(ImgUtils.imageContent(detail.pictures))
    if (dynamicInfo.link == "") {
        dynamicInfo.content = "动态"
        dynamicInfo.link = "https://t.bilibili.com/${dynamicInfo.did}"
        dynamicInfo.id = dynamicInfo.did.toString()
    }
    return biList
}

suspend fun DynamicText.bufferedImages(dynamicInfo: DynamicInfo): MutableList<BufferedImage> {
    val biList = mutableListOf<BufferedImage>()
    ImgUtils.textContent(detail.content, dynamicInfo.display.emojiInfo?.emojiDetails)?.let { biList.add(it) }
    if (dynamicInfo.link == "") {
        dynamicInfo.content = "动态"
        dynamicInfo.link = "https://t.bilibili.com/${dynamicInfo.did}"
        dynamicInfo.id = dynamicInfo.did.toString()
    }
    return biList
}

suspend fun DynamicVideo.bufferedImages(dynamicInfo: DynamicInfo): MutableList<BufferedImage> {
    val biList = mutableListOf<BufferedImage>()
    ImgUtils.textContent(dynamic, dynamicInfo.display.emojiInfo?.emojiDetails)?.let { biList.add(it) }
    if (dynamicInfo.type == DynamicType.REPLY) {
        biList.add(ImgUtils.videoContentOld(cover, title, description, "视频"))
    } else {
        biList.add(ImgUtils.videoContent(cover, title, description, "视频"))
    }
    if (dynamicInfo.link == "") {
        dynamicInfo.content = "视频"
        dynamicInfo.link = "https://www.bilibili.com/video/av$aid"
        dynamicInfo.id = "av$aid"
    }
    return biList
}

suspend fun DynamicArticle.bufferedImages(dynamicInfo: DynamicInfo): MutableList<BufferedImage> {
    val biList = mutableListOf<BufferedImage>()
    biList.add(ImgUtils.articleContent(images, title, summary))
    if (dynamicInfo.link == "") {
        dynamicInfo.content = "专栏"
        dynamicInfo.link = "https://www.bilibili.com/read/cv$id"
        dynamicInfo.id = "cv$id"
    }
    return biList
}

suspend fun DynamicMusic.bufferedImages(dynamicInfo: DynamicInfo): MutableList<BufferedImage> {
    val biList = mutableListOf<BufferedImage>()
    ImgUtils.textContent(intro, dynamicInfo.display.emojiInfo?.emojiDetails)?.let { biList.add(it) }
    biList.add(ImgUtils.musicContent(cover, title, type))
    if (dynamicInfo.link == "") {
        dynamicInfo.content = "音乐"
        dynamicInfo.link = "https://www.bilibili.com/audio/au$id"
        dynamicInfo.id = "au$id"
    }
    return biList
}

suspend fun DynamicSketch.bufferedImages(dynamicInfo: DynamicInfo): MutableList<BufferedImage> {
    val biList = mutableListOf<BufferedImage>()
    ImgUtils.textContent(vest.content, dynamicInfo.display.emojiInfo?.emojiDetails)?.let { biList.add(it) }
    biList.add(ImgUtils.musicContent(detail.cover, detail.title, detail.description, false))
    if (dynamicInfo.link == "") {
        dynamicInfo.content = "动态"
        dynamicInfo.link = "https://t.bilibili.com/${dynamicInfo.did}"
        dynamicInfo.id = dynamicInfo.did.toString()
    }
    return biList
}

suspend fun DynamicEpisode.bufferedImages(dynamicInfo: DynamicInfo): MutableList<BufferedImage> {
    val biList = mutableListOf<BufferedImage>()
    biList.add(ImgUtils.videoContent(cover, season.title, description, "番剧"))
    if (dynamicInfo.link == "") {
        dynamicInfo.content = "动态"
        dynamicInfo.link = "https://t.bilibili.com/${dynamicInfo.did}"
        dynamicInfo.id = dynamicInfo.did.toString()
    }
    return biList
}


suspend fun DynamicInfo.buildTextDynamic(contact: Contact): Message {
    val content = (dynamicContent ?: card.dynamicContent(type)).content(type, this)
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
    val biList = (dynamicContent ?: card.dynamicContent(type)).buildContent(type, this)
    val footer = replaceTemplate(BiliPluginConfig.footerTemplate)
    biList.add(ImgUtils.footer(footer))
    val file = withContext(Dispatchers.IO) {
        ImgUtils.buildImageMessage(biList, profile, time, color, link,"dynamic/${uid}/${did}.png")
    }
    //val file = ImgUtils.buildImageMessage(biList, profile, time, color, "dynamic/${uid}/${did}.png")
    val msg = replaceTemplate(BiliPluginConfig.pushTemplate)
    return file.uploadAsImage(contact) + msg
}

fun DynamicInfo.replaceTemplate(template: String): String {
    return template.replace("{name}", uname!!).replace("{uid}", uid.toString())
        .replace("{type}", content).replace("{time}", time).replace("{link}", link)
        .replace("{id}", id)
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
