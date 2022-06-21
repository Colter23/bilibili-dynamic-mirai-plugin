package top.colter.mirai.plugin.bilibili

import top.colter.mirai.plugin.bilibili.BiliConfig.accountConfig
import top.colter.mirai.plugin.bilibili.api.createGroup
import top.colter.mirai.plugin.bilibili.api.followGroup
import top.colter.mirai.plugin.bilibili.api.userInfo
import top.colter.mirai.plugin.bilibili.utils.FontUtils.loadTypeface
import top.colter.mirai.plugin.bilibili.utils.biliClient
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.forEachDirectoryEntry
import kotlin.io.path.name

suspend fun initData() {
    checkCookie()
    initTagid()
    loadFonts()
}

suspend fun checkCookie() {
    BiliBiliDynamic.cookie.parse(accountConfig.cookie)

    try {
        BiliBiliDynamic.mid = biliClient.userInfo()?.mid!!
        BiliBiliDynamic.logger.info("BiliBili UID: ${BiliBiliDynamic.mid}")
    }catch (e: Exception) {
        BiliBiliDynamic.logger.error(e.message)
        BiliBiliDynamic.logger.error("如未登录，请bot管理员在聊天环境内发送 /bili login 进行登录")
        return
    }
}

suspend fun initTagid() {
    if (accountConfig.autoFollow && accountConfig.followGroup.isNotEmpty()) {
        biliClient.followGroup()?.forEach {
            if (it.name == accountConfig.followGroup) {
                BiliBiliDynamic.tagid = it.tagId
                return
            }
        }
        try {
            val res = biliClient.createGroup(accountConfig.followGroup) ?: throw Exception()
            BiliBiliDynamic.tagid = res.tagId
        }catch (e: Exception){
            BiliBiliDynamic.logger.error("创建分组失败 ${e.message}")
        }

    }
}

fun loadFonts() {
    BiliBiliDynamic.dataFolderPath.resolve("font").apply {
        if (exists()){
            forEachDirectoryEntry {
                loadTypeface(it.toString(), it.name.split(".").first())
            }
        }else {
            createDirectory()
        }
    }
}