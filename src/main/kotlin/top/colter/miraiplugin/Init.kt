package top.colter.miraiplugin

import com.alibaba.fastjson.JSON
import kotlinx.coroutines.delay
import top.colter.miraiplugin.bean.User
import top.colter.miraiplugin.utils.httpGet
import top.colter.miraiplugin.PluginConfig.BPI
import top.colter.miraiplugin.utils.generateImg
import java.awt.Font
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

suspend fun init(){
    PluginMain.logger.info("初始化数据中...")

    PluginData.userData.forEach { user ->
        delay((500..2000L).random())
        val rawDynamic = httpGet(BPI["dynamic"]+user.uid).getJSONObject("data").getJSONArray("cards")

        val raw0 = rawDynamic.getJSONObject(0)
        val desc = raw0.getJSONObject("desc")
        user.dynamicId = desc.getBigInteger("dynamic_id").toString()

        try {
            if (user.liveRoom!="0"){
                delay(500)
                user.liveStatus = httpGet(BPI["liveStatus"] + user.liveRoom).getJSONObject("data").getJSONObject("room_info").getInteger("live_status")
            }
        }catch (e:Exception){
            user.liveStatus = 0
        }

        var lastId = "0"
        try {
            rawDynamic.forEach { item ->
                val desc = JSON.parseObject(item.toString()).getJSONObject("desc")
                lastId = desc.getBigInteger("dynamic_id").toString()
                PluginMain.historyDynamic.add(lastId)
            }
        }catch (e:Exception){

        }


        // 查找记录上一页动态
        delay((500L..700L).random())
        val rawDynamic2 = httpGet(BPI["dynamic"]+user.uid+"&offset_dynamic_id="+lastId ).getJSONObject("data").getJSONArray("cards")

        try{
            rawDynamic2.forEach { item ->
                val desc = JSON.parseObject(item.toString()).getJSONObject("desc")
                PluginMain.historyDynamic.add(desc.getBigInteger("dynamic_id").toString())
            }
        }catch (e:Exception){

        }

    }

    // 初始化字体
    if (PluginConfig.font.indexOf('.')!=-1){
        var bis : BufferedInputStream? = null
        try {
            bis = BufferedInputStream(FileInputStream(File("${PluginData.runPath}${PluginConfig.basePath}/font/${PluginConfig.font}")))
            PluginMain.font = Font.createFont(Font.TRUETYPE_FONT, bis)
        }catch (e:Exception){

        }finally {
            bis?.close()
        }
    }

    PluginMain.logger.info("初始化结束")
}

suspend fun initFollowInfo(uid:String, user: User, hex: String): String? {
    val rawDynamic = httpGet(BPI["dynamic"]+uid).getJSONObject("data").getJSONArray("cards")
    rawDynamic.forEach { item ->
        val desc = JSON.parseObject(item.toString()).getJSONObject("desc")
        PluginMain.historyDynamic.add(desc.getBigInteger("dynamic_id").toString())
    }
    val res = rawDynamic.getJSONObject(0)
    val userProfile = res.getJSONObject("desc").getJSONObject("user_profile")
    val name = userProfile.getJSONObject("info").getString("uname")

    user.uid = uid
    user.name = name
    user.dynamicId = res.getJSONObject("desc").getBigInteger("dynamic_id").toString()

    val face = userProfile.getJSONObject("info").getString("face")
    val pendant = userProfile.getJSONObject("pendant").getString("image")

    delay(500)
    val liveRoom = httpGet(BPI["liveRoom"]+uid,"aaa").getJSONObject("data").getBigInteger("roomid").toString()
    user.liveRoom = liveRoom

    try {
        if (user.liveRoom!="0"){
            delay(500)
            user.liveStatus = httpGet(BPI["liveStatus"] + user.liveRoom).getJSONObject("data").getJSONObject("room_info").getInteger("live_status")
        }
    }catch (e:Exception){
        user.liveStatus = 0
    }

    var r :String? = null
    if (PluginConfig.pushMode ==0){
        r = generateImg(uid,name,face,pendant,hex)
    }

    return r
}