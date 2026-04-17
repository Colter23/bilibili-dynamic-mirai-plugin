package top.colter.mirai.plugin.bilibili

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.value

object NapcatConfig : ReadOnlyPluginConfig("NapcatConfig") {

    val enable: Boolean by value(false)

    val napcat: List<NapcatHttpConfig> by value(listOf(NapcatHttpConfig()))

}

@Serializable
data class NapcatHttpConfig(
    val url: String = "",
    val token: String = "",
    val qq: Long = 0L
)
