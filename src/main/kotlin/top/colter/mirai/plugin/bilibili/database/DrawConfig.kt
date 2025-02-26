package top.colter.mirai.plugin.bilibili.database

import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value


object DrawConfig : ReadWritePluginConfig("DrawConfig") {

    @ValueDescription("""
        〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓
        绘图配置
    """)
    val help: String by value("https://github.com/Colter23/bilibili-dynamic-mirai-plugin#BiliConfig.yml")

    @ValueDescription("""
        〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓
        绘图开关 true / false
    """)
    var enable: Boolean by value(true)



}