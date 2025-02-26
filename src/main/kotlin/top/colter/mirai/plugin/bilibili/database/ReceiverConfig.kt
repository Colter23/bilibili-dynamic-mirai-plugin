package top.colter.mirai.plugin.bilibili.database

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import top.colter.mirai.plugin.bilibili.data.Receiver
import top.colter.mirai.plugin.bilibili.database.TriggerMode.*


object ReceiverConfig: AutoSavePluginData("ReceiverConfig") {
    val receivers: MutableMap<Receiver, ReceiverConfigData> by value()
}

//fun Receiver.config(): ReceiverConfigData {
//    require(id != "0" && (type == ReceiverType.Group || type == ReceiverType.Friend )) { "只接受单个群或好友" }
//
//    ReceiverConfig.receivers[this] ?: ReceiverConfig.receivers
//
//}


/**
 * 接收者配置
 *
 * @param atAllLimit 时间段不进行@全体成员
 * @param template 推送模板
 * @param linkResolve 链接解析
 */
@Serializable
data class ReceiverConfigData(
    var enable: Boolean = true,
    var liveEnd: Boolean = false,
    var atAllLimit: TimeRange? = null,
    val template: MutableMap<PushTemplateType, String> = mutableMapOf(),
    val linkResolve: LinkResolveConfig = LinkResolveConfig()
)

data class ReceiverConfigTemp(
    val atAll: Boolean = false,
    val template: String? = null,
    val linkResolve: String? = null,

)

/**
 * 链接解析配置
 *
 * @param triggerMode 触发模式
 * @param returnLink 是否返回解析的链接
 */
@Serializable
data class LinkResolveConfig(
    val triggerMode: TriggerMode = At,
    val returnLink: Boolean = false
)

/**
 * 触发模式
 *
 * [At] @触发
 * [Always] 一直
 * [Never] 永不触发
 */
enum class TriggerMode {
    At,
    Always,
    Never
}

