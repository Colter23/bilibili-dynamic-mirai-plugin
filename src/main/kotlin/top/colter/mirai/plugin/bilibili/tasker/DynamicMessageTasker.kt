package top.colter.mirai.plugin.bilibili.tasker

import org.jetbrains.skia.Color
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.BiliData
import top.colter.mirai.plugin.bilibili.data.DynamicItem
import top.colter.mirai.plugin.bilibili.data.DynamicMessage
import top.colter.mirai.plugin.bilibili.data.DynamicType
import top.colter.mirai.plugin.bilibili.draw.makeDrawDynamic
import top.colter.mirai.plugin.bilibili.draw.makeRGB
import top.colter.mirai.plugin.bilibili.utils.formatTime
import top.colter.mirai.plugin.bilibili.utils.time
import top.colter.mirai.plugin.bilibili.utils.uid

object DynamicMessageTasker : BiliTasker() {

    override val interval: Int = 0

    private val dynamicChannel by BiliBiliDynamic::dynamicChannel
    private val messageChannel by BiliBiliDynamic::messageChannel

    private val dynamic by BiliData::dynamic

    override suspend fun main() {
        val dynamicDetail = dynamicChannel.receive()
        val dynamicItem = dynamicDetail.item
        logger.debug(dynamicItem.idStr)
        messageChannel.send(dynamicItem.buildMessage(dynamicDetail.contact))
    }

    suspend fun DynamicItem.buildMessage(contact: String? = null): DynamicMessage {
        return DynamicMessage(
            idStr,
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
            DynamicType.DYNAMIC_TYPE_FORWARD -> {
                "${modules.moduleDynamic.desc?.text}\n\n 转发动态:\n${orig?.textContent()}"
            }
            DynamicType.DYNAMIC_TYPE_WORD,
            DynamicType.DYNAMIC_TYPE_DRAW -> {
                modules.moduleDynamic.desc?.text ?: ""
            }
            DynamicType.DYNAMIC_TYPE_ARTICLE -> {
                modules.moduleDynamic.major?.article?.title!!
            }
            DynamicType.DYNAMIC_TYPE_AV -> {
                modules.moduleDynamic.major?.archive?.title!!
            }
            DynamicType.DYNAMIC_TYPE_MUSIC -> {
                modules.moduleDynamic.major?.music?.title!!
            }
            DynamicType.DYNAMIC_TYPE_PGC -> {
                modules.moduleDynamic.major?.pgc?.title!!
            }
            DynamicType.DYNAMIC_TYPE_COMMON_SQUARE -> {
                modules.moduleDynamic.major?.common?.title!!
            }
            DynamicType.DYNAMIC_TYPE_LIVE -> {
                modules.moduleDynamic.major?.live?.title!!
            }
            DynamicType.DYNAMIC_TYPE_LIVE_RCMD -> {
                modules.moduleDynamic.major?.liveRcmd?.content!!
            }
            else -> {
                ""
            }
        }

    }

    fun DynamicItem.dynamicImages(): List<String>? {

        return when (type) {
            DynamicType.DYNAMIC_TYPE_FORWARD -> {
                orig?.dynamicImages()!!
            }
            DynamicType.DYNAMIC_TYPE_DRAW -> {
                modules.moduleDynamic.major?.draw?.items?.map { it.src }
            }
            DynamicType.DYNAMIC_TYPE_ARTICLE -> {
                modules.moduleDynamic.major?.article?.covers
            }
            DynamicType.DYNAMIC_TYPE_AV -> {
                listOf(modules.moduleDynamic.major?.archive?.cover!!)
            }
            DynamicType.DYNAMIC_TYPE_MUSIC -> {
                listOf(modules.moduleDynamic.major?.music?.cover!!)
            }
            DynamicType.DYNAMIC_TYPE_PGC -> {
                listOf(modules.moduleDynamic.major?.pgc?.cover!!)
            }
            DynamicType.DYNAMIC_TYPE_COMMON_SQUARE -> {
                listOf(modules.moduleDynamic.major?.common?.cover!!)
            }
            DynamicType.DYNAMIC_TYPE_LIVE -> {
                listOf(modules.moduleDynamic.major?.live?.cover!!)
            }
            DynamicType.DYNAMIC_TYPE_LIVE_RCMD -> {
                modules.moduleDynamic.major?.liveRcmd
                listOf()
            }
            else -> {
                listOf()
            }
        }

    }

    fun DynamicItem.dynamicLinks(): List<DynamicMessage.Link> {

        return listOf(
            DynamicMessage.Link(
                "",
                "https://t.bilibili.com/$idStr"
            )
        )

    }

    suspend fun DynamicItem.makeDynamic(): String? {
        val drawEnable = true

        val color = dynamic[uid]?.color?:BiliConfig.imageConfig.defaultColor
        val colors = color.split(";", "；").map { Color.makeRGB(it.trim()) }

        return if (drawEnable) makeDrawDynamic(colors) else null
    }

}