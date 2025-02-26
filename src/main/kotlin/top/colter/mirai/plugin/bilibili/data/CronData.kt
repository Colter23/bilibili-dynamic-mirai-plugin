package top.colter.mirai.plugin.bilibili.data

import com.cronutils.model.Cron
import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.parser.CronParser
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Serializable
data class CronData(
    @Serializable(CronSerializer::class)
    val cron: Cron
)

val CronParser: CronParser by lazy {
    CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))
}

object CronSerializer: KSerializer<Cron> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(Cron::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Cron {
        return CronParser.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Cron) {
        encoder.encodeString(value.asString())
    }
}


