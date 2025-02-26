package top.colter.mirai.plugin.bilibili.database

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import top.colter.mirai.plugin.bilibili.data.Receiver

object BlackWhiteList: AutoSavePluginData("BlackWhiteList") {
    val global: BlackWhiteConfig by value(BlackWhiteConfig())
    val contact: MutableMap<Receiver, BlackWhiteConfig> by value(mutableMapOf(
        Receiver.from("f12345678") to BlackWhiteConfig(
            biliUser = mutableListOf(45612316L, 7987546L)
        ),
        Receiver.from("cg黑名单分组") to BlackWhiteConfig(
//            mode = PluginFun.LIMIT
        ),
    ))
}

@Serializable
data class BlackWhiteConfig(
    var isBlackList: Boolean = true,
//    var mode: PluginFun = PluginFun.ALL,
    val biliUser: MutableList<Long> = mutableListOf()
)
