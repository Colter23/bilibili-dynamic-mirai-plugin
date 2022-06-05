package top.colter.mirai.plugin.bilibili

import top.colter.mirai.plugin.bilibili.BiliDynamicConfig.accountConfig
import top.colter.mirai.plugin.bilibili.BiliDynamicConfig.followGroup
import top.colter.mirai.plugin.bilibili.api.createGroup
import top.colter.mirai.plugin.bilibili.api.followGroup
import top.colter.mirai.plugin.bilibili.api.userInfo
import top.colter.mirai.plugin.bilibili.client.BiliClient

suspend fun initTagid() {
    val client = BiliClient()
    runCatching {
        BiliBiliDynamic.mid = client.userInfo()?.mid!!
        BiliBiliDynamic.logger.info("BiliBili UID: ${BiliBiliDynamic.mid}")
    }.onFailure {
        BiliBiliDynamic.logger.error(it.message)
        return
    }
    if (accountConfig.autoFollow && accountConfig.followGroup.isNotEmpty()) {
        client.followGroup()?.forEach {
            if (it.name == followGroup) {
                BiliBiliDynamic.tagid = it.tagId
                return
            }
        }

        val res = client.createGroup(followGroup)
        if (res == null) {
            BiliBiliDynamic.logger.error("创建分组失败")
            return
        }
        BiliBiliDynamic.tagid = res.tagId
    }
}