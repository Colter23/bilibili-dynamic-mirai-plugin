package top.colter.mirai.plugin.bilibili.command

import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext
import net.mamoe.mirai.console.command.descriptor.EmptyCommandArgumentContext
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation

interface Command {
    fun register() {
        commands.add(this)
    }

    companion object{
        val commands: MutableList<Command> = mutableListOf()

        private val commandMap: Map<String, KFunction<*>>
            get() {
                val map = mutableMapOf<String, KFunction<*>>()
                commands.forEach { command ->
                    command::class.functions.filter { it.hasAnnotation<CommandMethod>() }.forEach {
                        map[it.name] = it
                    }
                }
                return map
            }

        fun findCommand(name: String): KFunction<*>? {
            return commandMap[name]
        }
    }
}

abstract class AbstractCommand(
    overrideContext: CommandArgumentContext = EmptyCommandArgumentContext
) : Command {

}