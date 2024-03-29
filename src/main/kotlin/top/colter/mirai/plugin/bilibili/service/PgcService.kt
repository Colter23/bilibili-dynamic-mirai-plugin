package top.colter.mirai.plugin.bilibili.service

import top.colter.mirai.plugin.bilibili.Bangumi
import top.colter.mirai.plugin.bilibili.api.followPgc
import top.colter.mirai.plugin.bilibili.api.getEpisodeInfo
import top.colter.mirai.plugin.bilibili.api.getMediaInfo
import top.colter.mirai.plugin.bilibili.api.getSeasonInfo

val pgcRegex = """^((?:ss)|(?:md)|(?:ep))(\d{4,10})$""".toRegex()

object PgcService {

    suspend fun followPgc(id: String, subject: String): String {
        val regex = pgcRegex.find(id) ?: return "ID 格式错误 例(ss11111, md22222, ep33333)"

        val type = regex.destructured.component1()
        val id = regex.destructured.component2().toLong()

        return when (type) {
            "ss" -> followPgcBySsid(id, subject)
            "md" -> followPgcByMdid(id, subject)
            "ep" -> followPgcByEpid(id, subject)
            else -> "额(⊙﹏⊙)"
        }
    }

    suspend fun followPgcBySsid(ssid: Long, subject: String): String {
        client.followPgc(ssid) ?: return "追番失败"
        bangumi.getOrPut(ssid) {
            val season = client.getSeasonInfo(ssid) ?: return "获取番剧信息失败, 如果是港澳台番剧请用 media id (md11111) 订阅"
            Bangumi(season.title, season.seasonId, season.mediaId, type(season.type))
        }.apply {
            contacts.add(subject)
            return "追番成功( •̀ ω •́ )✧ [$title]"
        }
    }

    suspend fun followPgcByMdid(mdid: Long, subject: String): String {
        val season = client.getMediaInfo(mdid) ?: return "获取番剧信息失败"
        val ssid = season.media.seasonId
        client.followPgc(ssid) ?: return "追番失败"
        bangumi.getOrPut(ssid) {
            Bangumi(season.media.title, ssid, season.media.mediaId, season.media.typeName)
        }.apply {
            contacts.add(subject)
            return "追番成功( •̀ ω •́ )✧ [$title]"
        }
    }

    suspend fun followPgcByEpid(epid: Long, subject: String): String {
        val season = client.getEpisodeInfo(epid) ?: return "获取番剧信息失败, 如果是港澳台番剧请用 media id (md11111) 订阅"
        client.followPgc(season.seasonId) ?: return "追番失败"
        bangumi.getOrPut(season.seasonId) {
            Bangumi(season.title, season.seasonId, season.mediaId, type(season.type))
        }.apply {
            contacts.add(subject)
            return "追番成功( •̀ ω •́ )✧ [$title]"
        }
    }

    fun delPgc(id: String, subject: String): String {
        val regex = pgcRegex.find(id) ?: return "ID 格式错误 例(ss11111, md22222)"

        val type = regex.destructured.component1()
        val id = regex.destructured.component2().toLong()

        return when (type) {
            "ss" -> {
                val pgc = bangumi[id] ?: return "没有这个番剧哦"
                if (pgc.contacts.remove(subject)) {
                    if (pgc.contacts.isEmpty()) bangumi.remove(id)
                    "删除成功"
                } else "没有订阅这个番剧哦"
            }
            "md" -> {
                val pgc = bangumi.filter { it.value.mediaId == id }.values
                if (pgc.isEmpty()) return "没有这个番剧哦"
                val contacts = pgc.first().contacts
                if (contacts.remove(subject)) {
                    if (contacts.isEmpty()) bangumi.remove(pgc.first().seasonId)
                    "删除成功"
                } else "没有订阅这个番剧哦"
            }
            "ep" -> "无法通过ep进行删除，请使用 ss 或 md"
            else -> "额(⊙﹏⊙)"
        }
    }

    fun type(type: Int) = when (type) {
        1 -> "番剧"
        2 -> "电影"
        3 -> "纪录片"
        4 -> "国创"
        5 -> "电视剧"
        7 -> "综艺"
        else -> "未知"
    }


}