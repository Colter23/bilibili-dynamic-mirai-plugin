package top.colter.mirai.plugin.bilibili.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.contact.Group
import top.colter.mirai.plugin.bilibili.AtAllType
import top.colter.mirai.plugin.bilibili.command.GroupOrContact
import top.colter.mirai.plugin.bilibili.command.subject

object AtAllService {
    private val mutex = Mutex()

    private fun toAtAllType(type: String) =
        when (type.lowercase()) {
            "全部", "all", "a" -> AtAllType.ALL
            "全部动态", "dynamic", "d" -> AtAllType.DYNAMIC
            "直播", "live", "l" -> AtAllType.LIVE
            "视频", "video", "v" -> AtAllType.VIDEO
            "音乐", "music", "m" -> AtAllType.MUSIC
            "专栏", "article" -> AtAllType.ARTICLE
            else -> null
        }

    suspend fun addAtAll(type: String, uid: Long = 0L, target: GroupOrContact) = mutex.withLock {
        val atAllType = toAtAllType(type) ?: return "没有这个类型哦 [$type]"
        if (target.group == null) {
            if (target.contact !is Group) return "仅在群聊中有用哦"
            if (target.contact.botPermission.level == 0) return "Bot不为管理员, 无法使用At全体"
        }
        val list = atAll.getOrPut(target.subject) { mutableMapOf() }.getOrPut(uid) { mutableSetOf() }
        if (list.isEmpty()) {
            list.add(atAllType)
            atAll[target.subject]?.set(uid, list)
        } else when (atAllType) {
            AtAllType.ALL -> {
                list.clear()
                list.add(atAllType)
            }
            AtAllType.DYNAMIC -> {
                list.removeAll(listOf(AtAllType.ALL, AtAllType.VIDEO, AtAllType.MUSIC, AtAllType.ARTICLE))
                list.add(atAllType)
            }
            AtAllType.LIVE -> {
                list.remove(AtAllType.ALL)
                list.add(atAllType)
            }
            else -> {
                list.remove(AtAllType.ALL)
                list.remove(AtAllType.DYNAMIC)
                list.add(atAllType)
            }
        }
        "添加成功"
    }

    suspend fun delAtAll(type: String, uid: Long = 0L, subject: String) = mutex.withLock {
        val atAllType = toAtAllType(type) ?: return@withLock "没有这个类型哦 [$type]"
        if (atAll[subject]?.get(uid)?.remove(atAllType) == true) "删除成功" else "删除失败"
    }

    suspend fun listAtAll(uid: Long = 0L, subject: String) = mutex.withLock {
        val list = atAll[subject]?.get(uid)
        if (list.isNullOrEmpty()) return@withLock "没有At全体项哦"
        buildString { list.forEach { appendLine(it.value) } }
    }
}

