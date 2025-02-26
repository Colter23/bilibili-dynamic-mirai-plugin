package top.colter.mirai.plugin.bilibili.database

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import top.colter.bilibili.data.season.Season

object BiliSeason: AutoSavePluginData("BiliSeason")  {
    val season: MutableList<Season> by value()
}

