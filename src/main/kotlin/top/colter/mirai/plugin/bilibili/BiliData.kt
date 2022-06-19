package top.colter.mirai.plugin.bilibili

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import java.time.Instant

object BiliData : AutoSavePluginData("BiliData") {
    // key: uid
    @ValueDescription("订阅信息")
    val dynamic: MutableMap<Long, SubData> by value(mutableMapOf(0L to SubData("ALL")))

    // key: contact
    @ValueDescription("动态过滤")
    val filter: MutableMap<String, MutableMap<Long, DynamicFilter>> by value()

    // key: template name
    @ValueDescription("动态推送模板")
    val dynamicPushTemplate: MutableMap<String, MutableSet<Long>> by value()

    // key: template name
    @ValueDescription("直播推送模板")
    val livePushTemplate: MutableMap<String, MutableSet<Long>> by value()
}

@Serializable
data class SubData(
    val name: String,
    var color: String? = null,
    var last: Long = Instant.now().epochSecond,
    var lastLive: Long = Instant.now().epochSecond,
    val contacts: MutableList<String> = mutableListOf(),
    val banList: MutableMap<String, String> = mutableMapOf(),
)

@Serializable
enum class FilterType {
    TYPE,
    REGULAR
}

@Serializable
data class DynamicFilter(
    val typeSelect: TypeFilter = TypeFilter(),
    val regularSelect: RegularFilter = RegularFilter(),
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

@Serializable
enum class FilterMode(val value: String) {
    WHITE_LIST("白名单"),
    BLACK_LIST("黑名单")
}

@Serializable
enum class DynamicFilterType(val value: String) {
    DYNAMIC("动态"),
    FORWARD("转发动态"),
    VIDEO("视频"),
    MUSIC("音乐"),
    ARTICLE("专栏"),
    LIVE("直播"),
}