package top.colter.mirai.plugin.bilibili.database

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import org.jetbrains.skia.paragraph.Alignment


object PushTemplate : ReadWritePluginConfig("PushTemplate") {

    // 不会被 ValueDescription 吃掉的空白符 括号内 -> (ㅤ)
    @ValueDescription("""
        〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓
        推送模板配置
        所有推送模板都可自行添加新模板 模板名可随意取
    """)
    val help: String by value("https://github.com/Colter23/bilibili-dynamic-mirai-plugin#BiliConfig.yml")

    @ValueDescription("""
        〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓
        默认动态推送模板
    """)
    var dynamicDefault: String by value("OneMsg")

    @ValueDescription("""
        动态推送模板  可用配置项: 
        ㅤㅤ{name}: 用户名
        ㅤㅤ{uid}: 用户ID
        ㅤㅤ{did}: 动态ID
        ㅤㅤ{type}: 动态类型
        ㅤㅤ{time}: 时间
        ㅤㅤ{content}: 动态内容
        ㅤㅤ{draw}: 绘制的动态图
        ㅤㅤ{images}: 动态内的原图
        ㅤㅤ{link}: 动态链接
        ㅤㅤ{links}: 动态链接
        ㅤㅤ{>>} {<<}: 包装成转发动态
        ㅤㅤ{n}: 换行
        ㅤㅤ{r}: 分割对话
    """)
    val dynamic: MutableMap<String, String> by value(mutableMapOf(
        "DrawOnly" to "{draw}",
        "TextOnly" to "{name}@{type}{n}{link}{n}{content}{n}{images}",
        "OneMsg" to "{draw}{n}{name}@{type}{n}{link}",
        "TwoMsg" to "{draw}{r}{name}@{uid}@{type}{n}{time}{n}{link}",
        "ForwardMsg" to "{draw}{>>}作者：{name}{n}UID：{uid}{n}时间：{time}{n}类型：{type}{n}链接：{links}{r}{content}{r}{images}{<<}",
    ))


    @ValueDescription("""
        〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓
        直播默认推送模板
    """)
    var liveDefault: String by value("OneMsg")
    @ValueDescription("""
        直播推送模板  可用配置项: 
        ㅤㅤ{draw}: 绘制的直播图
        ㅤㅤ{name}: 名称
        ㅤㅤ{uid}: 用户ID
        ㅤㅤ{rid}: 房间号
        ㅤㅤ{time}: 直播开始时间
        ㅤㅤ{title}: 直播标题
        ㅤㅤ{area}: 直播分区
        ㅤㅤ{cover}: 直播封面
        ㅤㅤ{link}: 直播链接
        ㅤㅤ{n}: 换行
        ㅤㅤ{r}: 分割对话
    """)
    val live: MutableMap<String, String> by value(mutableMapOf(
        "DrawOnly" to "{draw}",
        "TextOnly" to "{name}@直播{n}{link}{n}标题: {title}",
        "OneMsg" to "{draw}{n}{name}@直播{n}{link}",
        "TwoMsg" to "{draw}{r}{name}@{uid}@直播{n}{title}{n}{time}{n}{link}",
    ))


    @ValueDescription("""
        〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓
        默认推送模板
    """)
    var defaultLiveEnd: String by value("SimpleMsg")
    @ValueDescription("""
        直播结束推送模板  可用配置项: 
        ㅤㅤ{name}: 名称
        ㅤㅤ{uid}: 用户ID
        ㅤㅤ{rid}: 房间号
        ㅤㅤ{title}: 直播标题
        ㅤㅤ{area}: 直播分区
        ㅤㅤ{startTime}: 直播开始时间
        ㅤㅤ{endTime}: 直播结束时间
        ㅤㅤ{duration}: 直播时长
        ㅤㅤ{link}: 直播链接
    """)
    val liveEnd: MutableMap<String, String> by value(mutableMapOf(
        "SimpleMsg" to "{name} 直播结束啦!{n}直播时长: {duration}",
        "ComplexMsg" to "{name} 直播结束啦!{n}标题: {title}{n}直播时长: {duration}"
    ))
    @ValueDescription("""
        〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓
        转发动态中的原动态模板
    """)
    var originDynamic: String by value("{name} ")
    @ValueDescription("""
        〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓
        转发卡片
    """)
    val forwardCard: ForwardDisplay by value(ForwardDisplay())
    @ValueDescription("""
        〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓〓
        页脚配置
    """)
    var footer: FooterConfig by value(FooterConfig())

}

@Serializable
data class FooterConfig(
    var dynamicFooter: String = "",
    var liveFooter: String = "",
    var footerAlign: Alignment = Alignment.LEFT
)

@Serializable
data class ForwardDisplay(
    val title: String = "{name} {type} 详情",
    val preview: String = "时间: {time}\n{content}",
    val summary: String = "ID: {did}",
    val brief: String = "[{name} {type}]"
)
