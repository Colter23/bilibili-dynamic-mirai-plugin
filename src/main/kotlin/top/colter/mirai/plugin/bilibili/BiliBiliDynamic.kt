package top.colter.mirai.plugin.bilibili

import kotlinx.coroutines.channels.Channel
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.utils.info
import top.colter.mirai.plugin.bilibili.data.DynamicItem
import top.colter.mirai.plugin.bilibili.data.DynamicMessage
import top.colter.mirai.plugin.bilibili.tasker.BiliTasker
import top.colter.mirai.plugin.bilibili.tasker.DynamicCheckTasker
import top.colter.mirai.plugin.bilibili.tasker.MessageTasker
import top.colter.mirai.plugin.bilibili.tasker.SendTasker

object BiliBiliDynamic : KotlinPlugin(
    JvmPluginDescription(
        id = "top.colter.bilibili-dynamic",
        name = "BiliBili Dynamic",
        version = "3.0.0-M1",
    ) {
        author("Colter")
    }
) {

    val dynamicChannel = Channel<DynamicItem>(20)
    val messageChannel = Channel<DynamicMessage>(20)

    val subDynamic: MutableMap<Long, SubData> by BiliSubscribeData::dynamic

    override fun onEnable() {
        logger.info { "Plugin loaded" }

        BiliSubscribeData.reload()
        BiliDynamicConfig.reload()

        this.dataFolderPath

        waitOnline {
            DynamicCheckTasker.start()
            MessageTasker.start()
            SendTasker.start()
        }

    }

    override fun onDisable() {
        BiliTasker.cancelAll()

        BiliSubscribeData.save()
        BiliDynamicConfig.save()
    }

    /**
     * author cssxsh
     */
    private fun waitOnline(block: () -> Unit) {
        if (Bot.instances.isEmpty()) {
            globalEventChannel().subscribeOnce<BotOnlineEvent> { block() }
        } else {
            block()
        }
    }
}