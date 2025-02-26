package top.colter.mirai.plugin.bilibili.data

import net.mamoe.mirai.message.data.*
import top.colter.bilibili.data.dynamic.type.BiliDynamicType
import top.colter.mirai.plugin.bilibili.tools.uploadImage
import java.lang.System.currentTimeMillis
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure


interface BiliMessage {
    val type: BiliDynamicType
    val links: List<String>?
    val images: List<ByteArray>?
    val draw: ByteArray?
}

class BiliDynamicMessage(
    val did: Long,
    val uid: Long,
    val name: String,
    val time: String,
    val content: String,
    override val type: BiliDynamicType,
    override val links: List<String>? = null,
    override val images: List<ByteArray>? = null,
    override val draw: ByteArray? = null,
//    val origin: BiliMessage? = null
): BiliMessage


suspend fun BiliMessage.buildMessage(template: String, receiver: Receiver): List<Message> {
    val tagRegex = """\{([a-z]+|>>|<<)}""".toRegex()

    return buildMessageChainList {
        val last = tagRegex.findAll(template).fold(0) { pos, match ->
            val name = match.destructured.component1()
            chain.append(template.subSequence(pos, match.range.first))
            when (name) {
                ">>" -> forward()
                "<<" -> buildForward(1L, "AAAAA")
                "n" -> chain.append("\n")
                "r" -> flash()
                else -> if (!makeMessage(name, receiver)) {
                    chain.append(template.subSequence(match.range))
                }
            }
            match.range.last + 1
        }
        chain.append(template.subSequence(last, template.length))
    }
}

fun BiliMessage.makePrimitiveMessage(name: String): String? {
    val props = this::class.declaredMemberProperties
    val prop = props.find { it.name == name }
    return if (prop != null && prop::class.java.isPrimitive) {
        prop.call(this)?.toString()
    } else null
}

context(MessageChainListBuilder)
suspend fun BiliMessage.makeMessage(name: String, receiver: Receiver): Boolean {
    val prop = this::class.declaredMemberProperties.find { it.name == name }
    return if (prop != null) {
        val type = prop.returnType.jvmErasure
        if (type.java.isPrimitive || type.java.isEnum || type == String::class) {
            prop.call(this)?.toString()?.let { chain.append(it) }
        }else {
            if (type == ByteArray::class) {
                val value = prop.call(this) as ByteArray?
                if (value != null) {
                    receiver.uploadImage(value)?.let { chain.add(it) }
                }
            } else if (type == List::class) {
                val list = prop.call(this) as List<*>?
                if (list?.isNotEmpty() == true) {
                    if (list.first()!!::class == ByteArray::class) {
                        list.forEach {
                            receiver.uploadImage(it as ByteArray)?.let { chain.add(it) }
                        }
                    }else if (list.first()!!::class == String::class) {
                        chain.append(list.joinToString("\n"))
                    }
                }
            } // else if (type == BiliMessage::class) { }
        }
        true
    }else {
        false
    }
}

class MessageChainListBuilder(
    private val list: MutableList<Message> = mutableListOf(),
    private var forwardList: MutableList<MessageChain>? = null
) {
    var chain = MessageChainBuilder()

    fun forward() {
        flash()
        if (forwardList == null) forwardList = mutableListOf()
    }

    fun buildForward(
        senderId: Long,
        senderName: String,
        time: Int = (currentTimeMillis() / 1000).toInt(),
        displayStrategy: ForwardMessage.DisplayStrategy = ForwardMessage.DisplayStrategy
    ) {
        if (forwardList != null) {
            flash()
            if (forwardList!!.isNotEmpty()) {
                list.add(forwardList!!.toForwardMessage(senderId, senderName, time, displayStrategy))
            }
            forwardList = null
        }
    }

    fun flash() {
        chain.asMessageChain()
        if (chain.isNotEmpty()) {
            if (forwardList != null) {
                forwardList!!.add(chain.toMessageChain())
            }else {
                list.add(chain.toMessageChain())
            }
            chain = MessageChainBuilder()
        }
    }

    fun build(): List<Message> {
        flash()
        return list
    }
}

inline fun buildMessageChainList(block: MessageChainListBuilder.() -> Unit): List<Message> {
    return MessageChainListBuilder().apply(block).build()
}

fun Iterable<Message>.toForwardMessage(
    senderId: Long,
    senderName: String,
    time: Int = (currentTimeMillis() / 1000).toInt(),
    displayStrategy: ForwardMessage.DisplayStrategy = ForwardMessage.DisplayStrategy
): ForwardMessage =
    RawForwardMessage(this.map { ForwardMessage.Node(senderId, time, senderName, it) }).render(displayStrategy)
