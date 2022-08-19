package top.colter.mirai.plugin.bilibili.command

import net.mamoe.mirai.contact.Contact
import top.colter.mirai.plugin.bilibili.Group
import top.colter.mirai.plugin.bilibili.utils.delegate
import top.colter.mirai.plugin.bilibili.utils.name

data class GroupOrContact(
    val contact: Contact? = null,
    val group: Group? = null,
)

val GroupOrContact.isGroup: Boolean
    get() = group != null

val GroupOrContact.subject: String
    get() = group?.name ?: contact!!.delegate

val GroupOrContact.name: String
    get() = group?.name ?: contact!!.name
