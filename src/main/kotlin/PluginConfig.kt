package top.colter.mirai.plugin

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object PluginConfig : AutoSavePluginConfig("config") {

    // 管理,报错都会发送此 必填!!!!!!!!!!
    var admin : Long by value()
    // 是否开启报错推送
    var exception by value(true)
    // 推送模式
    // 0 :图片推送(默认)     1 : 文字推送
    var pushMode by value(0)

    // bot状态
    var botState by value(true)
    // 插件的数据路径 基于启动器根目录
    var basePath : String by value("/DynamicPlugin")
    // 如不带后缀名则使用系统的字体，如系统中没有这个字体 则会使用系统默认字体
    // 带后缀名则使用上面的数据路径下/font 文件夹下的字体文件
    // 需自行创建font文件夹，并把字体文件放进去
    val font : String by value("微软雅黑")

    //---------------好友相关----------------//
    var friend : MutableMap<String,String> by value(mutableMapOf(
        //好友功能总开关 包括回复
        "enable" to "false",
        //同意好友申请
        "agreeNewFriendRequest" to "false",
        //欢迎语 不需要的话请删除引号中间的内容
        "welcomeMessage" to "( •̀ ω •́ )✧"
    ))
    //---------------群相关----------------//
    var group : MutableMap<String,String> by value(mutableMapOf(
        //群功能总开关 包括回复
        "enable" to "false",
        //欢迎新成员
        "welcomeMemberJoin" to "false",
        //欢迎语
        "welcomeMessage" to "欢迎"
    ))

    //---------------动态检测----------------//
    var dynamic : MutableMap<String,String> by value(mutableMapOf(
        //动态检测总开关
        "enable" to "true",
        //访问间隔 单位:秒  范围:[1,∞]
        //这个间隔是每次访问b站api时就会触发
        "interval" to "10",
        //慢速模式开启时间段 不开启则填000-000
        //例：200..800就是凌晨2点到8点
        "lowSpeed" to "200-800",
        //是否保存动态图片
        "saveDynamicImage" to "true"
    ))

    //---------------直播检测----------------//
    var live : MutableMap<String,String> by value(mutableMapOf(
        //直播检测总开关
        "enable" to "true"
    ))

    //---------------百度翻译----------------//
    var baiduTranslate : Map<String,String> by value(mapOf(
        //是否开启百度翻译
        "enable" to "false",
        //百度翻译api密钥
        "APP_ID" to "",
        "SECURITY_KEY" to ""
    ))

    //----------BiliBiliApi(BPI) B站API----------//
    var BPI : Map<String,String> by value(mapOf(
        // 动态API
        "dynamic" to "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?visitor_uid=1111111111&offset_dynamic_id=0&need_top=0&host_uid=",
        // 粉丝数API
        "followNum" to "https://api.bilibili.com/x/relation/stat?vmid=",
        // 直播状态API
        "liveStatus" to "https://api.live.bilibili.com/xlive/web-room/v1/index/getInfoByRoom?room_id=",
        // 直播id API
        "liveRoom" to "https://api.live.bilibili.com/room/v1/Room/getRoomInfoOld?mid=",
        // 大航海数 需要参数 用户id:ruid 直播间id:roomid  eg: ruid=487550002&roomid=21811136
        "guard" to "https://api.live.bilibili.com/xlive/app-room/v2/guardTab/topList?page=1&page_size=1&",
        // cookie 必填!!!!!!!!!!!!!!!!
        "COOKIE" to ""
    ))
}