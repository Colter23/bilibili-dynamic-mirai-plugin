package top.colter.mirai.plugin.bilibili.database

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import top.colter.bilibili.data.dynamic.BiliDynamic
import top.colter.bilibili.data.dynamic.type.BiliDynamicType
import top.colter.bilibili.data.dynamic.type.BiliDynamicType.*
import top.colter.mirai.plugin.bilibili.data.Receiver
import top.colter.mirai.plugin.bilibili.data.Sender
import top.colter.mirai.plugin.bilibili.lisener.textContent


object BiliSubscribe: AutoSavePluginData("BiliSubscribe") {
    val subscribe: MutableMap<Receiver, SubscribeData> by value(mutableMapOf(
        Receiver.from("f12345678") to SubscribeData(
//            globalConfig = SubscribeConfig(
////                template = mutableMapOf(BiliDynamicType.ALL_DYNAMIC to "template1")
//            ),
//            sender = mutableMapOf(
//                Sender.from("s654313232") to null,
//                Sender.from("u987456321") to SubscribeConfig(
//                    filter = mutableListOf(FilterChain.form("TYPE=VIDEO AND V_TYPE=REPLAY OR V_TYPE=COOPERATE AND REGEX=正则 END"))
//                )
//            )
        )
    ))
}


@Serializable
data class SubscribeData(
    val globalConfig: SubscribeConfig? = null,
    val sender: MutableMap<Sender, SubscribeConfig?> = mutableMapOf()
)



//@Serializable
//data class GlobalConfig(
//    val linkResolve: Int = 0,
////    var atall: Boolean = false,
////    var atallLimit: String = "",
//    val subscribeConfig: SubscribeConfig? = null
//)

@Serializable
data class SubscribeConfig(
//    var atall: Boolean = false,
//    var liveClose: Boolean = false,
//    val type: Set<SubscribeType>,
//    val filter: MutableList<FilterChain> = mutableListOf(),
//    val atAllFilter: MutableList<FilterChain> = mutableListOf(),
    val filter: Filter = Filter(),
    val atAllFilter: Filter = Filter(),
)

//data class Filter(
//
//)

@Serializable
enum class FilterType {
    TYPE,
    REGULAR
}

@Serializable
data class Filter(
    val typeFilter: TypeFilter = TypeFilter(),
    val regularFilter: RegularFilter = RegularFilter(),
)

@Serializable
data class TypeFilter(
    var mode: FilterMode = FilterMode.BLACK_LIST,
    val list: MutableList<DynamicFilterType> = mutableListOf()
)

@Serializable
data class RegularFilter(
    var mode: FilterMode = FilterMode.BLACK_LIST,
    val list: MutableList<String> = mutableListOf()
)

fun Filter.filter(dynamic: BiliDynamic): Boolean {
    if (!typeFilter.filter(dynamic)) return false
    if (!regularFilter.filter(dynamic)) return false
    return true
}

fun TypeFilter.filter(dynamic: BiliDynamic): Boolean {
    if (list.isNotEmpty()) {
        val b = list.contains(dynamic.type.toFilterType())
        when (mode) {
            FilterMode.WHITE_LIST -> if (!b) return false
            FilterMode.BLACK_LIST -> if (b) return false
        }
    }
    return true
}

fun RegularFilter.filter(dynamic: BiliDynamic): Boolean {
    if (list.isNotEmpty()) {
        list.forEach {
            val b = Regex(it).containsMatchIn(dynamic.textContent())
            when (mode) {
                FilterMode.WHITE_LIST -> if (!b) return@filter false
                FilterMode.BLACK_LIST -> if (b) return@filter false
            }
        }
    }
    return true
}

@Serializable
enum class FilterMode(val value: String) {
    WHITE_LIST("白名单"),
    BLACK_LIST("黑名单")
}

@Serializable
enum class DynamicFilterType(val value: String) {
    ALL_DYNAMIC("全部动态"),
    DYNAMIC("动态"),
    FORWARD("转发动态"),
    VIDEO("视频"),
    MUSIC("音乐"),
    ARTICLE("专栏"),
    PGC("番剧"),
    LIVE("直播"),
}

fun BiliDynamicType.toFilterType() =
    when (this) {
        FORWARD,
        VIDEO,
        ARTICLE,
        MUSIC,
        PGC,
        LIVE -> DynamicFilterType.valueOf(this.name)
        else -> DynamicFilterType.DYNAMIC
    }

/**
 * 订阅类型 未来可能会扩展
 */
//enum class SubscribeType {
//    DYNAMIC,
//    LIVE
//}

//@Serializable
//data class AtConfig(
////    var timeRange: TimeRange? = null,
//    // TODO(用过滤器)
//    val filter: String
//)