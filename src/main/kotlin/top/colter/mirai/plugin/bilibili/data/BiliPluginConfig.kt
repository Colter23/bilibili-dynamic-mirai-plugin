package top.colter.mirai.plugin.bilibili.data

import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object BiliPluginConfig : ReadOnlyPluginConfig("BiliPluginConfig") {

    @ValueDescription("管理员")
    val admin: String by value("")

    @ValueDescription("推送模式\n0: 文字推送\n1: 图片推送")
    val pushMode: Int by value(1)

    @ValueDescription("添加订阅时是否允许bot自动关注未关注的用户")
    val autoFollow: Boolean by value(true)

    @ValueDescription("Bot关注时保存的分组(最长16字符)")
    val followGroup: String by value("Bot关注")

    @ValueDescription("检测间隔(推荐15-30) 单位秒")
    val interval: Int by value(15)

    @ValueDescription("图片推送模式用的字体, 详细请看readme")
    val font: String by value("Microsoft Yahei")

    @ValueDescription("直播@全体")
    val liveAtAll: Boolean by value(false)

    @ValueDescription("推送文字模板, 参数请看readme")
    val pushTemplate: String by value("{name}@{type}\n{link}")

    //@Suppress(stringSerialization = DOUBLE_QUOTATION)
    @ValueDescription("cookie, 请使用双引号")
    val cookie: String by value("")

    @ValueDescription("百度翻译")
    val baiduTranslate: Map<String, String> by value(
        mapOf(
            //是否开启百度翻译
            "enable" to "false",
            //百度翻译api密钥
            "APP_ID" to "",
            "SECURITY_KEY" to ""
        )
    )

}