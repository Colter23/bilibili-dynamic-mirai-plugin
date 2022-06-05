package top.colter.mirai.plugin.bilibili.tasker

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImageTo
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import top.colter.mirai.plugin.bilibili.*
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic.save
import top.colter.mirai.plugin.bilibili.BiliDynamicConfig.accountConfig
import top.colter.mirai.plugin.bilibili.api.*
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.draw.LoginQrCodeDraw
import top.colter.mirai.plugin.bilibili.utils.findContact
import java.net.URI

internal val logger by BiliBiliDynamic::logger

object BiliDataTasker {

    val mutex = Mutex()

    val client = BiliClient()

    val dynamic: MutableMap<Long, SubData> by BiliDynamicData::dynamic

    suspend fun listenAll(subject: String) = mutex.withLock {
        dynamic.forEach { (uid, sub) ->
            if (subject in sub.contacts) {
                sub.contacts.remove(subject)
            }
        }
        val user = dynamic[0]
        user?.contacts?.set(subject, "11")
    }

    suspend fun cancelListen(subject: String) = mutex.withLock {
        dynamic[0]?.contacts?.remove(subject)
    }

    private suspend fun followUser(uid: Long): String {
        if (uid == BiliBiliDynamic.mid) {
            return ""
        }

        val attr = client.isFollow(uid)?.attribute
        if (attr == 0) {
            if (!accountConfig.autoFollow) {
                return "未关注此用户"
            } else {
                val res = client.follow(uid)
                if (res.code != 0) {
                    return "关注失败: ${res.message}"
                }
                if (accountConfig.followGroup.isNotEmpty()) {
                    val res1 = client.groupAddUser(uid, BiliBiliDynamic.tagid)
                    if (res1.code != 0) {
                        logger.error("移动分组失败: ${res1.message}")
                    }
                }
            }
        } else if (attr == 128) {
            return "此账号已被拉黑"
        }
        return ""
    }

    suspend fun setColor(uid: Long, color: String): String {
        if (color.first() != '#' || color.length != 7) {
            return "格式错误，请输入16进制颜色，如: #d3edfa"
        }
        mutex.withLock {
            dynamic[uid]?.color = color
        }
        return "设置完成"
    }

    suspend fun addSubscribe(uid: Long, subject: String) = mutex.withLock {
        if (dynamic[0]?.contacts?.contains(subject) == true) {
            dynamic[0]?.contacts?.remove(subject)
        }
        val m = followUser(uid)
        if (m != "") {
            return@withLock m
        }
        val user = dynamic[uid]
        if (user == null) {
            val u = client.userInfo(uid)
            val subData = SubData(u?.name!!)
            subData.contacts[subject] = "11"
            dynamic[uid] = subData
            "订阅 ${dynamic[uid]?.name} 成功! \n默认检测 动态+视频+直播 如果需要调整请发送/bili set $uid\n如要设置主题色请发送/bili color $uid <16进制颜色>"
        } else {
            if (user.contacts.contains(subject)) {
                "之前订阅过这个人哦"
            } else {
                user.contacts[subject] = "11"
                "订阅 ${dynamic[uid]?.name} 成功! \n默认检测 动态+视频+直播 如果需要调整请发送/bili set $uid"
            }

        }
    }

    suspend fun addFilter(regex: String, uid: Long, subject: String, mode: Boolean = true) = mutex.withLock {
        if (dynamic.containsKey(uid)) {
            val filter = if (mode) dynamic[uid]?.filter else dynamic[uid]?.containFilter
            if (filter?.containsKey(subject) == true) {
                filter[subject]?.add(regex)
            } else {
                filter?.set(subject, mutableListOf(regex))
            }
            "设置成功"
        } else {
            "还未关注此人哦"
        }
    }

    suspend fun listFilter(uid: Long, subject: String) = mutex.withLock {
        if (dynamic.containsKey(uid)) {
            return@withLock buildString {
                appendLine("过滤 ")
                if (dynamic[uid]?.filter?.containsKey(subject) == true && dynamic[uid]?.filter?.get(subject)?.size!! > 0) {
                    dynamic[uid]?.filter?.get(subject)?.forEachIndexed { index, s ->
                        appendLine("f$index: $s")
                    }
                } else {
                    appendLine("还没有设置过滤哦")
                }
                appendLine("包含 ")
                if (dynamic[uid]?.containFilter?.containsKey(subject) == true && dynamic[uid]?.containFilter?.get(
                        subject
                    )?.size!! > 0
                ) {
                    dynamic[uid]?.containFilter?.get(subject)?.forEachIndexed { index, s ->
                        appendLine("c$index: $s")
                    }
                } else {
                    appendLine("还没有设置包含哦")
                }
            }
        } else {
            "还未关注此人哦"
        }
    }

    suspend fun delFilter(uid: Long, subject: String, index: String) = mutex.withLock {
        if (dynamic.containsKey(uid)) {
            var i = 0
            runCatching {
                i = index.substring(1).toInt()
            }.onFailure {
                return@withLock "索引错误"
            }
            val filter = if (index[0] == 'f') {
                dynamic[uid]?.filter
            } else if (index[0] == 'c') {
                dynamic[uid]?.containFilter
            } else {
                return@withLock "索引值错误"
            }
            if (filter?.containsKey(subject) == true) {
                if (filter[subject]?.size!! < i) return@withLock "索引超出范围"
                val ft = filter[subject]?.get(i)
                filter[subject]?.removeAt(i)
                "已删除 $ft 过滤"
            } else {
                "还没有设置过滤哦"
            }
        } else {
            "还未关注此人哦"
        }
    }

    suspend fun removeSubscribe(uid: Long, subject: String) = mutex.withLock {
        val user = dynamic[uid]
        user?.contacts?.remove(subject)
        user
    }

    suspend fun removeAllSubscribe(subject: String) = mutex.withLock {
        dynamic.count { (uid, sub) ->
            if (sub.contacts.contains(subject)) {
                sub.contacts.remove(subject)
                true
            } else false
        }
    }

    suspend fun list(subject: String) = mutex.withLock {
        var count = 0
        buildString {
            dynamic.forEach { (uid, sub) ->
                if (subject in sub.contacts) {
                    appendLine("${sub.name}@$uid")
                    count++
                }
            }
            append("共 $count 个订阅")
        }
    }

    suspend fun listAll() = mutex.withLock {
        var count = 0
        buildString {
            appendLine("名称@UID#订阅人数")
            appendLine()
            dynamic.forEach { (uid, sub) ->
                appendLine("${sub.name}@$uid#${sub.contacts.keys.size}")
                count++
            }
            appendLine()
            append("共 $count 个订阅")
        }
    }

    suspend fun listUser() = mutex.withLock {
        buildString {
            val user = mutableSetOf<String>()
            dynamic.forEach { (uid, sub) ->
                user.addAll(sub.contacts.keys)
            }
            val group = StringBuilder()
            val friend = StringBuilder()
            user.forEach {
                findContact(it).apply {
                    when (this) {
                        is Group -> group.appendLine("${name}@${id}")
                        is Friend -> friend.appendLine("${nick}@${id}")
                    }
                }
            }
            appendLine("====群====")
            append(group.ifEmpty { "无\n" })
            appendLine("====好友====")
            append(friend.ifEmpty { "无\n" })
            appendLine()
            append("共 ${user.size} 名用户")
        }
    }

    suspend fun login(contact: Contact) {
        val loginData = client.getLoginUrl().data!!

        val image = LoginQrCodeDraw.qrCode(loginData.url)
        image.encodeToData()!!.bytes.toExternalResource().toAutoCloseable().sendAsImageTo(contact)
        contact.sendMessage("请使用BiliBili手机APP扫码登录 3分钟有效")

        runCatching {
            withTimeout(180000) {
                while (true) {
                    val loginInfo = client.loginInfo(loginData.oauthKey!!)

                    if (loginInfo.status == true) {
                        val url = loginInfo.data!!.url
                        val querys = URI(url).query.split("&")
                        val cookie = buildString {
                            querys.forEach {
                                if (it.contains("SESSDATA") || it.contains("bili_jct")) append("$it; ")
                            }
                        }
                        accountConfig.cookie = cookie
                        BiliDynamicConfig.save()
                        BiliBiliDynamic.cookie.parse(cookie)
                        initTagid()
                        //getHistoryDynamic()
                        contact.sendMessage("登录成功!")
                        break
                    }
                    delay(3000)
                }
            }
        }.onFailure {
            contact.sendMessage("登录失败 ${it.message}")
        }

    }

}