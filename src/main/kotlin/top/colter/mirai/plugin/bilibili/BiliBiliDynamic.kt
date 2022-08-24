package top.colter.mirai.plugin.bilibili

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.plugin.name
import net.mamoe.mirai.console.plugin.version
import net.mamoe.mirai.console.util.SemVersion
import net.mamoe.mirai.utils.info
import top.colter.mirai.plugin.bilibili.command.DynamicCommand
import top.colter.mirai.plugin.bilibili.data.*
import top.colter.mirai.plugin.bilibili.old.migration
import top.colter.mirai.plugin.bilibili.old.updateData
import top.colter.mirai.plugin.bilibili.tasker.*

object BiliBiliDynamic : KotlinPlugin(
    JvmPluginDescription(
        id = "top.colter.bilibili-dynamic-mirai-plugin",
        name = "BiliBili Dynamic",
        version = "3.1.0",
    ) {
        author("Colter")
        dependsOn("xyz.cssxsh.mirai.plugin.mirai-skia-plugin", ">= 1.1.0")
    }
) {

    var uid: Long = 0L
    var tagid: Int = 0

    var cookie = BiliCookie()

    val dynamicChannel = Channel<DynamicDetail>(20)
    val liveChannel = Channel<LiveDetail>(20)
    val messageChannel = Channel<BiliMessage>(20)
    val missChannel = Channel<BiliMessage>(10)

    val liveGwp = PermissionId(BiliBiliDynamic.description.id, "live.atall")
    val videoGwp = PermissionId(BiliBiliDynamic.description.id, "video.atall")
    val crossContact = PermissionId(BiliBiliDynamic.description.id, "crossContact")

    override fun PluginComponentStorage.onLoad() {
        /**
         * run after auto login
         * @author cssxsh
         */
        runAfterStartup {
            updateData()

            DynamicCheckTasker.start()
            LiveCheckTasker.start()
            DynamicMessageTasker.start()
            LiveMessageTasker.start()
            SendTasker.start()
            ListenerTasker.start()
            if (BiliConfig.enableConfig.cacheClearEnable) CacheClearTasker.start()
        }
    }

    override fun onEnable() {
        // XXX: mirai console version check
        check(SemVersion.parseRangeRequirement(">= 2.12.0-RC").test(MiraiConsole.version)) {
            "$name $version 需要 Mirai-Console 版本 >= 2.12.0，目前版本是 ${MiraiConsole.version}"
        }
        logger.info { "BiliBili Dynamic Plugin loaded" }

        PermissionService.INSTANCE.register(liveGwp, "直播At全体")
        PermissionService.INSTANCE.register(videoGwp, "视频At全体")
        PermissionService.INSTANCE.register(crossContact, "跨聊天语境控制")

        DynamicCommand.register()

        BiliData.reload()
        BiliConfig.reload()
        BiliImageTheme.reload()
        BiliImageQuality.reload()

        migration()

        launch { initData() }
    }

    override fun onDisable() {
        DynamicCommand.unregister()
        dynamicChannel.close()
        messageChannel.close()

        BiliTasker.cancelAll()

        BiliData.save()
        BiliConfig.save()
    }
}