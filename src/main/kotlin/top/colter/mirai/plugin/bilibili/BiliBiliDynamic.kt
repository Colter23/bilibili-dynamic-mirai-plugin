package top.colter.mirai.plugin.bilibili

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info

object BiliBiliDynamic : KotlinPlugin(
    JvmPluginDescription(
        id = "top.colter.bilibili-dynamic",
        name = "BiliBili Dynamic",
        version = "0.1.0",
    ) {
        author("Colter")
    }
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }

        BiliDynamicConfig.reload()

    }
}