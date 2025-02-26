package top.colter.mirai.plugin.bilibili.database

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import top.colter.mirai.plugin.bilibili.data.Receiver

object ContactGroup: AutoSavePluginData("ContactGroup") {
    val group: MutableList<Group> by value(mutableListOf(
        Group(
            name = "黑名单分组",
            creator = 123465789,
            createTime = 1663297302,
            admins = mutableSetOf(4561321361),
            contacts = mutableSetOf(
                Receiver.from("g84541313"),
                Receiver.from("f65945413")
            )
        )
    ))
}

@Serializable
data class Group(
    val name: String,
    val creator: Long,
    val createTime: Long,
    val admins: MutableSet<Long> = mutableSetOf(),
    val contacts: MutableSet<Receiver> = mutableSetOf(), // TODO("限制类型，不能为分组cg (委托?)")
)

internal operator fun Collection<Group>.get(name: String) = find { it.name == name }

