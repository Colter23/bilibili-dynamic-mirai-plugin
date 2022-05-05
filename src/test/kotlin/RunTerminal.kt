package top.colter

import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import java.io.File
import java.util.Properties

suspend fun main() {
    MiraiConsoleTerminalLoader.startAsDaemon()

    val pluginInstance = BiliBiliDynamic

    pluginInstance.load() // 主动加载插件, Console 会调用 BiliBiliDynamic.onLoad
    pluginInstance.enable() // 主动启用插件, Console 会调用 BiliBiliDynamic.onEnable

    val properties = Properties().apply { File("account.properties").inputStream().use { load(it) } }

    val bot = MiraiConsole.addBot(properties.getProperty("id").toLong(), properties.getProperty("password"))
        .alsoLogin() // 登录一个测试环境的 Bot

    MiraiConsole.job.join()
}
