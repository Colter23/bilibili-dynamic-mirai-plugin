package top.colter.mirai.plugin.bilibili.database

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalTime


@Serializable
class TimeRange(
    @Serializable(LocalTimeSerializer::class)
    @SerialName("start")
    override val start: LocalTime,
    @Serializable(LocalTimeSerializer::class)
    @SerialName("end")
    override val endInclusive: LocalTime
): ClosedRange<LocalTime> {
    override fun contains(value: LocalTime): Boolean {
        return when {
            start < endInclusive -> value in start..endInclusive
            start > endInclusive -> value >= start || value <= endInclusive
            else -> false
        }
    }
    override fun isEmpty(): Boolean = (start == endInclusive)
    override fun toString(): String = "$start~$endInclusive"
}

object LocalTimeSerializer: KSerializer<LocalTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(LocalTime::class.qualifiedName!!, PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): LocalTime {
        return LocalTime.parse(decoder.decodeString())
    }
    override fun serialize(encoder: Encoder, value: LocalTime) {
        encoder.encodeString(value.toString())
    }
}
