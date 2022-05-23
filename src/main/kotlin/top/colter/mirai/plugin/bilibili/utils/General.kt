package top.colter.mirai.plugin.bilibili.utils

import kotlinx.coroutines.channels.Channel
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic.dataFolderPath
import top.colter.mirai.plugin.bilibili.data.DynamicItem
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun List<String>.isBlank(): Boolean {
    if (size == 0) return true
    forEach { if (it != "") return false }
    return true
}

fun List<String>.isNotBlank(): Boolean = !isBlank()

val DynamicItem.time: Long
    get() = (idStr.toLong() shr 32) + 1498838400L

val DynamicItem.formatTime: String
    get() = DateTimeFormatter.ofPattern("yyyy年MM月dd日  HH:mm:ss")
        .format(LocalDateTime.ofEpochSecond(time, 0, OffsetDateTime.now().offset))

suspend fun <E> Channel<E>.sendAll(list: Collection<E>) = list.forEach { send(it) }

val cachePath: Path by lazy {
    dataFolderPath.resolve("cache")
}

val dynamicCachePath: Path by lazy {
    cachePath.resolve("dynamic")
}

