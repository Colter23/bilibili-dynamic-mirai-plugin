package top.colter.mirai.plugin.bilibili

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import top.colter.mirai.plugin.bilibili.utils.findContact
import top.colter.mirai.plugin.bilibili.utils.findContactAll
import top.colter.mirai.plugin.bilibili.utils.name
import java.time.Instant

object BiliData : AutoSavePluginData("BiliData") {
    @ValueDescription("数据版本")
    var dataVersion: Int by value(0)

    // key: uid
    @ValueDescription("订阅信息")
    val dynamic: MutableMap<Long, SubData> by value(mutableMapOf(0L to SubData("ALL")))

    // key: contact
    @ValueDescription("动态过滤")
    val filter: MutableMap<String, MutableMap<Long, DynamicFilter>> by value()

    // key: template name
    @ValueDescription("动态推送模板")
    val dynamicPushTemplate: MutableMap<String, MutableSet<String>> by value()

    // key: template name
    @ValueDescription("直播推送模板")
    val livePushTemplate: MutableMap<String, MutableSet<String>> by value()

    // key: contact
    @ValueDescription("AtAll")
    val atAll: MutableMap<String, MutableMap<Long, MutableSet<AtAllType>>> by value()

    // key: group name
    @ValueDescription("分组")
    val group: MutableMap<String, Group> by value()
}

@Serializable
data class SubData(
    val name: String,
    var color: String? = null,
    var last: Long = Instant.now().epochSecond,
    var lastLive: Long = Instant.now().epochSecond,
    val contacts: MutableSet<String> = mutableSetOf(),
    //val groups: MutableSet<String> = mutableSetOf(),
    val banList: MutableMap<String, String> = mutableMapOf(),
)

@Serializable
data class Group(
    val name: String,
    val creator: Long,
    val admin: MutableSet<Long> = mutableSetOf(),
    val contacts: MutableSet<String> = mutableSetOf(),
) {
    override fun toString(): String {
        return """
分组名: $name
创建者: ${findContactAll(creator)?.run { "$name($id)" }?:creator}

管理员: 
${admin.joinToString("\n") { findContactAll(it)?.run { "$name($id)" }?:it.toString() }.ifEmpty { "暂无管理员" }}

用户: 
${contacts.joinToString("\n") { findContact(it)?.run { "$name($id)" }?:it }.ifEmpty { "暂无用户" }}
""".trimIndent()
    }
}

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

enum class AtAllType(val value: String) {
    ALL("全部"),
    DYNAMIC("全部动态"),
    VIDEO("视频"),
    MUSIC("音乐"),
    ARTICLE("专栏"),
    LIVE("直播"),
}