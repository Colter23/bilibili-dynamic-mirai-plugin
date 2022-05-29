package top.colter.mirai.plugin.bilibili.utils

import io.ktor.client.request.*
import kotlinx.coroutines.channels.Channel
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.jetbrains.skia.Image
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.DynamicItem
import top.colter.mirai.plugin.bilibili.data.DynamicType.*
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.*

fun List<String>.isBlank(): Boolean {
    if (size == 0) return true
    forEach { if (it != "") return false }
    return true
}

fun List<String>.isNotBlank(): Boolean = !isBlank()

suspend fun <E> Channel<E>.sendAll(list: Collection<E>) = list.forEach { send(it) }

fun <T> Collection<T>.plusOrNull(element: T?): List<T> {
    return if (element != null) {
        val result = ArrayList<T>(size + 1)
        result.addAll(this)
        result.add(element)
        result
    } else {
        this as List
    }
}


val DynamicItem.time: Long
    get() = (idStr.toLong() shr 32) + 1498838400L

val DynamicItem.formatTime: String
    get() = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")
        .format(LocalDateTime.ofEpochSecond(time, 0, OffsetDateTime.now().offset))

val DynamicItem.link: String
    get() = when (type) {
        DYNAMIC_TYPE_WORD,
        DYNAMIC_TYPE_DRAW,
        DYNAMIC_TYPE_FORWARD,
        DYNAMIC_TYPE_COMMON_SQUARE -> "https://t.bilibili.com/$idStr"
        DYNAMIC_TYPE_ARTICLE -> "https://www.bilibili.com/read/cv${modules.moduleDynamic.major?.article?.id}"
        DYNAMIC_TYPE_AV -> "https://www.bilibili.com/video/${modules.moduleDynamic.major?.archive?.bvid}"
        DYNAMIC_TYPE_MUSIC -> "https://www.bilibili.com/audio/au${modules.moduleDynamic.major?.music?.id}"
        DYNAMIC_TYPE_LIVE -> "https://live.bilibili.com/${modules.moduleDynamic.major?.live?.id}"
        DYNAMIC_TYPE_LIVE_RCMD -> "https://live.bilibili.com/${modules.moduleDynamic.major?.live?.id}"
        DYNAMIC_TYPE_PGC -> "https://www.bilibili.com/bangumi/play/ep${modules.moduleDynamic.major?.pgc?.epid}"
        DYNAMIC_TYPE_NONE -> ""
    }


val cachePath: Path by lazy {
    ///////////////////////临时临时临时临时临时临时临时临时临时临时临时临时临时临时临时//////////////////////////////////////////
    Path("data/top.colter.bilibili-dynamic-mirai-plugin").resolve("cache")
    //dataFolderPath.resolve("cache")
}

fun CacheType.cachePath(): Path {
    return cachePath.resolve(path).apply {
        if (notExists()) createDirectories()
    }
}

fun CacheType.cachePath(file: String): Path {
    return cachePath().resolve(file)
}

enum class CacheType(val path: String) {
    DRAW_DYNAMIC("draw/dynamic"),
    DRAW_LIVE("draw/live"),
    IMAGES("images"),
    EMOJI("emoji"),
    USER("user"),
    OTHER("other"),
    UNKNOWN(""),
}

fun Path.findFile(file: String): Path? {
    forEachDirectoryEntry {
        if (it.isDirectory()) {
            val path = it.findFile(file)
            if (path != null) return path
        } else {
            if (it.name == file) return it
        }
    }
    return null
}

val imageClient = BiliClient()
suspend fun getOrDownload(url: String, cacheType: CacheType = CacheType.UNKNOWN): ByteArray {
    val fileName = url.split("?").first().split("@").first().split("/").last()

    val filePath = if (cacheType == CacheType.UNKNOWN) {
        cachePath.findFile(fileName) ?: CacheType.OTHER.cachePath(fileName)
    } else {
        cacheType.cachePath(fileName)
    }
    return if (filePath.exists()) {
        filePath.readBytes()
    } else {
        imageClient.useHttpClient {
            it.get<ByteArray>(url).apply {
                filePath.writeBytes(this)
            }
        }
    }
}

suspend fun getOrDownloadImage(url: String, cacheType: CacheType = CacheType.UNKNOWN): Image =
    Image.makeFromEncoded(getOrDownload(url, cacheType))

suspend fun uploadImage(url: String, cacheType: CacheType = CacheType.UNKNOWN, contact: Contact) =
    contact.uploadImage(getOrDownload(url, cacheType).toExternalResource().toAutoCloseable())


