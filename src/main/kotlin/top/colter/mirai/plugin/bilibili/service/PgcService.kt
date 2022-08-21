package top.colter.mirai.plugin.bilibili.service

import top.colter.mirai.plugin.bilibili.Bangumi
import top.colter.mirai.plugin.bilibili.api.followPgc
import top.colter.mirai.plugin.bilibili.api.pgcEpisodeInfo
import top.colter.mirai.plugin.bilibili.api.pgcMediaInfo
import top.colter.mirai.plugin.bilibili.api.pgcSeasonInfo

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
        //return client.followPgc(ssid)?.toast!!
        client.followPgc(ssid) ?: return "追番失败"
        bangumi.getOrPut(ssid) {
            val season = client.pgcSeasonInfo(ssid) ?: return "获取番剧信息失败, 如果是港澳台番剧请用 media id (md11111) 订阅"
            Bangumi(season.title, season.seasonId, season.mediaId, type(season.type))
        }.apply {
            contacts.add(subject)
            return "追番成功( •̀ ω •́ )✧ [$title]"
        }
    }

    suspend fun followPgcByMdid(mdid: Long, subject: String): String {
        val season = client.pgcMediaInfo(mdid) ?: return "获取番剧信息失败"
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
        val season = client.pgcEpisodeInfo(epid) ?: return "获取番剧信息失败, 如果是港澳台番剧请用 media id (md11111) 订阅"
        client.followPgc(season.seasonId) ?: return "追番失败"
        bangumi.getOrPut(season.seasonId) {
            Bangumi(season.title, season.seasonId, season.mediaId, type(season.type))
        }.apply {
            contacts.add(subject)
            return "追番成功( •̀ ω •́ )✧ [$title]"
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