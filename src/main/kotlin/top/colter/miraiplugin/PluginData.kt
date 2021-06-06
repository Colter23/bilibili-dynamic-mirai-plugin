package top.colter.miraiplugin

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import top.colter.miraiplugin.bean.User

object PluginData : AutoSavePluginData("pluginData"){
    // 运行路径 在初始化时赋值
    var runPath by value("./")

    var followList : MutableList<String> by value()

    var groupList : MutableMap<Long,MutableList<String>> by value()
    var friendList : MutableMap<Long,MutableList<String>> by value()

    var followMemberGroup : MutableMap<String,MutableList<Long>> by value()

    var userData : MutableList<User> by value()
}