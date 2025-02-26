package top.colter.mirai.plugin.bilibili.lisener

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import org.jetbrains.skia.Image
import top.colter.bilibili.data.LazyImage
import top.colter.bilibili.data.dynamic.BiliDynamic
import top.colter.bilibili.data.dynamic.content
import top.colter.bilibili.data.dynamic.major
import top.colter.bilibili.data.dynamic.type.MajorType
import top.colter.bilibili.data.dynamic.type.OriginDynamicType.*
import top.colter.mirai.plugin.bilibili.data.BiliDynamicMessage
import top.colter.mirai.plugin.bilibili.data.BiliMessage
import top.colter.mirai.plugin.bilibili.data.Receiver
import top.colter.mirai.plugin.bilibili.data.buildMessage
import top.colter.mirai.plugin.bilibili.database.PushTemplate
import top.colter.mirai.plugin.bilibili.database.PushTemplateType
import top.colter.mirai.plugin.bilibili.database.ReceiverConfig
import top.colter.mirai.plugin.bilibili.draw.DynamicDraw
import top.colter.mirai.plugin.bilibili.event.BiliDynamicEvent
import top.colter.mirai.plugin.bilibili.tools.getOrDownload
import top.colter.mirai.plugin.bilibili.tools.logger
import kotlin.coroutines.CoroutineContext


object DynamicListener: SimpleListenerHost() {
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        logger.error("MessageEventListener Exception: $exception")
    }

    @EventHandler
    suspend fun BiliDynamicEvent.onMessage() {
        val dynamicMessage = message.dynamic.toBiliMessage(DynamicDraw(message.dynamic))
        val templateMap = message.receiver.dynamicTemplate()

        for ((template, receivers) in templateMap) {
            val message = dynamicMessage.buildMessage(template, receivers.first())
            receivers.sendMessage(message)
        }

    }

}

suspend fun List<Receiver>.sendMessage(messages: List<Message>) {
    val sendContact: MutableMap<Contact, MutableList<MessageReceipt<Contact>>> = mutableMapOf()
    forEach {
        it.contacts.forEach {
            if (!sendContact.containsKey(it)) {
                sendContact.getOrPut(it) { mutableListOf() }.addAll(it.sendMessage(messages))
            }
        }
    }
}

suspend fun Receiver.sendMessage(messages: List<Message>) {
    messages.forEach {
        sendMessage(it)
    }
}

suspend fun Contact.sendMessage(messages: List<Message>): List<MessageReceipt<Contact>> {
    return messages.map {
        sendMessage(it)
    }
}


fun Set<Receiver>.dynamicTemplate(): Map<String, List<Receiver>> {
    val default = PushTemplate.dynamicDefault
    val dynamicTemplate = PushTemplate.dynamic
    val receivers = ReceiverConfig.receivers
    val templateMap = mutableMapOf(default to mutableListOf<Receiver>())
    var tempReceiver: Receiver? = null

    for (receiver in this) {
        if (tempReceiver != null) templateMap[default]!!.add(tempReceiver)
        tempReceiver = receiver
        val config = receivers[receiver] ?: continue
        val templateName = config.template[PushTemplateType.DYNAMIC] ?: continue
        templateMap.getOrPut(templateName) { mutableListOf() }.add(receiver)
        tempReceiver = null
    }

    return templateMap.mapNotNull { (name, receiver) ->
        var template = dynamicTemplate[name]
        if (template == null) {
            logger.error("未找到名为 [$name] 的模板!")
            template = dynamicTemplate[default]
        }
        if (template == null) {
            logger.error("未找到默认模板 [$default] !")
            template = dynamicTemplate.values.first()
        }
        if (receiver.isEmpty()) null else template to receiver.toList()
    }.toMap()
}


suspend fun List<LazyImage>.loadImage(): List<ByteArray> {
    forEach {
        if (it.image == null) {
            it.image = getOrDownload(it.url)
        }
    }
    return this.mapNotNull { it.image }
}

suspend fun BiliDynamic.toBiliMessage(draw: Image? = null): BiliMessage {
    val images = images()?.loadImage()

    return BiliDynamicMessage(
        did = id,
        uid = mid,
        name = name,
        type = type,
        time = formatTime,
        content = textContent(),
        images = images,
        links = links(),
        draw = draw?.encodeToData()?.bytes,
//        origin = origin?.toBiliMessage()
    )
}

//val template: Map<String, String>

fun Set<Receiver>.receiverMessageTemplate(): Map<String, Set<Receiver>> {
    return mapOf()
}

fun BiliDynamic.textContent(): String {
    return when (originType) {
        FORWARD -> "${content?.text}" // \n\n转发 ${origin?.name} 的动态: \n${origin?.textContent()}  转移
        WORD,
        DRAW -> content?.text ?: major?.blocked?.hintMessage ?: ""
        ARTICLE -> major?.article?.title ?: ""
        AV -> major?.video?.title ?: ""
        MUSIC -> major?.music?.title ?: ""
        PGC -> major?.pgc?.title ?: ""
        UGC_SEASON -> major?.ugcSeason?.title ?: ""
        COMMON_VERTICAL,
        COMMON_SQUARE -> major?.common?.title ?: ""
        LIVE -> major?.live?.title ?: ""
        LIVE_RCMD -> major?.liveRcmd?.liveInfo?.livePlayInfo?.title ?: ""
        MEDIALIST -> major?.mediaList?.title ?: ""
        NONE -> major?.none?.tips ?: ""
        UNKNOWN -> "未知的动态类型: ${originType.info}"
    }
}

fun BiliDynamic.images(): List<LazyImage>? {
    return when (originType) {
        FORWARD -> origin?.images()!!
        DRAW -> if (major?.type == MajorType.DRAW) major?.draw?.images?.map { it.src } else null
        ARTICLE -> major?.article?.covers
        AV -> major?.video?.cover?.list()
        MUSIC -> major?.music?.cover?.list()
        PGC -> major?.pgc?.cover?.list()
        UGC_SEASON -> major?.ugcSeason?.cover?.list()
        COMMON_SQUARE -> major?.common?.cover?.list()
        LIVE -> major?.live?.cover?.list()
        LIVE_RCMD -> major?.liveRcmd?.liveInfo?.livePlayInfo?.cover?.list()
        else -> null
    }
}

fun BiliDynamic.links(): List<String>? {
    return when (originType) {
//        NONE,
//        WORD,
//        DRAW,
//        COMMON_VERTICAL,
//        COMMON_SQUARE,
//        UGC_SEASON -> listOf(DYNAMIC_LINK(id))
//        FORWARD -> listOf(DYNAMIC_LINK(id), DYNAMIC_LINK(origin!!.id))
//        ARTICLE -> listOf(ARTICLE_LINK(major?.article?.id!!.toString()), DYNAMIC_LINK(id))
//        AV -> listOf(VIDEO_LINK(major?.video?.aid.toString()), DYNAMIC_LINK(id))
//        MUSIC -> listOf(MUSIC_LINK(major?.music?.id!!.toString()), DYNAMIC_LINK(id))
//        PGC -> listOf(EPISODE_LINK(major?.pgc?.epid!!.toString()), DYNAMIC_LINK(id))
//        LIVE -> listOf(LIVE_LINK(major?.live?.rid!!.toString()), DYNAMIC_LINK(id))
//        LIVE_RCMD -> listOf(
//            LIVE_LINK(major?.liveRcmd?.liveInfo?.livePlayInfo?.roomId!!.toString()),
//            DYNAMIC_LINK(id)
//        )
        else -> null
    }
}

inline fun <reified T> T.list(): List<T> = listOf(this)

