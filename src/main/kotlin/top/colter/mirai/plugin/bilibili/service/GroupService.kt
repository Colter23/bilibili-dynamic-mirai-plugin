package top.colter.mirai.plugin.bilibili.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.contact.Friend
import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.BiliData
import top.colter.mirai.plugin.bilibili.Group
import top.colter.mirai.plugin.bilibili.utils.delegate
import top.colter.mirai.plugin.bilibili.utils.findContactAll
import top.colter.mirai.plugin.bilibili.utils.name

object GroupService {
    private val mutex = Mutex()

    suspend fun createGroup(name: String, operator: Long) = mutex.withLock {
        if (!group.containsKey(name)) {
            if (name.matches("^[0-9]*$".toRegex())) return@withLock "分组名不能全为数字"
            group[name] = Group(name, operator)
            "创建成功"
        }else "分组名称重复"
    }

    suspend fun delGroup(name: String, operator: Long) = mutex.withLock {
        if (group.containsKey(name)) {
            if (group[name]!!.creator == operator) {
                dynamic.forEach { (_, s) -> s.contacts.remove(name) }
                BiliData.dynamicPushTemplate.forEach { (_, c) -> c.remove(name) }
                BiliData.livePushTemplate.forEach { (_, c) -> c.remove(name) }
                filter.remove(name)
                atAll.remove(name)
                group.remove(name)
                "删除成功"
            }else "无权删除"
        }else "没有此分组 [$name]"
    }

    suspend fun listGroup(name: String? = null, operator: Long) = mutex.withLock {
        if (name == null) {
            group.values.filter {
                operator == BiliConfig.admin || operator == it.creator || it.admin.contains(operator)
            }.joinToString("\n") {
                "${it.name}@${findContactAll(it.creator)?.name?:it.creator}"
            }.ifEmpty { "没有创建或管理任何分组哦" }
        } else {
            group[name]?.toString() ?: "没有此分组哦"
        }
    }

    suspend fun setGroupAdmin(name: String, contacts: String, operator: Long) = mutex.withLock {
        if (group.containsKey(name)) {
            if (group[name]!!.creator == operator) {
                var failMsg = ""
                group[name]?.admin?.addAll(contacts.split(",","，").map {
                    findContactAll(it).run {
                        if (this != null && this is Friend) id else {
                            failMsg += "$it, "
                            null
                        }
                    }
                }.filterNotNull().toSet())
                if (failMsg.isEmpty()) "添加成功"
                else "[$failMsg] 添加失败"
            }else "无权添加"
        }else "没有此分组 [$name]"
    }

    suspend fun banGroupAdmin(name: String, contacts: String, operator: Long) = mutex.withLock {
        if (group.containsKey(name)) {
            if (group[name]!!.creator == operator) {
                var failMsg = ""
                val admin = group[name]!!.admin
                contacts.split(",","，").map {
                    try {
                        it.toLong()
                    }catch (e: NumberFormatException) {
                        failMsg += "$it, "
                        null
                    }
                }.filterNotNull().toSet().forEach {
                    if (!admin.remove(it)) failMsg += "$it, "
                }
                if (failMsg.isEmpty()) "删除成功"
                else "[$failMsg] 删除失败"
            }else "无权删除"
        }else "没有此分组 [$name]"
    }

    suspend fun pushGroupContact(name: String, contacts: String, operator: Long) = mutex.withLock {
        if (group.containsKey(name)) {
            if (checkGroupPerm(name, operator)) {
                var failMsg = ""
                group[name]?.contacts?.addAll(contacts.split(",","，").map {
                    findContactAll(it)?.delegate.apply {
                        if (this == null) failMsg += "$it, "
                    }
                }.filterNotNull().toSet())
                if (failMsg.isEmpty()) "添加成功"
                else "[$failMsg] 添加失败"
            }else "无权添加"
        }else "没有此分组 [$name]"
    }

    suspend fun delGroupContact(name: String, contacts: String, operator: Long) = mutex.withLock {
        if (group.containsKey(name)) {
            if (checkGroupPerm(name, operator)) {
                var failMsg = ""
                group[name]?.contacts?.removeAll(contacts.split(",","，").map {
                    findContactAll(it)?.let {
                        failMsg += "$it, "
                        it.delegate
                    } ?: ""
                }.filter { it.isNotEmpty() }.toSet())
                if (failMsg.isEmpty()) "删除成功"
                else "[$failMsg] 删除失败"
            }else "无权删除"
        }else "没有此分组 [$name]"
    }

    fun checkGroupPerm(name: String, operator: Long): Boolean =
        group[name]?.creator == operator || group[name]?.admin?.contains(operator) == true

}

