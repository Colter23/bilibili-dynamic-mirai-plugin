package top.colter.mirai.plugin.bilibili.tasker

import kotlinx.coroutines.withTimeout
import org.jetbrains.skia.Color
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.BiliData
import top.colter.mirai.plugin.bilibili.data.*
import top.colter.mirai.plugin.bilibili.data.DynamicType.*
import top.colter.mirai.plugin.bilibili.draw.makeDrawDynamic
import top.colter.mirai.plugin.bilibili.draw.makeRGB
import top.colter.mirai.plugin.bilibili.utils.formatTime
import top.colter.mirai.plugin.bilibili.utils.logger
import top.colter.mirai.plugin.bilibili.utils.mid
import top.colter.mirai.plugin.bilibili.utils.time

object DynamicMessageTasker : BiliTasker() {

    override var interval: Int = 0

    private val dynamicChannel by BiliBiliDynamic::dynamicChannel
    private val messageChannel by BiliBiliDynamic::messageChannel

    private val dynamic by BiliData::dynamic
    private val bangumi by BiliData::bangumi

    override suspend fun main() {
        val dynamicDetail = dynamicChannel.receive()
        withTimeout(180002) {
            val dynamicItem = dynamicDetail.item
            logger.debug("动态: ${dynamicItem.modules.moduleAuthor.name}@${dynamicItem.idStr}@${dynamicItem.typeStr}")
            messageChannel.send(dynamicItem.buildMessage(dynamicDetail.contact))
        }
    }

    suspend fun DynamicItem.buildMessage(contact: String? = null): DynamicMessage {
        return DynamicMessage(
            did,
            modules.moduleAuthor.mid,
            modules.moduleAuthor.name,
            type,
            formatTime,
            time.toInt(),
            textContent(),
            dynamicImages(),
            dynamicLinks(),
            makeDynamic(),
            contact
        )
    }

    fun DynamicItem.textContent(): String {
        return when (type) {
            DYNAMIC_TYPE_FORWARD -> "${modules.moduleDynamic.desc?.text}\n\n 转发动态:\n${orig?.textContent()}"
            DYNAMIC_TYPE_WORD,
            DYNAMIC_TYPE_DRAW -> modules.moduleDynamic.desc?.text ?: ""
            DYNAMIC_TYPE_ARTICLE -> modules.moduleDynamic.major?.article?.title!!
            DYNAMIC_TYPE_AV -> modules.moduleDynamic.major?.archive?.title!!
            DYNAMIC_TYPE_MUSIC -> modules.moduleDynamic.major?.music?.title!!
            DYNAMIC_TYPE_PGC -> modules.moduleDynamic.major?.pgc?.title!!
            DYNAMIC_TYPE_UGC_SEASON -> modules.moduleDynamic.major?.ugcSeason?.title!!
            DYNAMIC_TYPE_COMMON_VERTICAL,
            DYNAMIC_TYPE_COMMON_SQUARE -> modules.moduleDynamic.major?.common?.title!!
            DYNAMIC_TYPE_LIVE -> modules.moduleDynamic.major?.live?.title!!
            DYNAMIC_TYPE_LIVE_RCMD -> modules.moduleDynamic.major?.liveRcmd?.liveInfo?.livePlayInfo?.title!!
            DYNAMIC_TYPE_NONE -> modules.moduleDynamic.major?.none?.tips!!
            DYNAMIC_TYPE_UNKNOWN -> "未知的动态类型: $typeStr"
        }
    }

    fun DynamicItem.dynamicImages(): List<String>? {
        return when (type) {
            DYNAMIC_TYPE_FORWARD -> orig?.dynamicImages()!!
            DYNAMIC_TYPE_DRAW -> modules.moduleDynamic.major?.draw?.items?.map { it.src }
            DYNAMIC_TYPE_ARTICLE -> modules.moduleDynamic.major?.article?.covers
            DYNAMIC_TYPE_AV -> listOf(modules.moduleDynamic.major?.archive?.cover!!)
            DYNAMIC_TYPE_MUSIC -> listOf(modules.moduleDynamic.major?.music?.cover!!)
            DYNAMIC_TYPE_PGC -> listOf(modules.moduleDynamic.major?.pgc?.cover!!)
            DYNAMIC_TYPE_UGC_SEASON -> listOf(modules.moduleDynamic.major?.ugcSeason?.cover!!)
            DYNAMIC_TYPE_COMMON_SQUARE -> listOf(modules.moduleDynamic.major?.common?.cover!!)
            DYNAMIC_TYPE_LIVE -> listOf(modules.moduleDynamic.major?.live?.cover!!)
            DYNAMIC_TYPE_LIVE_RCMD -> listOf(modules.moduleDynamic.major?.liveRcmd?.liveInfo?.livePlayInfo?.cover!!)
            else -> listOf()
        }
    }

    suspend fun DynamicItem.dynamicLinks(): List<DynamicMessage.Link> {
        return when (type) {
            DYNAMIC_TYPE_FORWARD -> {
                listOf(
                    DynamicMessage.Link("动态", DYNAMIC_LINK(did)),
                    DynamicMessage.Link("原动态", DYNAMIC_LINK(orig!!.did)),
                )
            }

            DYNAMIC_TYPE_NONE,
            DYNAMIC_TYPE_WORD,
            DYNAMIC_TYPE_DRAW,
            DYNAMIC_TYPE_COMMON_VERTICAL,
            DYNAMIC_TYPE_COMMON_SQUARE,
            DYNAMIC_TYPE_UGC_SEASON,
            DYNAMIC_TYPE_UNKNOWN -> {
                listOf(
                    DynamicMessage.Link("动态", DYNAMIC_LINK(did))
                )
            }

            DYNAMIC_TYPE_ARTICLE -> {
                listOf(
                    DynamicMessage.Link(
                        DYNAMIC_TYPE_ARTICLE.text,
                        ARTICLE_LINK(this.modules.moduleDynamic.major?.article?.id!!.toString())
                    ),
                    DynamicMessage.Link("动态", DYNAMIC_LINK(did))
                )
            }

            DYNAMIC_TYPE_AV -> {
                listOf(
                    DynamicMessage.Link(
                        DYNAMIC_TYPE_AV.text,
                        VIDEO_LINK(this.modules.moduleDynamic.major?.archive?.aid.toString())
                    ),
                    DynamicMessage.Link("动态", DYNAMIC_LINK(did))
                )
            }

            DYNAMIC_TYPE_MUSIC -> {
                listOf(
                    DynamicMessage.Link(
                        DYNAMIC_TYPE_MUSIC.text,
                        MUSIC_LINK(this.modules.moduleDynamic.major?.music?.id!!.toString())
                    ),
                    DynamicMessage.Link("动态", DYNAMIC_LINK(did))
                )
            }

            DYNAMIC_TYPE_PGC -> {
                listOf(
                    DynamicMessage.Link(DYNAMIC_TYPE_PGC.text, EPISODE_LINK(this.modules.moduleDynamic.major?.pgc?.epid!!.toString())),
                    DynamicMessage.Link("动态", DYNAMIC_LINK(did))
                )
            }

            DYNAMIC_TYPE_LIVE -> {
                listOf(
                    DynamicMessage.Link(
                        DYNAMIC_TYPE_LIVE.text,
                        LIVE_LINK(this.modules.moduleDynamic.major?.live?.id!!.toString())
                    ),
                    DynamicMessage.Link("动态", DYNAMIC_LINK(did))
                )
            }

            DYNAMIC_TYPE_LIVE_RCMD -> {
                listOf(
                    DynamicMessage.Link(
                        DYNAMIC_TYPE_LIVE_RCMD.text,
                        LIVE_LINK(this.modules.moduleDynamic.major?.liveRcmd?.liveInfo?.livePlayInfo?.roomId!!.toString())
                    ),
                    DynamicMessage.Link("动态", DYNAMIC_LINK(did))
                )
            }

        }

    }

    suspend fun DynamicItem.makeDynamic(): String? {
        return if (BiliConfig.enableConfig.drawEnable) {
            val color = (if (this.type == DYNAMIC_TYPE_PGC) bangumi[mid]?.color else dynamic[mid]?.color)
                ?: BiliConfig.imageConfig.defaultColor
            val colors = color.split(";", "；").map { Color.makeRGB(it.trim()) }
            makeDrawDynamic(colors)
        } else null
    }

}