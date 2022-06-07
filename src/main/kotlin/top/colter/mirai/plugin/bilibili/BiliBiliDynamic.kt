package top.colter.mirai.plugin.bilibili

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.utils.info
import top.colter.mirai.plugin.bilibili.data.BiliCookie
import top.colter.mirai.plugin.bilibili.data.DynamicDetail
import top.colter.mirai.plugin.bilibili.data.DynamicMessage
import top.colter.mirai.plugin.bilibili.tasker.*

object BiliBiliDynamic : KotlinPlugin(
    JvmPluginDescription(
        id = "top.colter.bilibili-dynamic-mirai-plugin",
        name = "BiliBili Dynamic",
        version = "3.0.0-M1",
    ) {
        author("Colter")
    }
) {

    var mid: Long = 0L
    var tagid: Int = 0

    var cookie = BiliCookie()

    val dynamicChannel = Channel<DynamicDetail>(20)
    val messageChannel = Channel<DynamicMessage>(20)

    val subDynamic: MutableMap<Long, SubData> by BiliDynamicData::dynamic

    override fun onEnable() {
        logger.info { "Plugin loaded" }

        BiliDynamicData.reload()
        BiliDynamicConfig.reload()

        cookie.parse(BiliDynamicConfig.accountConfig.cookie)

        launch {
            checkCookie()
            initTagid()
        }

        waitOnline {
            DynamicCheckTasker.start()
            MessageTasker.start()
            SendTasker.start()
            ListenerTasker.start()
            CacheClearTasker.start()
        }

    }

    override fun onDisable() {
        dynamicChannel.close()
        messageChannel.close()

        BiliTasker.cancelAll()

        BiliDynamicData.save()
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