package top.colter.mirai.plugin.bilibili.tasker

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import net.mamoe.mirai.Bot
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic.currentBot
import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.NapcatConfig
import top.colter.mirai.plugin.bilibili.utils.logger


object NapcatCheckTasker : BiliTasker() {
    override var interval: Int = 10

    private val napcat by NapcatConfig::napcat

    private val client = HttpClient(OkHttp) {
        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
        expectSuccess = true
        //Json { json }
    }

    private val status: MutableMap<Long, Boolean> = mutableMapOf()

    override suspend fun main() {
        var lock = false
        var botDown = 0L
        for (nc in napcat) {
            try {
                val res = client.post(nc.url) {
                    header("Authorization", "Bearer ${nc.token}")
                    setBody("{}")
                }.body<String>()

                if (res.contains("\"online\":true")) {
                    if (currentBot != nc.qq) {
                        if (!lock) {
                            currentBot = nc.qq
                            Bot.getInstanceOrNull(nc.qq)?.let {
                                it.getFriend(BiliConfig.admin)?.sendMessage("主Bot已切换至: ${it.nick}(${it.id})")
                            }
                        }
                    }
                    if (!status.containsKey(nc.qq) || status[nc.qq] == false) {
                        status[nc.qq] = true
                    }
                    lock = true
                } else {
                    if (!status.containsKey(nc.qq)) {
                        status[nc.qq] = false
                    }
                    if (status[nc.qq] == true) {
                        status[nc.qq] = false
                        botDown = nc.qq
                    }
                }
            } catch (e: Exception) {
                logger.warning("访问Napcat失败", e)
            }

        }

        if (botDown != 0L) {
            if (currentBot != null) {
                Bot.getInstanceOrNull(currentBot!!)?.let {
                    it.getFriend(BiliConfig.admin)?.sendMessage("Bot已下线: $botDown")
                }
            } else {
                logger.warning("Bot已下线: $botDown")
            }

        }

    }

}
