package top.colter.mirai.plugin.bilibili.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.serializer

val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    isLenient = true
    allowStructuredMapKeys = true
}

inline fun <reified T> String.decode(): T = json.decodeFromString(json.serializersModule.serializer(),this)
inline fun <reified T> JsonElement.decode(): T = json.decodeFromJsonElement(this)
