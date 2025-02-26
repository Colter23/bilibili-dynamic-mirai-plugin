package top.colter.mirai.plugin.bilibili.database

import net.mamoe.mirai.console.data.AbstractPluginData
import net.mamoe.mirai.console.data.PluginConfig
import net.mamoe.mirai.console.util.ConsoleExperimentalApi


/**
 * 可读可写配置
 * 不会自动保存，需要手动调用 save()
 */
@OptIn(ConsoleExperimentalApi::class)
open class ReadWritePluginConfig constructor(
    override val saveName: String
) : AbstractPluginData(), PluginConfig
