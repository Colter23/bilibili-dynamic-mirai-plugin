package top.colter.mirai.plugin.bilibili.command

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.descriptor.CommandValueArgumentParser
import net.mamoe.mirai.console.command.descriptor.ExistingContactValueArgumentParser
import top.colter.mirai.plugin.bilibili.BiliData

object GroupOrContactParser: CommandValueArgumentParser<GroupOrContact> {

    override fun parse(raw: String, sender: CommandSender): GroupOrContact {
        val group = BiliData.group[raw]
        return GroupOrContact(
            if (group == null) ExistingContactValueArgumentParser.parse(raw, sender) else null,
            group
        )
    }

}

