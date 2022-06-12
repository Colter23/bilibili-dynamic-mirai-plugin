package top.colter.mirai.plugin.bilibili.utils

import io.ktor.client.request.*
import kotlinx.coroutines.channels.Channel
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.jetbrains.skia.Image
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic.dataFolderPath
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.DynamicItem
import top.colter.mirai.plugin.bilibili.data.DynamicType.*
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.*


internal val logger by BiliBiliDynamic::logger

val biliClient = BiliClient()

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

val DynamicItem.uid: Long
    get() = modules.moduleAuthor.mid

val DynamicItem.time: Long
    get() = (idStr.toLong() shr 32) + 1498838400L

val DynamicItem.formatTime: String
    get() = time.formatTime

val Long.formatTime: String
    get() = formatTime()

fun Long.formatTime(template: String = "yyyy年MM月dd日 HH:mm:ss"): String
    = DateTimeFormatter.ofPattern(template)
        .format(LocalDateTime.ofEpochSecond(this, 0, OffsetDateTime.now().offset))

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


fun loadResource(file: String) =
    BiliBiliDynamic::class.java.getResource(file)?.path!!
//BiliBiliDynamic::class.java.getResource(file)!!.openStream().use { it.readBytes() }

fun loadResourceBytes(path: String) =
    BiliBiliDynamic.getResourceAsStream(path)!!.readBytes()

val cachePath: Path by lazy {
    dataFolderPath.resolve("cache")
}

fun CacheType.cachePath(): Path {
    return cachePath.resolve(path).apply {
        if (notExists()) createDirectories()
    }
}

fun CacheType.cacheFile(filePath: String): Path {
    val split = filePath.split("/")
    val path = split.dropLast(1).joinToString("/")
    val file = split.last()
    return cachePath().resolve(path).apply {
        if (notExists()) createDirectories()
    }.resolve(file)
}

enum class CacheType(val path: String) {
    DRAW("draw"),
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

fun cacheImage(image: Image, path: String, cacheType: CacheType): String {
    val file = cacheType.cacheFile(path)
    file.writeBytes(image.encodeToData()!!.bytes)
    return "${cacheType.path}/$path"
}

suspend fun getOrDownload(url: String, cacheType: CacheType = CacheType.UNKNOWN): ByteArray {
    val fileName = url.split("?").first().split("@").first().split("/").last()

    val filePath = if (cacheType == CacheType.UNKNOWN) {
        cachePath.findFile(fileName) ?: CacheType.OTHER.cacheFile(fileName)
    } else {
        cacheType.cacheFile(fileName)
    }
    return if (filePath.exists()) {
        filePath.setLastModifiedTime(FileTime.from(Instant.now()))
        filePath.readBytes()
    } else {
        biliClient.useHttpClient {
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


/**
 * 查找Contact
 */
fun findContact(del: String): Contact? {
    if (del.isBlank()) return null
    val delegate = del.toLong()
    for (bot in Bot.instances) {
        if (delegate < 0) {
            for (group in bot.groups) {
                if (group.id == delegate * -1) return group
            }
        } else {
            for (friend in bot.friends) {
                if (friend.id == delegate) return friend
            }
            for (stranger in bot.strangers) {
                if (stranger.id == delegate) return stranger
            }
            for (group in bot.groups) {
                for (member in group.members) {
                    if (member.id == delegate) return member
                }
            }
        }
    }
    return null
}

/**
 * 通过正负号区分群和用户
 * @author cssxsh
 */
val Contact.delegate get() = (if (this is Group) id * -1 else id).toString()
