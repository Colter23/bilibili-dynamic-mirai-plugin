package top.colter.mirai.plugin.bilibili.utils

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.utils.translate.MD5
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists
import kotlin.io.path.writeText

val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    isLenient = true
    allowStructuredMapKeys = true
}

inline fun <reified T> String.decode(): T = json.parseToJsonElement(this).decode()

inline fun <reified T> JsonElement.decode(): T {
    return try {
        json.decodeFromJsonElement(this)
    }catch (e: SerializationException) {
        val time = System.currentTimeMillis().formatTime("yyyy-MM-dd")
        val md5 = MD5.md5(e.message)
        val fileName = "$time-$md5.json"

        BiliBiliDynamic.dataFolderPath.resolve("exception").apply {
            if (notExists()) createDirectories()
        }.resolve(fileName).apply {
            if (notExists()) writeText(json.encodeToString(JsonElement.serializer(), this@decode))
        }

        BiliBiliDynamic.logger.error("json解析失败，请把 /data/exception/ 目录下的 $fileName 文件反馈给开发者")
        throw e
    }
}
