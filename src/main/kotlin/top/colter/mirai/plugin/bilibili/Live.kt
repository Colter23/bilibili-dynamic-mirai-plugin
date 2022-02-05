package top.colter.mirai.plugin.bilibili

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import top.colter.mirai.plugin.bilibili.data.BiliPluginConfig
import top.colter.mirai.plugin.bilibili.data.LiveInfo
import top.colter.mirai.plugin.bilibili.utils.ImgUtils.buildLiveImageMessage
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


val LiveInfo.time: String
    get() = DateTimeFormatter.ofPattern("yyyy年MM月dd日  HH:mm:ss")
        .format(LocalDateTime.ofEpochSecond(liveTime, 0, OffsetDateTime.now().offset))

suspend fun LiveInfo.build(contact: Contact): Message {
    return when (BiliPluginConfig.pushMode) {
        // 图片模式
        1 -> buildImageLive(contact)
        // 文字模式
        else -> this.buildTextLive(contact)
    }
}

suspend fun LiveInfo.buildTextLive(contact: Contact): Message {
    var resMessage: Message = buildString {
        appendLine("〓 $uname 〓 直播 〓")
        appendLine("直播: $title")
        appendLine()
        appendLine("时间: $time")
        appendLine("https://live.bilibili.com/$roomId")
    }.toPlainText()
    resMessage += getImageMessage(cover, contact)
    return resMessage
}

suspend fun LiveInfo.buildImageLive(contact: Contact): Message {
    val link = "https://live.bilibili.com/${roomId}"
    val file = buildLiveImageMessage(title, cover, time, uname, face, "#d3edfa", "live/${uid}/${liveTime}.png")
    val template = BiliPluginConfig.livePushTemplate.ifEmpty {
        BiliPluginConfig.pushTemplate
    }
    val msg = template.replace("{name}", uname).replace("{uid}", uid.toString())
        .replace("{type}", "直播").replace("{time}", time).replace("{link}", link)
    return (file.uploadAsImage(contact) + msg)
}
