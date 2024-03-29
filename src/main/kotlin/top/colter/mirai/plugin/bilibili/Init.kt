package top.colter.mirai.plugin.bilibili

import top.colter.mirai.plugin.bilibili.BiliConfig.accountConfig
import top.colter.mirai.plugin.bilibili.api.createGroup
import top.colter.mirai.plugin.bilibili.api.followGroup
import top.colter.mirai.plugin.bilibili.api.userInfo
import top.colter.mirai.plugin.bilibili.data.EditThisCookie
import top.colter.mirai.plugin.bilibili.data.toCookie
import top.colter.mirai.plugin.bilibili.utils.FontUtils.loadTypeface
import top.colter.mirai.plugin.bilibili.utils.biliClient
import top.colter.mirai.plugin.bilibili.utils.decode
import xyz.cssxsh.mirai.skia.downloadTypeface
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
    val cookieFile = BiliBiliDynamic.dataFolder.resolve("cookies.json")
    if (cookieFile.exists()) {
        try {
            val cookie = cookieFile.readText().decode<List<EditThisCookie>>().toCookie()
            if (!cookie.isEmpty()) {
                BiliBiliDynamic.cookie = cookie
            } else {
                BiliBiliDynamic.logger.error("cookies.json 中缺少必要的值 [SESSDATA] [bili_jct]")
            }
        } catch (e: Exception) {
            BiliBiliDynamic.logger.error("解析 cookies.json 失败")
        }
    }
    if (BiliBiliDynamic.cookie.isEmpty()) BiliBiliDynamic.cookie.parse(accountConfig.cookie)

    try {
        BiliBiliDynamic.uid = biliClient.userInfo()?.mid!!
        BiliBiliDynamic.logger.info("BiliBili UID: ${BiliBiliDynamic.uid}")
    } catch (e: Exception) {
        BiliBiliDynamic.logger.error(e.message)
        BiliBiliDynamic.logger.error("如未登录，请bot管理员在聊天环境内发送 /bili login 进行登录")
        return
    }
}

suspend fun initTagid() {
    if (accountConfig.autoFollow && accountConfig.followGroup.isNotEmpty()) {
        try {
            biliClient.followGroup()?.forEach {
                if (it.name == accountConfig.followGroup) {
                    BiliBiliDynamic.tagid = it.tagId
                    return
                }
            }
            val res = biliClient.createGroup(accountConfig.followGroup) ?: throw Exception()
            BiliBiliDynamic.tagid = res.tagId
        } catch (e: Exception) {
            BiliBiliDynamic.logger.error("初始化分组失败 ${e.message}")
        }

    }
}

suspend fun loadFonts() {
    val fontFolder = BiliBiliDynamic.dataFolder.resolve("font")
    val fontFolderPath = BiliBiliDynamic.dataFolderPath.resolve("font")
    val LXGW = fontFolder.resolve("LXGWWenKai-Bold.ttf")

    fontFolderPath.apply {
        if (!exists()) createDirectory()
        if (fontFolder.listFiles().none { it.isFile } || !LXGW.exists()) {
            try {
                downloadTypeface(fontFolder, "https://file.zfont.cn/d/file/font_cn_file/霞鹜文楷-v1.235.2.zip")
                val f = fontFolder.resolve("霞鹜文楷-v1.235.2")
                f.resolve("LXGWWenKai-Bold.ttf").copyTo(LXGW)
                try {
                    f.walkBottomUp().onLeave { it.delete() }
                }catch (_: Exception) { }
            }catch (e: Throwable) {
                BiliBiliDynamic.logger.error("下载字体失败! $e")
            }
        }
        forEachDirectoryEntry {
            if (it.toFile().isFile) loadTypeface(it.toString(), it.name.split(".").first())
        }
    }
}