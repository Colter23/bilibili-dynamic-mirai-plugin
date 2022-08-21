package top.colter.mirai.plugin.bilibili.service

import net.mamoe.mirai.event.MessageSelectBuilder
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.whileSelectMessages
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.BiliData
import top.colter.mirai.plugin.bilibili.client.BiliClient

internal val logger by BiliBiliDynamic::logger

val client = BiliClient()

val dynamic by BiliData::dynamic
val filter by BiliData::filter
val group by BiliData::group
val atAll by BiliData::atAll
val bangumi by BiliData::bangumi


fun isFollow(uid: Long, subject: String) =
    uid == 0L || (dynamic.containsKey(uid) && dynamic[uid]!!.contacts.contains(subject))


suspend inline fun <reified T : MessageEvent> T.whileSelect(
    count: Int = 2,
    timeout: Long = 120_000,
    defaultReply: String = "没有这个选项哦",
    crossinline selectBuilder: MessageSelectBuilder<T, Boolean>.() -> Unit
): String? {
    var c = 0
    var res: String? = null
    whileSelectMessages {
        "退出" {
            subject.sendMessage("已退出")
            res = "退出"
            false
        }
        apply(selectBuilder)
        default {
            c++
            subject.sendMessage("$defaultReply${if (c < count) ", 请重新输入" else ", 超出重试次数, 退出"}")
            if (c >= count) res = "超次"
            c < count
        }
        timeout(timeout) {
            res = "超时"
            false
        }
    }
    return res
}
