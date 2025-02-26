package top.colter.mirai.plugin.bilibili.database

import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value


object CommandConfig : ReadWritePluginConfig("CommandConfig") {

    @ValueDescription("""
        〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓
        指令前缀 可随意填写 不建议为空 要简化输入可以用下面的指令别名
    """)
    val prefix: String by value("/bili")

    @ValueDescription("""
        〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓
        指令名 在前缀的基础上
    """)
    val command: Map<String, List<String>> by value()

    @ValueDescription("""
        〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓
        指令别名 这个别名前面不需要有前缀
    """)
    val alias: Map<String, List<String>> by value()
}