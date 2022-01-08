package top.colter.mirai.plugin.bilibili

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
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
        version = "2.0.9"
    ) {
        author("Colter")
        info(
            """
            把B站订阅者的动态转发到QQ
        """.trimIndent()
        )
    }
) {

    val contactMap: MutableMap<Long, Contact> = mutableMapOf()

    var sessData: String = ""
    var biliJct: String = ""

    var tagid: Int = 0

    override fun onEnable() {
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
    }
}
