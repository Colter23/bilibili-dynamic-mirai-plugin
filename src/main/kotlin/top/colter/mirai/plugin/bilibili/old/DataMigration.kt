package top.colter.mirai.plugin.bilibili.old

import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic.reload

fun migration(){
    migrationData()
    migrationConfig()
}

fun migrationData(){
    if (BiliBiliDynamic.dataFolder.resolve("BiliSubscribeData.yml").exists()){
        BiliSubscribeData.reload()


    }
}

fun migrationConfig(){
    if (BiliBiliDynamic.configFolder.resolve("BiliPluginConfig.yml").exists()){
        BiliPluginConfig.reload()


    }
}

fun migrationImage(){



}