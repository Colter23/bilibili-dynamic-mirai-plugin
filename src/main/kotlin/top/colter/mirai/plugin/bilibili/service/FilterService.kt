package top.colter.mirai.plugin.bilibili.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import top.colter.mirai.plugin.bilibili.DynamicFilter
import top.colter.mirai.plugin.bilibili.DynamicFilterType
import top.colter.mirai.plugin.bilibili.FilterMode
import top.colter.mirai.plugin.bilibili.FilterType

object FilterService {
    private val mutex = Mutex()

    suspend fun addFilter(type: FilterType, mode: FilterMode?, regex: String?, uid: Long, subject: String) =
        mutex.withLock {
            if (!isFollow(uid, subject)) return@withLock "还未订阅此人哦"

            if (!filter.containsKey(subject)) filter[subject] = mutableMapOf()
            if (!filter[subject]!!.containsKey(uid)) filter[subject]!![uid] = DynamicFilter()

            val dynamicFilter = filter[subject]!![uid]!!
            when (type) {
                FilterType.TYPE -> {
                    if (mode != null) dynamicFilter.typeSelect.mode = mode
                    if (regex != null && regex != "") {
                        val t = when (regex) {
                            "动态" -> DynamicFilterType.DYNAMIC
                            "转发动态" -> DynamicFilterType.FORWARD
                            "视频" -> DynamicFilterType.VIDEO
                            "音乐" -> DynamicFilterType.MUSIC
                            "专栏" -> DynamicFilterType.ARTICLE
                            "直播" -> DynamicFilterType.LIVE
                            else -> return@withLock "没有这个类型 $regex"
                        }
                        dynamicFilter.typeSelect.list.add(t)
                    }
                }
                FilterType.REGULAR -> {
                    if (mode != null) dynamicFilter.regularSelect.mode = mode
                    if (regex != null && regex != "") dynamicFilter.regularSelect.list.add(regex)
                }
            }
            "设置成功"
        }

    suspend fun listFilter(uid: Long, subject: String) = mutex.withLock {
        if (!isFollow(uid, subject)) return@withLock "还未订阅此人哦"

        if (!(filter.containsKey(subject) && filter[subject]!!.containsKey(uid))) return@withLock "目标没有过滤器"

        buildString {
            //appendLine("当前目标过滤器: ")
            //appendLine()
            val typeSelect = filter[subject]!![uid]!!.typeSelect
            if (typeSelect.list.isNotEmpty()) {
                append("动态类型过滤器: ")
                appendLine(typeSelect.mode.value)
                typeSelect.list.forEachIndexed { index, type -> appendLine("  t$index: ${type.value}") }
                appendLine()
            }
            val regularSelect = filter[subject]!![uid]!!.regularSelect
            if (regularSelect.list.isNotEmpty()) {
                append("正则过滤器: ")
                appendLine(regularSelect.mode.value)
                regularSelect.list.forEachIndexed { index, reg -> appendLine("  r$index: $reg") }
                appendLine()
            }
        }
    }

    suspend fun delFilter(index: String, uid: Long, subject: String) = mutex.withLock {
        if (!isFollow(uid, subject)) return@withLock "还未订阅此人哦"
        if (!(filter.containsKey(subject) && filter[subject]!!.containsKey(uid))) return@withLock "当前目标没有过滤器"

        var i = 0
        runCatching {
            i = index.substring(1).toInt()
        }.onFailure {
            return@withLock "索引错误"
        }
        var flag = false
        val filter = if (index[0] == 't') {
            flag = true
            filter[subject]!![uid]!!.typeSelect.list
        } else if (index[0] == 'r') {
            filter[subject]!![uid]!!.regularSelect.list
        } else return@withLock "索引类型错误"
        if (filter.size < i) return@withLock "索引超出范围"
        val t = filter[i]
        filter.removeAt(i)

        if (flag) "已删除 ${(t as DynamicFilterType).value} 类型过滤"
        else "已删除 ${(t as String)} 正则过滤"
    }
}
