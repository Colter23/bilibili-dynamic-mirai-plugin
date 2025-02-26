package top.colter.mirai.plugin.bilibili.tools

import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.json.Json
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.MiraiLogger
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.data.Receiver
import java.io.IOException


val logger by lazy {
    try {
        BiliBiliDynamic.logger
    } catch (_: Throwable) {
        MiraiLogger.Factory.create(BiliBiliDynamic::class)
    }
}

class MyLogger(override val identity: String?, override val isEnabled: Boolean) : MiraiLogger {
    override fun debug(message: String?) {
        TODO("Not yet implemented")
    }

    override fun debug(message: String?, e: Throwable?) {
        TODO("Not yet implemented")
    }

    override fun error(message: String?) {
        TODO("Not yet implemented")
    }

    override fun error(message: String?, e: Throwable?) {
        TODO("Not yet implemented")
    }

    override fun info(message: String?) {
        TODO("Not yet implemented")
    }

    override fun info(message: String?, e: Throwable?) {
        TODO("Not yet implemented")
    }

    override fun verbose(message: String?) {
        TODO("Not yet implemented")
    }

    override fun verbose(message: String?, e: Throwable?) {
        TODO("Not yet implemented")
    }

    override fun warning(message: String?) {
        TODO("Not yet implemented")
    }

    override fun warning(message: String?, e: Throwable?) {
        TODO("Not yet implemented")
    }

}


suspend fun <E> Channel<E>.sendAll(list: Collection<E>) = list.forEach { send(it) }

fun Boolean.ifTrue(block: () -> Unit) = if (this) { block(); true } else false
fun Boolean.ifFalse(block: () -> Unit) = if (!this) { block(); false } else true

val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    isLenient = true
    allowStructuredMapKeys = true
}

//inline fun <reified T> String.decode(): T  = try {
//    json.decodeFromString(this)
//}catch (e: SerializationException) {
//    logger.error("Json数据解析失败 请把报错信息汇报给开发者")
//    throw e
//}
//
//
//inline fun <reified T> JsonElement.decode(): T = try {
//    json.decodeFromJsonElement(this)
//}catch (e: SerializationException) {
//    logger.error("Json数据解析失败 请把报错信息汇报给开发者")
//    throw e
//}

//internal inline fun <reified T : Any, reified R> reflectField() = ReadOnlyProperty<T, R> { thisRef, property ->
//    thisRef::class.java.getDeclaredField(property.name).apply { isAccessible = true }.get(thisRef) as R
//}
//
//internal inline fun <reified R> Any.reflectMethod(method: String, vararg args: Any?): R =
//    this::class.java.getDeclaredMethod(method).invoke(this, args) as R


fun loadResourceBytes(path: String): ByteArray? {
    return try {
        BiliBiliDynamic.getResourceAsStream(path)?.readBytes()
    } catch (e: IOException) {
        logger.error("加载资源失败 $path", e)
        null
    }
}


suspend fun Contact.uploadImage(url: String, cacheType: CacheType = CacheType.UNKNOWN) = try {
    getOrDownload(url, cacheType)?.toExternalResource()?.let { uploadImage(it.toAutoCloseable()) }
}catch (e: Exception){
    logger.error("上传图片失败! $url\n$e")
    null
}

suspend fun Contact.uploadImage(byteArray: ByteArray) = try {
    uploadImage(byteArray.toExternalResource().toAutoCloseable())
}catch (e: Exception){
    logger.error("上传图片失败! \n$e")
    null
}

suspend fun Contact.uploadImage(byteArrays: List<ByteArray>): List<net.mamoe.mirai.message.data.Image> {
    return byteArrays.mapNotNull {
        uploadImage(it)
    }
}

suspend fun Receiver.uploadImage(url: String, cacheType: CacheType = CacheType.UNKNOWN) = try {
    getOrDownload(url, cacheType)?.toExternalResource()?.let { contacts.first().uploadImage(it.toAutoCloseable()) }
}catch (e: Exception){
    logger.error("上传图片失败! $url\n$e")
    null
}

suspend fun Receiver.uploadImage(byteArray: ByteArray) = try {
    contacts.first().uploadImage(byteArray.toExternalResource().toAutoCloseable())
}catch (e: Exception){
    logger.error("上传图片失败! \n$e")
    null
}

suspend fun Receiver.uploadImage(byteArrays: List<ByteArray>): List<net.mamoe.mirai.message.data.Image> {
    return byteArrays.mapNotNull {
        uploadImage(it)
    }
}