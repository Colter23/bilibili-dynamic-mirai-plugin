package top.colter.mirai.plugin.bilibili.tasker

import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.data.DynamicItem
import top.colter.mirai.plugin.bilibili.data.DynamicMessage
import top.colter.mirai.plugin.bilibili.data.DynamicType
import top.colter.mirai.plugin.bilibili.draw.makeDrawDynamic
import top.colter.mirai.plugin.bilibili.utils.formatTime
import top.colter.mirai.plugin.bilibili.utils.time

object MessageTasker: BiliTasker() {

    override val interval: Int = 0

    override suspend fun main() {
        val dynamicItem = BiliBiliDynamic.dynamicChannel.receive()
        BiliBiliDynamic.messageChannel.send(dynamicItem.buildMessage())
    }

    suspend fun DynamicItem.buildMessage(): DynamicMessage {
        return DynamicMessage(
            idStr,
            modules.moduleAuthor.mid,
            modules.moduleAuthor.name,
            type.text,
            formatTime,
            time.toInt(),
            textContent(),
            dynamicImages(),
            dynamicLinks(),
            makeDynamic()
        )
    }

    fun DynamicItem.textContent(): String{
        return when (type){
            DynamicType.DYNAMIC_TYPE_FORWARD -> {
                orig?.textContent()!!
            }
            DynamicType.DYNAMIC_TYPE_WORD,
            DynamicType.DYNAMIC_TYPE_DRAW -> {
                modules.moduleDynamic.desc?.text?: ""
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

    fun DynamicItem.dynamicImages(): List<String>?{

        return when (type){
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

    fun DynamicItem.dynamicLinks(): List<DynamicMessage.Link>{

        return listOf(
            DynamicMessage.Link(
            "",
            ""
        ))

    }

    suspend fun DynamicItem.makeDynamic(): String?{
        val drawEnable = true
        return if (drawEnable) makeDrawDynamic() else null
    }

}