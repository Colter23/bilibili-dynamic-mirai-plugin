package top.colter.mirai.plugin.bilibili

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value


object BiliDynamicConfig : AutoSavePluginConfig("BiliPluginConfig") {


    @ValueDescription("管理员")
    val admin: String by value("")

    @ValueDescription("推送模式\n0: 文字推送\n1: 图片推送")
    val pushMode: Int by value(1)

    @ValueDescription("添加订阅时是否允许 bot 自动关注未关注的用户")
    val autoFollow: Boolean by value(true)

    @ValueDescription("Bot 关注时保存的分组(最长16字符)")
    val followGroup: String by value("Bot关注")

    @ValueDescription("检测间隔(推荐 15-30) 单位秒")
    val interval: Int by value(15)

    @ValueDescription("直播检测间隔(与动态检测独立) 单位秒")
    val liveInterval: Int by value(20)

    @ValueDescription("低频检测时间段与倍率(例: 3-8x2 三点到八点检测间隔为正常间隔的2倍) 24小时制")
    val lowSpeed: String by value("0-0x2")

    @ValueDescription("图片推送模式用的字体, 详细请看 readme")
    val font: String by value("")

    @ValueDescription("动态/视频推送文字模板, 参数请看 readme")
    val pushTemplate: String by value("{name}@{type}\n{link}")

    @ValueDescription("直播推送文字模板, 如不配置则与上面的动态推送模板一致")
    val livePushTemplate: String by value("")

    @ValueDescription("页脚模板")
    val footerTemplate: String by value("{type}ID: {id}")

    @ValueDescription("是否开启图片二维码")
    val qrCode: Boolean by value(false)

    @ValueDescription("卡片圆角大小")
    val cardArc: Int by value(20)

    //@Suppress(stringSerialization = DOUBLE_QUOTATION)
    @ValueDescription("cookie, 请使用双引号")
    var cookie: String by value("")

    @ValueDescription("百度翻译")
    val baiduTranslate: TranslateConfig by value()



    @ValueDescription("图片配置")
    val debugMode: Boolean by value(false)

    @ValueDescription("图片配置")
    val imageConfig: ImageConfig by value()

    @ValueDescription("代理")
    val proxy: List<String> by value()

    @ValueDescription("代理")
    val accountConfig: BiliAccountConfig by value()

    @ValueDescription("代理")
    val enableConfig: EnableConfig by value()

    @ValueDescription("代理")
    val checkConfig: CheckConfig by value()

    @ValueDescription("代理")
    val templateConfig: TemplateConfig by value()

}

@Serializable
data class EnableConfig(
    val enable: Boolean = true,
    val proxyEnable: Boolean = true,
)

@Serializable
data class TranslateConfig(
    val enable: Boolean = false,
    val APP_ID: String = "",
    val SECURITY_KEY: String = "",
)

@Serializable
data class ImageConfig(
    val quality: Int = 1,
    val theme: String = "",
    val font: String = "",
    val fontSizeMultiple: Float = 1.0f,
    val badgeEnable: Boolean = true,
    /**
     * 可选值:
     * fanCard 粉丝卡片
     * qrCode  动态链接二维码
     * none    无
     */
    val cardOrnament: String = "fanCard"
)

@Serializable
data class ProxyConfig(
    val proxy: List<String> = listOf(),
//    val verifyUrl: String = "http://httpbin.org",
)

@Serializable
data class BiliAccountConfig(
    var cookie: String = "",
    val autoFollow: Boolean = true,
    val followGroup: String = "Bot关注"
)

@Serializable
data class CheckConfig(
    val interval: Int = 15,
)


@Serializable
data class PushConfig(
    val quality: Int = 1,
)

@Serializable
data class TemplateConfig(
    val defaultDynamicTemplate: String = "defaultOneMsg",
    val dynamic: MutableMap<String, String> = mutableMapOf(
        "drawOnly" to "{draw}",
        "defaultOneMsg" to "{draw}\n{name}@{type}\n{link}",
        "defaultTwoMsg" to "{draw}\r{name}@{uid}@{type}\n{time}\n{link}",
        "defaultForwardMsg" to "{draw}{>>}作者：{name}\nUID：{uid}\n时间：{time}\n类型：{type}\n链接：{link}\r{content}\r{images}{<<}",
        //"defaultForwardMsg" to "【{name}】{type}\r{draw}\n{link} {>>}作者：{name}\nUID：{uid}\n时间：{time}\n类型：{type}\n链接：{link}\r{content}\r{images}{<<}",
    ),
    val live: String = "",
    val forwardCard: ForwardDisplay = ForwardDisplay()
)

@Serializable
data class ForwardDisplay(
    val title: String = "{name} {type} 详情",
    val summary: String = "ID: {did}",
    val brief: String = "[{name} {type}]",
    val preview: String = "时间: {time}\n{content}"
)

@Serializable
data class CacheConfig(
    val dynamicDraw: Int = 1,
)

