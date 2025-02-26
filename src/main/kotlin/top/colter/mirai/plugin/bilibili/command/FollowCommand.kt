package top.colter.mirai.plugin.bilibili.command

import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext
import net.mamoe.mirai.console.command.descriptor.CommandValueArgumentParser
import top.colter.mirai.plugin.bilibili.data.Receiver
import top.colter.mirai.plugin.bilibili.database.CommandConfig
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

object FollowCommand : Command{

    @CommandMethod
    fun follow(uid: String, receiver: Receiver) {

    }

}

val prefix: String by CommandConfig::prefix


class CommandNotFound(message: String = "未找到指令"): Exception(message)
class CommandArgumentParserException(message: String = "指令参数解析失败"): IllegalArgumentException(message)


object CommandParser {

    var commandSplit: CommandSplit = SpaceCommandSplit
//    var argumentParser: List<CommandArgumentParser<Any>> = SpaceCommandSplit

    fun parser(input: String) {
        if (!isCommand(input)) return

        var arguments = commandSplit.split(input)
        val function = findCommand(arguments.first()) ?: throw CommandNotFound()
        arguments = sortedCommand(arguments.run { subList(1, size) })
        CommandArgumentContext.Builtins
    }

    private fun isCommand(input: String): Boolean {
        return input.startsWith(prefix)
    }


    private fun findCommand(argument: CommandArgument): KFunction<*>? {
        return Command.findCommand(argument.value!!)
    }

    private fun sortedCommand(arguments: List<CommandArgument>): List<CommandArgument> {
        return arguments
    }

    private fun argumentParser(arguments: List<CommandArgument>, function: KFunction<*>) {

    }

}

data class CommandArgument(
    var index: Int,
    val name: String? = null,
    val value: String? = null
)

interface CommandSplit {
    fun split(input: String): List<CommandArgument>
}

object SpaceCommandSplit: CommandSplit {
    override fun split(input: String): List<CommandArgument> {
        return input.trim().split(" ").mapIndexedNotNull { index, value ->
            if (value.isNotBlank()) {
                CommandArgument(index = index, value = value)
            } else null
        }
    }
}

class CommandArgumentContext {
    val list : MutableMap<KClass<*>, CommandValueArgumentParser<*>> = mutableMapOf()
    fun <T : Any> get(kClass: KClass<T>): CommandValueArgumentParser<T>? {
        return list[kClass] as CommandValueArgumentParser<T>
    }
    fun <T : Any> add(kClass: KClass<T>, parse: CommandValueArgumentParser<T>) {
        list[kClass] = parse
    }
}

//interface CommandArgumentParser<out T : Any> {
//    public fun parse(raw: String): T
//}
//
//object IntArgumentParser: CommandArgumentParser<Int> {
//    override fun parse(raw: String): Int {
//        raw.toIntOrNull() ?: CommandArgumentParserException("无法将")
//    }
//}