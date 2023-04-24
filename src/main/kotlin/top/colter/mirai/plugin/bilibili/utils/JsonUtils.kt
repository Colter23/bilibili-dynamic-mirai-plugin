package top.colter.mirai.plugin.bilibili.utils

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import kotlin.io.path.appendText
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
        val time = (System.currentTimeMillis() / 1000).formatTime("yyyy-MM-dd")

        val md5 = e.message?.md5()
        val fileName = "$time-$md5.json"

        BiliBiliDynamic.dataFolderPath.resolve("exception").apply {
            if (notExists()) createDirectories()
        }.resolve(fileName).apply {
            if (notExists()) {
                writeText(e.stackTraceToString())
                appendText("\n\n\n")
                appendText(json.encodeToString(JsonElement.serializer(), this@decode))
            }
        }

        BiliBiliDynamic.logger.error("json解析失败，请把 /data/exception/ 目录下的 $fileName 文件反馈给开发者\n${e.message}")
        throw e
    }
}
