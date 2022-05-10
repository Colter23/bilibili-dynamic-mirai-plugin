package top.colter.mirai.plugin.bilibili.utils

import kotlinx.coroutines.channels.Channel
import top.colter.mirai.plugin.bilibili.data.DynamicItem

fun List<String>.isBlank(): Boolean {
    if (size == 0) return true
    forEach { if (it != "") return false }
    return true
}

fun List<String>.isNotBlank(): Boolean = !isBlank()

val DynamicItem.time: Long
    get() = (idStr.toLong() shr 32) + 1498838400L


suspend fun <E> Channel<E>.sendAll(list: Collection<E>) = list.forEach { send(it) }
