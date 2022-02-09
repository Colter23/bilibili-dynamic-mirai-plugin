package top.colter.mirai.plugin.bilibili

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact
import top.colter.mirai.plugin.bilibili.command.DynamicCommand
import top.colter.mirai.plugin.bilibili.data.BiliPluginConfig
import top.colter.mirai.plugin.bilibili.data.BiliSubscribeData

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "top.colter.bilibili-dynamic-mirai-plugin",
        name = "BilibiliDynamic",
        version = "2.1.5"
    ) {
        author("Colter")
        info(
            """
            低延迟检测B站动态/直播的mirai-console插件
        """.trimIndent()
        )
    }
) {

    val contactMap: MutableMap<Long, Contact> = mutableMapOf()

    var sessData: String = ""
    var biliJct: String = ""
    var mid: Long = 0L

    var tagid: Int = 0

    val gwp = PermissionId(PluginMain.description.id,"live.atall")

    override fun onEnable() {
        PermissionService.INSTANCE.register(gwp,"直播At全体")

        BiliSubscribeData.reload()
        BiliPluginConfig.reload()
        DynamicCommand.register()

        initCookie()
        initTagid()

        Listener.subscribe()
        DynamicTasker.start()
    }

    override fun onDisable() {
        DynamicCommand.unregister()
        DynamicTasker.stop()
        Listener.stop()
        BiliSubscribeData.save()
        BiliPluginConfig.save()
    }
}
