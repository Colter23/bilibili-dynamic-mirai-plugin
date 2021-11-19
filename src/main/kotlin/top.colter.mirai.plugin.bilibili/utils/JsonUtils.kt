package top.colter.mirai.plugin.bilibili.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

class JsonUtils {
}

val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    isLenient = true
    allowStructuredMapKeys = true
}

inline fun <reified T> JsonElement.decode(): T = json.decodeFromJsonElement(json.serializersModule.serializer(),this)

//inline fun <reified T> decode(url : String) : T {
//    val js = httpGet(url).decode<ResultData>()
//    if (js.code != 0){
//        println(url)
//        println(js)
//        throw Exception()
//    }
//    return js.data!!.decode<T>()
//}