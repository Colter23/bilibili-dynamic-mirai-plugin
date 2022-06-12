package top.colter.mirai.plugin.bilibili

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import top.colter.mirai.plugin.bilibili.utils.CacheType


object BiliConfig : AutoSavePluginConfig("BiliPluginConfig") {

    @ValueDescription("具体的配置文件描述请前往下方链接查看")
    val help: String by value("https://github.com/Colter23/bilibili-dynamic-mirai-plugin")

    @ValueDescription("管理员QQ号")
    val admin: String by value("")

    @ValueDescription("Debug模式")
    val debugMode: Boolean by value(false)

    @ValueDescription("功能开关:\n")
    val enableConfig: EnableConfig by value()

    @ValueDescription("账号配置:\ncookie: BiliBili的cookie, 可使用 /bili login 自动获取\nautoFollow: 添加订阅时是否允许 bot 自动关注未关注的用户\nfollowGroup: Bot 关注时保存的分组(最长16字符)")
    val accountConfig: BiliAccountConfig by value()

    @ValueDescription("检测配置:\ninterval: 动态检测间隔(推荐 15-30) 单位秒\nliveInterval: 直播检测间隔(与动态检测独立) 单位秒\nlowSpeed: 低频检测时间段与倍率(例: 3-8x2 三点到八点检测间隔为正常间隔的2倍) 24小时制")
    val checkConfig: CheckConfig by value()

    @ValueDescription("图片配置:\nquality: 图片质量(分辨率), 内置 800w: 800px, 1000w: 1000px, 1200w: 1200px, 1500w: 1500px(图片宽度)\ntheme: 绘图主题, 内置 v3: 新版绘图主题, v2: 旧版绘图主题")
    val imageConfig: ImageConfig by value()

    @ValueDescription("模板配置:\ndefaultDynamicPush: 默认使用的推送模板, 填写下方动态模板名\ndynamicPush: 动态推送模板\nlivePush: 直播推送模板\nforwardCard: 转发卡片模板\nfooter: 图片页脚")
    val templateConfig: TemplateConfig by value()

    @ValueDescription("缓存配置:\nexpires: 图片过期时长 单位天\n为 0 时表示不清理此类图片\n当图片在指定时间内未被再次使用,就会被删除\n可选类型:\nDRAW: 由插件绘制的图片\nIMAGES: 动态图和封面等\nEMOJI: B站的Emoji\nUSER: 用户头像,头像挂件,粉丝卡片套装等\nOTHER: 其他图片")
    val cacheConfig: CacheConfig by value()

    @ValueDescription("代理配置:\nproxy: 代理列表")
    val proxyConfig: ProxyConfig by value()

    @ValueDescription("翻译配置:\n百度翻译 API 密钥\nhttps://api.fanyi.baidu.com/")
    val translateConfig: TranslateConfig by value()

}

@Serializable
data class EnableConfig(
    val enable: Boolean = true,
    val proxyEnable: Boolean = true,
    val cacheClearEnable: Boolean = true,
)

@Serializable
data class TranslateConfig(
    val APP_ID: String = "",
    val SECURITY_KEY: String = "",
)

@Serializable
data class ImageConfig(
    val quality: String = "1000w",
    val theme: String = "v3",
    val font: String = "HarmonyOS Sans_SC Medium",
    val fontSizeMultiple: Float = 1.0f,
    /**
     * 可选值:
     * fanCard 粉丝卡片
     * qrCode  动态链接二维码
     * none    无
     */
    val cardOrnament: String = "fanCard",
    val badgeEnable: BadgeEnable = BadgeEnable(),
)

@Serializable
data class BadgeEnable(
    val left: Boolean = true,
    val right: Boolean = false,
){
    val enable: Boolean = left || right
}

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
    val liveInterval: Int = 20,
    val lowSpeed: String = "0-0x2",
)


@Serializable
data class PushConfig(
    val quality: Int = 1,
)

@Serializable
data class TemplateConfig(
    val defaultDynamicPush: String = "oneMsg",
    val defaultLivePush: String = "oneMsg",
    val dynamicPush: MutableMap<String, String> = mutableMapOf(
        "drawOnly" to "{draw}",
        "oneMsg" to "{draw}\n{name}@{type}\n{link}",
        "twoMsg" to "{draw}\r{name}@{uid}@{type}\n{time}\n{link}",
        "forwardMsg" to "{draw}{>>}作者：{name}\nUID：{uid}\n时间：{time}\n类型：{type}\n链接：{link}\r{content}\r{images}{<<}",
    ),
    val livePush: MutableMap<String, String> = mutableMapOf(
        "drawOnly" to "{draw}",
        "oneMsg" to "{draw}\n{name}@直播\n{link}",
        "oneMsgAtAll" to "{draw}\n{name}@直播\n{link}",
        "twoMsg" to "{draw}\r{name}@{uid}@直播\n{title}\n{time}\n{link}",
    ),
    val forwardCard: ForwardDisplay = ForwardDisplay(),
    val footer: String = ""
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
    val expires: Map<CacheType, Int> = mapOf(
        CacheType.DRAW to 7,
        CacheType.IMAGES to 7,
        CacheType.EMOJI to 7,
        CacheType.USER to 7,
        CacheType.OTHER to 7,
    )

)

