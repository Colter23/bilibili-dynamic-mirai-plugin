package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


//enum class SubscriberType(val reg: Regex, val prefix: String){
//    Friend("""(f)(\d{1,10})""".toRegex(), "f"),
//    Group("""(g)(\d{1,10})""".toRegex(), "g"),
//    ContactGroup("""(cg)(\w{1,16})""".toRegex(), "cg"),
//}
//
///**
// * 订阅者
// */
//@Serializable(SubscriberSerializer::class)
//data class Subscriber (
//    val type: SubscriberType,
//    val id: String
//){
//    companion object {
//        fun from(value: String): Subscriber {
//            val type = SubscriberType.values().find {
//                it.reg.matches(value)
//            }?:throw FormatException("订阅者格式错误")
//            return Subscriber(type, type.reg.find(value)!!.destructured.component2())
//        }
//    }
//
//    override fun toString(): String = "${type.prefix}$id"
//}
//
//object SubscriberSerializer: KSerializer<Subscriber> {
//    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(Subscriber::class.qualifiedName!!, PrimitiveKind.STRING)
//    override fun deserialize(decoder: Decoder): Subscriber = Subscriber.from(decoder.decodeString())
//    override fun serialize(encoder: Encoder, value: Subscriber) = encoder.encodeString(value.toString())
//}
