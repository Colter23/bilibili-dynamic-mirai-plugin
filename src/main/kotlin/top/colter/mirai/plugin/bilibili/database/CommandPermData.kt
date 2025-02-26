package top.colter.mirai.plugin.bilibili.database

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.contact.Contact
import top.colter.mirai.plugin.bilibili.data.Receiver


object CommandPermData: AutoSavePluginData("CommandPerm") {
    val perm: MutableList<UserPerm> by value(mutableListOf())
}


enum class PluginFun{
    ALL,
    COMMAND,
    LINK_RESOLVE,
    LIMIT
}

// /bili perm

// * 全体
// g* 所有群
// f* 所有好友
// ga* 所有群的管理
// go* 所有群的群主
// cg* 所有分组
// g123456 具体的群
// f123456 具体的好友
// ga123456 具体群的管理
// go123456 具体群的群主
// cg分组 具体的分组


// * 所有指令
// c / config  配置相关的指令
// s / search  搜索相关的指令
// f / follow  订阅相关的指令
// ... 具体的指令
enum class Fun(val list: List<String> = listOf()) {
    CONFIG(listOf("config")),
    SEARCH(listOf("searchUser", "searchVideo")),
    FOLLOW(listOf("follow", "unFollow")),
    DETAIL(mutableListOf())
}


// /bili perm s g123456     给123456群搜索指令的权限
// /bili perm * f654321 g123456  给654321好友 123456群的所有权限

data class UserPerm (
    val user: Receiver,
    val target: Receiver? = null, // 默认本身有操作自己的权限，target是可以操作其他人
    val perm: CommandPerm,
    val exclude: CommandPerm
) {
    fun hasPermission(command: String, target: Contact?): Boolean {
        return if (this.target?.contacts?.contains(target) == true) {
            perm.hasPermission(command)
        } else {
            false
        }
    }
}

class CommandPerm(
    private val command: LinkedHashSet<Fun> = LinkedHashSet()
) : MutableSet<Fun> by command {
    fun getPermCommand(): List<String> {
        return command.flatMap { it.list }.distinct()
    }

    fun hasPermission(command: String): Boolean {
        return getPermCommand().contains(command)
    }
}


fun MutableList<UserPerm>.contact(contact: Contact): List<UserPerm> {
    return filter {
        it.user.contacts.contains(contact)
    }
}

fun Contact.hasPermission(command: String, target: Contact): Boolean {
    CommandPermData.perm.contact(this).forEach {
        val has = it.hasPermission(command, if (target == this) null else target)
        if (has) return true
    }
    return false
}

