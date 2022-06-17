package top.colter.mirai.plugin.bilibili.old

import top.colter.mirai.plugin.bilibili.*
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic.reload
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic.save

fun migration(){
    migrationData()
    migrationConfig()
}

fun migrationData(){
    if (BiliBiliDynamic.dataFolder.resolve("BiliSubscribeData.yml").exists()){
        BiliSubscribeData.reload()
        if (!BiliSubscribeData.migrated){
            BiliBiliDynamic.logger.info("开始转移旧版数据...")

            BiliSubscribeData.dynamic.forEach { (t, u) ->
                if (!BiliData.dynamic.containsKey(t) || t == 0L){
                    BiliData.dynamic[t] = SubData(
                        u.name,
                        if (u.color == "#d3edfa") null else u.color,
                        u.last,
                        u.lastLive,
                        u.contacts.keys.toMutableList(),
                        u.banList
                    )
                    u.contacts.forEach { (c, l) ->
                        if (l != "11"){
                            when (l[0]){
                                '0' -> {
                                    if (!BiliData.filter.containsKey(c)){
                                        BiliData.filter[c] = mutableMapOf()
                                    }
                                    BiliData.filter[c]!![t] = DynamicFilter(
                                        typeSelect = TypeFilter(
                                            FilterMode.BLACK_LIST,
                                            mutableListOf(
                                                DynamicFilterType.DYNAMIC,
                                                DynamicFilterType.FORWARD,
                                                DynamicFilterType.VIDEO,
                                                DynamicFilterType.ARTICLE,
                                                DynamicFilterType.MUSIC
                                            )
                                        )
                                    )
                                }
                                '2' -> {
                                    if (!BiliData.filter.containsKey(c)){
                                        BiliData.filter[c] = mutableMapOf()
                                    }
                                    BiliData.filter[c]!![t] = DynamicFilter(
                                        typeSelect = TypeFilter(
                                            FilterMode.WHITE_LIST,
                                            mutableListOf(DynamicFilterType.VIDEO)
                                        )
                                    )
                                }
                            }
                            when (l[1]){
                                '0' -> {
                                    if (!BiliData.filter.containsKey(c)){
                                        BiliData.filter[c] = mutableMapOf()
                                    }
                                    if (!BiliData.filter[c]!!.containsKey(t)){
                                        BiliData.filter[c]!![t] = DynamicFilter()
                                    }
                                    if (BiliData.filter[c]!![t]!!.typeSelect.mode == FilterMode.BLACK_LIST) {
                                        BiliData.filter[c]!![t]!!.typeSelect.list.add(DynamicFilterType.LIVE)
                                    }
                                }
                                '1' -> {
                                    if (BiliData.filter[c]?.get(t)?.typeSelect?.mode == FilterMode.WHITE_LIST){
                                        BiliData.filter[c]?.get(t)?.typeSelect?.list?.add(DynamicFilterType.LIVE)
                                    }
                                }
                            }
                        }
                    }
                }else {
                    BiliBiliDynamic.logger.warning("新旧数据冲突! $t")
                }
            }
            BiliData.save()
            BiliSubscribeData.migrated = true
            BiliSubscribeData.save()
            BiliBiliDynamic.logger.info("数据转移成功")
        }
    }
}

fun migrationConfig(){
    if (BiliBiliDynamic.configFolder.resolve("BiliPluginConfig.yml").exists()){
        BiliPluginConfig.reload()
        if (!BiliPluginConfig.migrated){
            BiliBiliDynamic.logger.info("开始转移旧版配置...")

            //BiliConfig.admin = BiliPluginConfig.admin
            BiliConfig.accountConfig.cookie = BiliPluginConfig.cookie
            BiliConfig.accountConfig.autoFollow = BiliPluginConfig.autoFollow
            BiliConfig.accountConfig.followGroup = BiliPluginConfig.followGroup
            BiliConfig.checkConfig.interval = BiliPluginConfig.interval
            BiliConfig.checkConfig.liveInterval = BiliPluginConfig.liveInterval
            BiliConfig.checkConfig.lowSpeed = BiliPluginConfig.lowSpeed
            BiliConfig.imageConfig.font = BiliPluginConfig.font.split(";").first().split(".").first()
            //BiliConfig.templateConfig.dynamicPush["OldCustom"] = BiliPluginConfig.pushTemplate
            //BiliConfig.templateConfig.livePush["OldCustom"] = BiliPluginConfig.livePushTemplate
            BiliConfig.templateConfig.footer = BiliPluginConfig.footerTemplate
            if (BiliPluginConfig.qrCode) BiliConfig.imageConfig.cardOrnament = "QrCode"
            BiliConfig.enableConfig.translateEnable = BiliPluginConfig.baiduTranslate["enable"].toBoolean()
            BiliConfig.translateConfig.baidu.APP_ID = BiliPluginConfig.baiduTranslate["APP_ID"]?:""
            BiliConfig.translateConfig.baidu.SECURITY_KEY = BiliPluginConfig.baiduTranslate["SECURITY_KEY"]?:""

            BiliConfig.save()
            BiliPluginConfig.migrated = true
            BiliPluginConfig.save()
            BiliBiliDynamic.logger.info("配置转移成功")
        }
    }
}

fun migrationImage(){

}