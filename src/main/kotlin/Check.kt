package top.colter.mirai.plugin

import com.alibaba.fastjson.JSONObject
import kotlinx.coroutines.delay
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.info
import top.colter.mirai.plugin.PluginConfig.BPI
import top.colter.mirai.plugin.bean.Dynamic
import top.colter.mirai.plugin.utils.httpGet
import java.text.SimpleDateFormat

suspend fun check(bot: Bot){
    while (true){
        if (!(PluginConfig.botState&&(PluginConfig.dynamic["enable"]=="true"||PluginConfig.live["enable"]=="true"))){
            continue
        }
        try {
            PluginMain.logger.info {"Start testing...开始检测..."}

            val timestamp = System.currentTimeMillis()
            val time = SimpleDateFormat("HHmm").format(timestamp)

            val interval = PluginConfig.dynamic["interval"]!!.toLong()
            val shortDelay = 1000L..4000L
            val middleDelay = interval*1000..(interval+5)*1000
            val longDelay = (interval+10)*1000..(interval+15)*1000
            var delay = middleDelay

            val s = PluginConfig.dynamic["lowSpeed"]!!.split("-")
            if (s[0]!=s[1]){
                if (time.toInt() in s[0].toLong()..s[1].toLong()){
                    delay = longDelay
                }
            }


            PluginData.userData.forEach { user ->
                //获取动态
                delay(delay.random())
                val rawDynamicList = httpGet(BPI["dynamic"]+user.uid ,BPI["COOKIE"]!!).getJSONObject("data").getJSONArray("cards")
                val rawDynamicOne = rawDynamicList.getJSONObject(0)

                //动态检测
                if (PluginConfig.dynamic["enable"]=="true") {
                    var r = false
                    // 判断是否为最新动态
                    for (i in rawDynamicList.size downTo 1){
                        val rawDynamic = rawDynamicList[i-1] as JSONObject
                        val dynamicId = rawDynamic.getJSONObject("desc").getBigInteger("dynamic_id").toString()
                        if (!PluginMain.historyDynamic.contains(dynamicId)&&r){
                            user.dynamicId = dynamicId
                            PluginMain.historyDynamic.add(dynamicId)
                            sendDynamic(bot, rawDynamic, user)
                        }
                        if (dynamicId==user.dynamicId){
                            r = true
                        }
                    }
                }

                //直播检测
                if (PluginConfig.live["enable"]=="true") {
                    val liveStatus =
                        try {
                            rawDynamicOne.getJSONObject("display").getJSONObject("live_info").getInteger("live_status")
                        }catch (e:Exception){
                            0
                        }
                    if (liveStatus == 1 && (user.liveStatus==0||user.liveStatus==2)){
                        delay(shortDelay.random())
                        val roomInfo = httpGet(BPI["liveStatus"] + user.liveRoom).getJSONObject("data").getJSONObject("room_info")

                        val dynamic = Dynamic()
                        dynamic.did = user.liveRoom
                        dynamic.timestamp = roomInfo.getBigInteger("live_start_time").toLong()
                        dynamic.content = "直播: ${roomInfo.getString("title")}"
                        dynamic.isDynamic = false
                        dynamic.pictures = mutableListOf()
                        dynamic.info = "直播ID:"+user.liveRoom
                        dynamic.link = "https://live.bilibili.com/"+user.liveRoom

                        val cover = roomInfo.getString("cover")
                        val keyframe = roomInfo.getString("keyframe")
                        if (cover!=""){
                            dynamic.pictures?.add(cover)
                        }else if(keyframe!=""){
                            dynamic.pictures?.add(keyframe)
                        }
                        sendMessage(bot,user.uid,buildResMessage(dynamic, user))
                    }
                    user.liveStatus = liveStatus
                }

            }

            PluginMain.logger.info {"检测结束"}
            delay(20000L)

        }catch (e:Exception){
            if (PluginConfig.exception){
                bot.getGroup(PluginConfig.adminGroup)?.sendMessage("检测动态失败，2分钟后重试\n"+e.message)
            }
            delay(120000L)
        }
    }
}