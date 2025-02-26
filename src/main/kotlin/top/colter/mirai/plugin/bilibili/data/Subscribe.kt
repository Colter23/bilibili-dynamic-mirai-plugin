package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Message
import top.colter.mirai.plugin.bilibili.data.ReceiverType.*
import top.colter.mirai.plugin.bilibili.data.SenderType.Season
import top.colter.mirai.plugin.bilibili.data.SenderType.User
import top.colter.mirai.plugin.bilibili.database.BiliConfig
import top.colter.mirai.plugin.bilibili.tools.logger


/**
 * 发送者类型
 *
 * [User] 用户
 *
 * [Season] 番剧
 */
enum class SenderType(val regex: Regex, val prefix: String){
    User(regex = """u(\d{1,20}|\*)""".toRegex(), prefix = "u"),
    Season(regex = """s(\d{1,10}|\*)""".toRegex(), prefix = "s")
}


/**
 * 发送者
 *
 * @param type 类型 [SenderType]
 * @param id ID
 * @param form 来源账号 UID
 */
@Serializable(SenderSerializer::class)
data class Sender (
    val type: SenderType,
    val id: String,
    val form: Long
){
    companion object {
        fun from(value: String): Sender {
            val sp = value.split(".")
            require(sp.size == 1 || sp.size == 2) { "订阅格式错误: $value" }
            val formId = if (sp.size == 1) 0L else sp.first().toLong()
            val sender = sp.last()
            val type = SenderType.values().find {
                it.regex.matches(sender)
            }?:throw IllegalArgumentException("订阅格式错误: $value")
            val senderId = type.regex.find(sender)!!.destructured.component1().let {
                if (it == "*") "0" else it
            }
            return Sender(type = type, id = senderId, form = formId)
        }
    }

    override fun toString(): String = "${type.prefix}$id"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Sender

        if (type != other.type) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }
}

object SenderSerializer: KSerializer<Sender> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(Sender::class.qualifiedName!!, PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): Sender = Sender.from(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: Sender) = encoder.encodeString(value.toString())
}



/**
 * 接收者类型
 *
 * [All] 全体
 *
 * [Friend] 好友
 *
 * [Group] 群
 *
 * [GroupAdmin] 群管理
 *
 * [GroupOwner] 群主
 *
 * [ContactGroup] 分组
 */
enum class ReceiverType(val regex: Regex, val prefix: String): ListContact {
    All(regex = """(\*)""".toRegex(), prefix = "") {
        override fun listContact(bots: List<Bot>, id: String): List<Contact> {
            return listOf()
        }
    },
    Friend(regex = """f(\d{1,12}|\*)""".toRegex(), prefix = "f") {
        override fun listContact(bots: List<Bot>, id: String): List<Contact> {
            var friend: net.mamoe.mirai.contact.Friend? = null
            for (bot in bots) {
                friend = bot.friends[id.toLong()]
                if (friend != null) break
            }
            return if (friend == null) listOf() else listOf(friend)
        }
    },
    Group(regex = """g(\d{1,12}|\*)""".toRegex(), prefix = "g") {
        override fun listContact(bots: List<Bot>, id: String): List<Contact> {
            var group: net.mamoe.mirai.contact.Group? = null
            for (bot in bots) {
                group = bot.groups[id.toLong()]
                if (group != null) break
            }
            return if (group == null) listOf() else listOf(group)
        }
    },
    GroupAdmin(regex = """ga(\d{1,12}|\*)""".toRegex(), prefix = "ga") {
        override fun listContact(bots: List<Bot>, id: String): List<Contact> {
            TODO("Not yet implemented")
        }
    },
    GroupOwner(regex = """go(\d{1,12}|\*)""".toRegex(), prefix = "go") {
        override fun listContact(bots: List<Bot>, id: String): List<Contact> {
            TODO("Not yet implemented")
        }
    },
    ContactGroup(regex = """cg(\S{1,16}|\*)""".toRegex(), prefix = "cg") {
        override fun listContact(bots: List<Bot>, id: String): List<Contact> {
            TODO("Not yet implemented")
        }
    }
}

fun interface ListContact {
    fun listContact(bots: List<Bot>, id: String): List<Contact>
}



/**
 * 接收者
 *
 * @param type 类型 [ReceiverType]
 * @param id ID
 * @param bot 接收者所属bot
 */
@Serializable(ReceiverSerializer::class)
data class Receiver (
    val type: ReceiverType,
    val id: String,
    val bot: Long = 0L
){
    val contacts: List<Contact> by lazy {
        type.listContact(Bot.instances, id)
    }

    suspend fun sendMessage(message: Message) {
        if (BiliConfig.warning) {
            if (type != ContactGroup && contacts.size > 1) {
                logger.error("你正在使用通配群发消息，请确定是否要这么做，消息可能会发送到未知的群或好友。[${type.name}#$id] 将发送给 ${contacts.size} 名好友。" +
                        "如果仍要发送，可前往主配置文件关闭 warning 选项")
                return
            }
        }
        contacts.forEach {
            try {
                it.sendMessage(message)
            } catch (e: Exception) {
                logger.error("[${it.id}] 消息发送失败", e)
            }
        }
    }

    companion object {
        fun from(value: String): Receiver {
            val sp = value.split(".")
            require(sp.size == 1 || sp.size == 2) { "订阅者格式错误: $value" }
            val botId = if (sp.size == 1) 0L else sp.first().toLong()
            val receiver = sp.last()
            val type = ReceiverType.values().find {
                it.regex.matches(receiver)
            }?:throw IllegalArgumentException("订阅者格式错误: $value")
            val receiverId = type.regex.find(receiver)!!.destructured.component1().let {
                if (it == "*") "0" else it
            }
            return Receiver(type = type, id = receiverId, bot = botId)
        }
    }

    override fun toString(): String = "${if (bot == 0L) "" else "$bot."}${type.prefix}$id"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Receiver

        if (type != other.type) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }


}

object ReceiverSerializer: KSerializer<Receiver> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(Receiver::class.qualifiedName!!, PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): Receiver = Receiver.from(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: Receiver) = encoder.encodeString(value.toString())
}


//class ReceiverList<Receiver> (
//    private val container: MutableList<Receiver>
//) : MutableList<Receiver> by container {
//
//
//
//}




fun Receiver.findContact(msg: String) {
    when (this.type) {
        ReceiverType.Friend -> {
            // TODO("多bot查找策略，1：按登录顺序 2：随机 3：指定顺序  4: 交替")
//            val f = Bot.instances.forEach { bot ->
//                bot.friends.find { it.id == id.toLong() }?.let { return it }
//            }
        }
        ReceiverType.Group -> TODO()
        ReceiverType.ContactGroup -> TODO()
        else -> {}
    }
}


fun Receiver.sendMessage(msg: String) {

}

interface FindContactPolicy {

    fun find(bot: Bot, type: ReceiverType): List<Contact> {
        return when (type) {
            Friend -> {
                bot.friends.toList()
            }
            Group -> {
                bot.groups.toList()
            }
            else -> {
                listOf()
            }
        }
    }

}

class OrderFindContactPolicy: FindContactPolicy

/////////////////////  另一种实现 (不大行 //////////////////////
//
//sealed interface BReceiver{
//    fun sendMsg(msg: String)
//}
//
//data class GroupReceiver(
//    val id: Long
//): BReceiver {
//    override fun sendMsg(msg: String) {
//
//    }
//}


