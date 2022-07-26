package top.colter.mirai.plugin.bilibili

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import org.jetbrains.skia.paragraph.Alignment
import top.colter.mirai.plugin.bilibili.utils.CacheType


object BiliConfig : AutoSavePluginConfig("BiliConfig") {

    @ValueDescription("具体的配置文件描述请前往下方链接查看")
    val help: String by value("https://github.com/Colter23/bilibili-dynamic-mirai-plugin#BiliConfig.yml")

    @ValueDescription("管理员")
    var admin: Long by value(0L)

    @ValueDescription(
        """
        功能开关:
          drawEnable: 绘图开关
          notifyEnable: 操作通知开关
          lowSpeedEnable: 低频检测开关
          translateEnable: 翻译开关
          proxyEnable: 代理开关
          cacheClearEnable: 缓存清理开关
    """
    )
    val enableConfig: EnableConfig by value()

    @ValueDescription(
        """
        账号配置:
          cookie: BiliBili的cookie, 可使用 /bili login 自动获取
          autoFollow: 添加订阅时是否允许 bot 自动关注未关注的用户
          followGroup: Bot 关注时保存的分组(最长16字符)
    """
    )
    val accountConfig: BiliAccountConfig by value()

    @ValueDescription(
        """
        检测配置:
          interval: 动态检测间隔(推荐 15-30) 单位秒
          liveInterval: 直播检测间隔(与动态检测独立) 单位秒
          lowSpeed: 低频检测时间段与倍率(例: 3-8x2 三点到八点检测间隔为正常间隔的2倍) 24小时制
    """
    )
    val checkConfig: CheckConfig by value()

    @ValueDescription(
        """
        推送配置:
          messageInterval: QQ中同一个群中连续发送多个消息的间隔 单位毫秒
          pushInterval: QQ中连续发送多个群之间的间隔 单位毫秒
          atAllPlus: At全体拼接方式 SINGLE_MESSAGE: 单独的消息  PLUS_END: 追加到最后一条消息后面
    """
    )
    val pushConfig: PushConfig by value()

    @ValueDescription(
        """
        图片配置:
        当 ImageQuality.yml / ImageTheme.yml 中的 customOverload 开启后下面对应的配置将不再生效 
          quality: 图片质量(分辨率), 内置 800w: 800px, 1000w: 1000px, 1200w: 1200px, 1500w: 1500px(图片宽度)
          theme: 绘图主题, 内置 v3: 新版绘图主题, v2: 旧版绘图主题
          font: 绘图字体 字体名或字体文件名(不用加后缀) 目前仅支持单字体 字体放到插件数据路径下 `font` 文件夹中
          defaultColor: 默认绘图主题色 支持多个值自定义渐变 中间用分号`;`号分隔 单个值会自动生成渐变色
          cardOrnament: 卡片装饰 FanCard(粉丝卡片)  QrCode(动态链接二维码)  None(无)
          colorGenerator: 渐变色生成器配置 
          badgeEnable: 卡片顶部的标签 左边右边是否开启
    """
    )
    val imageConfig: ImageConfig by value()

    @ValueDescription(
        """
        模板配置:
          defaultDynamicPush: 默认使用的推送模板, 填写下方动态模板名
          dynamicPush: 动态推送模板
          livePush: 直播推送模板
          forwardCard: 转发卡片模板
          footer: 图片页脚
    """
    )
    val templateConfig: TemplateConfig by value()

    @ValueDescription(
        """
        缓存配置:
          expires: 图片过期时长 单位天
          为 0 时表示不清理此类图片
          当图片在指定时间内未被再次使用,就会被删除
          可选类型:
            DRAW: 由插件绘制的图片
            IMAGES: 动态图和封面等
            EMOJI: B站的Emoji
            USER: 用户头像,头像挂件,粉丝卡片套装等
            OTHER: 其他图片
    """
    )
    val cacheConfig: CacheConfig by value()

    @ValueDescription(
        """
        代理配置:
          proxy: 代理列表
    """
    )
    val proxyConfig: ProxyConfig by value()

    @ValueDescription(
        """
        翻译配置:
          cutLine: 正文与翻译的分割线
          baidu: 百度翻译 API 密钥 https://api.fanyi.baidu.com
    """
    )
    val translateConfig: TranslateConfig by value()

}

@Serializable
data class EnableConfig(
    val drawEnable: Boolean = true,
    val notifyEnable: Boolean = true,
    val lowSpeedEnable: Boolean = true,
    var translateEnable: Boolean = false,
    val proxyEnable: Boolean = false,
    val cacheClearEnable: Boolean = true,
)

@Serializable
data class TranslateConfig(
    val cutLine: String = "\n\n〓〓〓 翻译 〓〓〓\n",
    var baidu: BaiduTranslateConfig = BaiduTranslateConfig()
) {
    @Serializable
    data class BaiduTranslateConfig(
        var APP_ID: String = "",
        var SECURITY_KEY: String = "",
    )
}

@Serializable
data class ImageConfig(
    val quality: String = "1000w",
    val theme: String = "v3",
    var font: String = "HarmonyOS Sans SC Medium",
    var defaultColor: String = "#d3edfa",
    var cardOrnament: String = "FanCard",
    val colorGenerator: ColorGenerator = ColorGenerator(),
    val badgeEnable: BadgeEnable = BadgeEnable(),
) {

    @Serializable
    data class ColorGenerator(
        val hueStep: Int = 30,
        val lockSB: Boolean = true,
        val saturation: Float = 0.25f,
        val brightness: Float = 1f,
    )

    @Serializable
    data class BadgeEnable(
        var left: Boolean = true,
        var right: Boolean = false,
    ) {
        val enable: Boolean get() = left || right
    }
}

@Serializable
data class ProxyConfig(
    val proxy: List<String> = listOf(),
//    val verifyUrl: String = "http://httpbin.org",
)

@Serializable
data class BiliAccountConfig(
    var cookie: String = "",
    var autoFollow: Boolean = true,
    var followGroup: String = "Bot关注"
)

@Serializable
data class CheckConfig(
    var interval: Int = 15,
    var liveInterval: Int = 20,
    var lowSpeed: String = "0-0x2",
)


@Serializable
data class PushConfig(
    val messageInterval: Long = 100,
    val pushInterval: Long = 500,
    val atAllPlus: String = "PLUS_END", // SINGLE_MESSAGE PLUS_END
)

@Serializable
data class TemplateConfig(
    var defaultDynamicPush: String = "OneMsg",
    var defaultLivePush: String = "OneMsg",
    val dynamicPush: MutableMap<String, String> = mutableMapOf(
        "DrawOnly" to "{draw}",
        "TextOnly" to "{name}@{type}\n{link}\n{content}\n{images}",
        "OneMsg" to "{draw}\n{name}@{type}\n{link}",
        "TwoMsg" to "{draw}\r{name}@{uid}@{type}\n{time}\n{link}",
        "ForwardMsg" to "{draw}{>>}作者：{name}\nUID：{uid}\n时间：{time}\n类型：{type}\n链接：{link}\r{content}\r{images}{<<}",
    ),
    val livePush: MutableMap<String, String> = mutableMapOf(
        "DrawOnly" to "{draw}",
        "TextOnly" to "{name}@直播\n{link}\n标题: {title}",
        "OneMsg" to "{draw}\n{name}@直播\n{link}",
        "TwoMsg" to "{draw}\r{name}@{uid}@直播\n{title}\n{time}\n{link}",
    ),
    val forwardCard: ForwardDisplay = ForwardDisplay(),
    var footer: FooterConfig = FooterConfig(),

    )

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

