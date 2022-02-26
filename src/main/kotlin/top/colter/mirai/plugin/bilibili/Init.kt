package top.colter.mirai.plugin.bilibili

import net.mamoe.mirai.utils.error
import top.colter.mirai.plugin.bilibili.PluginMain.biliJct
import top.colter.mirai.plugin.bilibili.PluginMain.sessData
import top.colter.mirai.plugin.bilibili.data.*
import top.colter.mirai.plugin.bilibili.data.BiliPluginConfig.autoFollow
import top.colter.mirai.plugin.bilibili.data.BiliPluginConfig.followGroup
import top.colter.mirai.plugin.bilibili.utils.HttpUtils
import top.colter.mirai.plugin.bilibili.utils.decode

fun initCookie() {
    val cookieList = BiliPluginConfig.cookie.split("; ", ";")
    cookieList.forEach {
        val cookieItem = it.split("=")
        if (cookieItem[0] == "SESSDATA") {
            sessData = "SESSDATA=${cookieItem[1]}"
        } else if (cookieItem[0] == "bili_jct") {
            biliJct = cookieItem[1]
        }
    }
    if (sessData == "" || biliJct == "") {
        PluginMain.logger.error("Cookie错误!请检查是否有 SESSDATA 与 bili_jct 字段, 或使用 /bili login 进行登录")
    }
}

fun initTagid() {
    val httpUtils = HttpUtils()
    runCatching {
        PluginMain.mid = httpUtils.getAndDecode<UserID>(USER_ID).mid
        PluginMain.logger.info("BiliBili UID: ${PluginMain.mid}")
    }.onFailure {
        PluginMain.logger.error(it.message)
        return
    }
    if (autoFollow && followGroup.isNotEmpty()) {
        val groups = httpUtils.getAndDecode<List<FollowGroup>>(FOLLOW_GROUP)
        groups.forEach {
            if (it.name == followGroup) {
                PluginMain.tagid = it.tagId
                return
            }
        }
        val pb = "tag=$followGroup&csrf=${biliJct}"
        val res = httpUtils.post(CREATE_GROUP, pb).decode<ResultData>()
        if (res.code != 0) {
            PluginMain.logger.error { "创建分组失败: ${res.message}" }
            return
        }
        PluginMain.tagid = res.data?.decode<FollowGroup>()?.tagId ?: 0
    }
}