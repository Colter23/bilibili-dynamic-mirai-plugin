package top.colter.mirai.plugin.bilibili.data

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import top.colter.mirai.plugin.bilibili.data.BiliPluginConfig.provideDelegate

object BiliSubscribeData : AutoSavePluginData("BiliSubscribeData") {
    @ValueDescription("订阅信息")
    val dynamic: MutableMap<Long, SubData> by value(mutableMapOf(0L to SubData("ALL")))
}