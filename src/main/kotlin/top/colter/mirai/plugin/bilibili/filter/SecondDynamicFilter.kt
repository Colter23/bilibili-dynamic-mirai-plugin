package top.colter.mirai.plugin.bilibili.filter

import top.colter.bilibili.data.dynamic.BiliDynamic
import top.colter.mirai.plugin.bilibili.data.Receiver
import top.colter.mirai.plugin.bilibili.data.SenderType
import top.colter.mirai.plugin.bilibili.database.BiliSubscribe
import top.colter.mirai.plugin.bilibili.database.SubscribeConfig
import top.colter.mirai.plugin.bilibili.database.filter
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * 次级过滤器，用来过滤用户
 */
sealed interface SecondDynamicFilter {
    val priority: Int

//    fun filter(dynamic: Pair<BiliDynamic, Set<String>>): Pair<BiliDynamic, Set<String>>
    fun filter(dynamic: BiliDynamic, receiver: Set<Receiver>): Set<Receiver>
}


/**
 * 内容过滤
 */
object ContentFilter: SecondDynamicFilter {
    override val priority: Int get() = 1

    override fun filter(dynamic: BiliDynamic, receiver: Set<Receiver>): Set<Receiver> {
        val map = mutableMapOf<Receiver, SubscribeConfig?>()
        BiliSubscribe.subscribe.forEach { (receiver, data) ->
            data.sender.forEach { (sender, config) ->
                if (sender.type == SenderType.User && sender.id == dynamic.mid.toString()) {
                    map[receiver] = (config ?: data.globalConfig)
                }
            }
        }

        return map.mapNotNull { (receiver, config) ->
            if (config == null) {
                receiver
            } else if (config.filter.filter(dynamic)) {
                receiver
            } else null
        }.toSet()
        //  to ReceiverConfigTemp(atAll = config.atAllFilter.filter(dynamic))
//        return receiver
    }
}




//object ContentFilter: SecondDynamicFilter {
//    override val priority: Int get() = 10
//
//    override fun filter(dynamic: BiliDynamic, receiver: Set<Receiver>): Set<Receiver> {
//        return receiver
//    }
//}


