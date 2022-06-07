package top.colter.mirai.plugin.bilibili

import top.colter.mirai.plugin.bilibili.BiliDynamicConfig.accountConfig
import top.colter.mirai.plugin.bilibili.api.createGroup
import top.colter.mirai.plugin.bilibili.api.followGroup
import top.colter.mirai.plugin.bilibili.api.userInfo
import top.colter.mirai.plugin.bilibili.utils.biliClient


suspend fun checkCookie(){
    runCatching {
        BiliBiliDynamic.mid = biliClient.userInfo()?.mid!!
        BiliBiliDynamic.logger.info("BiliBili UID: ${BiliBiliDynamic.mid}")
    }.onFailure {
        BiliBiliDynamic.logger.error(it.message)
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

        val res = biliClient.createGroup(accountConfig.followGroup)
        if (res == null) {
            BiliBiliDynamic.logger.error("创建分组失败")
            return
        }
        BiliBiliDynamic.tagid = res.tagId
    }
}