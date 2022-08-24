package top.colter.mirai.plugin.bilibili.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.SubData
import top.colter.mirai.plugin.bilibili.api.follow
import top.colter.mirai.plugin.bilibili.api.groupAddUser
import top.colter.mirai.plugin.bilibili.api.isFollow
import top.colter.mirai.plugin.bilibili.api.userInfo
import top.colter.mirai.plugin.bilibili.utils.actionNotify
import top.colter.mirai.plugin.bilibili.utils.findContact

object DynamicService {
    private val mutex = Mutex()

    suspend fun listenAll(subject: String) = mutex.withLock {
        dynamic.forEach { (_, sub) ->
            if (subject in sub.contacts) {
                sub.contacts.remove(subject)
            }
        }
        val user = dynamic[0]
        user?.contacts?.add(subject)
    }

    suspend fun cancelListen(subject: String) = mutex.withLock {
        dynamic[0]?.contacts?.remove(subject)
    }

    private suspend fun followUser(uid: Long): String? {
        if (uid == BiliBiliDynamic.uid) return null

        val attr = client.isFollow(uid)?.attribute
        if (attr == 0) {
            if (!BiliConfig.accountConfig.autoFollow) return "未关注此用户"
            else {
                val res = client.follow(uid)
                if (res.code != 0) return "关注失败: ${res.message}"
                if (BiliConfig.accountConfig.followGroup.isNotEmpty()) {
                    val res1 = client.groupAddUser(uid, BiliBiliDynamic.tagid)
                    if (res1.code != 0) logger.error("移动分组失败: ${res1.message}")
                }
                actionNotify("通知: 账号关注 $uid")
            }
        } else if (attr == 128) return "此账号已被拉黑"
        return null
    }

    suspend fun setColor(uid: Long, color: String) = mutex.withLock {
        color.split(";", "；").forEach {
            if (it.first() != '#' || it.length != 7) return@withLock "格式错误，请输入16进制颜色，如: #d3edfa"
        }
        dynamic[uid]?.color = color
        "设置完成"
    }

    suspend fun addSubscribe(uid: Long, subject: String) = mutex.withLock {
        if (isFollow(uid, subject)) return@withLock "之前订阅过这个人哦"
        if (dynamic[0]?.contacts?.contains(subject) == true) dynamic[0]?.contacts?.remove(subject)

        if (!dynamic.containsKey(uid)) {
            val m = followUser(uid)
            if (m != null) return@withLock m
            val u = client.userInfo(uid)
            dynamic[uid] = SubData(u?.name!!)
        }

        //dynamic[uid]?.contacts?.apply {
        //    try {
        //        subject.toLong()
        //    } catch (e: NumberFormatException) {
        //        group[subject]?.contacts?.let {
        //            removeAll(it)
        //        }
        //    }
        //    add(subject)
        //}
        dynamic[uid]?.contacts?.add(subject)
        "订阅 ${dynamic[uid]?.name} 成功!"
    }

    suspend fun removeSubscribe(uid: Long, subject: String) = mutex.withLock {
        if (!isFollow(uid, subject)) return@withLock "还未订阅此人哦"
        val user = dynamic[uid]!!
        if (user.contacts.remove(subject)) {
            if (user.contacts.isEmpty()) dynamic.remove(uid)
            if (filter[subject]?.run {
                    remove(uid)
                    isEmpty()
                } == true) filter.remove(subject)
            if (atAll[subject]?.run {
                    remove(uid)
                    isEmpty()
                } == true) atAll.remove(subject)
            "对 ${user.name} 取消订阅成功"
        }else "取消订阅失败"
    }

    suspend fun removeAllSubscribe(subject: String) = mutex.withLock {
        filter.remove(subject)
        atAll.remove(subject)
        group.forEach { (_, g) -> g.contacts.remove(subject) }
        dynamic.count { (_, sub) -> sub.contacts.remove(subject) }
    }

    suspend fun list(subject: String) = mutex.withLock {
        buildString {
            appendLine("目标: $subject")
            appendLine()
            appendLine("UP主: ")
            val c = dynamic.count { (uid, sub) ->
                if (subject in sub.contacts) {
                    appendLine("${sub.name}@$uid")
                    true
                }else false
            }
            if (c == 0) appendLine("无")
            appendLine()
            appendLine("番剧: ")
            val cc = bangumi.count { (ssid, sub) ->
                if (subject in sub.contacts) {
                    appendLine("${sub.title}@ss$ssid")
                    true
                }else false
            }
            if (cc == 0) appendLine("无")
            appendLine()
            append("共 ${c + cc} 个订阅")
        }
    }

    suspend fun listAll() = mutex.withLock {
        var count = 0
        buildString {
            appendLine("名称@UID#订阅人数")
            appendLine()
            dynamic.forEach { (uid, sub) ->
                appendLine("${sub.name}@$uid#${sub.contacts.size}")
                count++
            }
            appendLine()
            append("共 $count 个订阅")
        }
    }

    suspend fun listUser(uid: Long? = null) = mutex.withLock {
        buildString {
            val user = mutableSetOf<String>()
            if (uid == null) {
                dynamic.forEach { (_, sub) ->
                    user.addAll(sub.contacts)
                }
            }else {
                val u = dynamic[uid]?: return@withLock "没有这个用户哦 [$uid]"
                appendLine("${u.name}[$uid]")
                appendLine()
                user.addAll(u.contacts)
            }
            val group = StringBuilder()
            val friend = StringBuilder()
            val gg = StringBuilder()
            user.forEach {
                try {
                    it.toLong()
                    findContact(it).apply {
                        when (this) {
                            is Group -> group.appendLine("$name@$id")
                            is Friend -> friend.appendLine("$nick@$id")
                        }
                    }
                }catch (e: NumberFormatException) {
                    gg.appendLine(it)
                }
            }
            appendLine("====群====")
            append(group.ifEmpty { "无\n" })
            appendLine("====好友====")
            append(friend.ifEmpty { "无\n" })
            appendLine("====分组====")
            append(gg.ifEmpty { "无\n" })
            appendLine()
            append("共 ${user.size} 名用户")
        }

    }
}