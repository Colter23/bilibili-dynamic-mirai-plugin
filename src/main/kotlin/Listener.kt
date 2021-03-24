package top.colter.mirai.plugin

import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import top.colter.mirai.plugin.bean.User

val emoji = listOf<String>("( •̀ ω •́ )✧","(oﾟvﾟ)ノ","(o゜▽゜)o☆","(￣▽￣)\"","(。・ω・)ノ",
    "(੭ˊ꒳ˋ)੭✧","⸜(* ॑꒳ ॑*  )⸝✩°｡⋆","( •̀ ω •́ )y","(￣3￣)","(ง •_•)ง","o(〃＾▽＾〃)o",
    "ヽ(✿ﾟ▽ﾟ)ノ","(/≧▽≦)/","(๑•̀ㅂ•́)و✧","(p≧▽≦)p"
    )

/**
 * 同意好友申请
 */
object NewFriendRequestListener : ListenerHost {
    @EventHandler
    suspend fun NewFriendRequestEvent.onMessage(){
        if (PluginConfig.friend["enable"]=="true"&&PluginConfig.friend["agreeNewFriendRequest"]=="true") {
            this.accept()
            if (PluginConfig.friend["agreeNewFriendRequest"]!=""){
                delay(2000)
                bot.getFriend(fromId)?.sendMessage(PluginConfig.friend["agreeNewFriendRequest"].toString())
            }

        }
    }
}

object MemberJoinListener : ListenerHost {
    val coroutineContext = SupervisorJob()
    @EventHandler
    suspend fun MemberJoinEvent.onMessage() {
        if (PluginConfig.group["enable"]=="true"&&PluginConfig.group["welcomeMemberJoin"]=="true") {
            group.sendMessage(At(user)+" "+PluginConfig.group["welcomeMessage"].toString()+emoji[(emoji.indices).random()])
        }
    }
}

/**
 * 监听消息
 */
object MessageListener : ListenerHost {
    @EventHandler
    suspend fun MessageEvent.onMessage() {
        val content = message.content

        if(subject is Group && subject.id == PluginConfig.adminGroup){
            when(content){
                "#?", "#？", "#help", "#帮助", "#功能", "#菜单" -> {
                    subject.sendMessage(
                        "#管理 : 查看管理功能\n" +
                            "#开启动态推送 [群/Q号] / #关闭动态推送 [群/Q号]\n" +
                            "#r [指定数字] / #骰子 [指定数字]\n" +
                            "#订阅 <UID> [群/Q号] [16进制主题色]\n"+
                            "#删除 <UID> [群/Q号]\n"+
                            "#订阅列表 [群/Q号]\n\n"+
                            "说明: <>内为必填, []为选填. 中间用空格隔开! 不要带括号!"
                    )
                }
                "#管理" -> {
                    subject.sendMessage(
                        "#关闭bot : 临时关闭bot\n"+
                            "#关闭 -a : 关闭全部功能\n"+
                            "#关闭 -d : 关闭动态功能\n"+
                            "#关闭 -l : 关闭直播功能\n"+
                            "#关闭 -r : 关闭命令回复(除管理群)\n"+
                            "#关闭 -gr : 关闭群命令回复\n"+
                            "#关闭 -fr : 关闭好友命令回复\n\n"+
                            "以上 '关闭' 可换为 'close'\n"+
                            "如要开启把 '关闭' 换成 '开启'或'open' 即可"
                    )
                }
                "#关闭bot", "#close bot" -> {
                    PluginConfig.botState = false
                    subject.sendMessage("bot已关闭")
                }
                "#开启bot", "#open bot" -> {
                    PluginConfig.botState = true
                    subject.sendMessage("bot已开启")
                }
                "#关闭 -a", "#close -a" -> {
                    PluginConfig.dynamic["enable"] = "false"
                    PluginConfig.live["enable"] = "false"
                    PluginConfig.group["enable"] = "false"
                    PluginConfig.friend["enable"] = "false"
                    subject.sendMessage("bot全部功能已关闭")
                }
                "#开启 -a", "#open -a" -> {
                    PluginConfig.dynamic["enable"] = "true"
                    PluginConfig.live["enable"] = "true"
                    PluginConfig.group["enable"] = "true"
                    PluginConfig.friend["enable"] = "true"
                    subject.sendMessage("bot全部功能已开启")
                }
                "#关闭 -d", "#close -d" -> {
                    PluginConfig.dynamic["enable"] = "false"
                    subject.sendMessage("动态转发已关闭")
                }
                "#开启 -d", "#open -d" -> {
                    PluginConfig.dynamic["enable"] = "true"
                    subject.sendMessage("动态转发已开启")
                }
                "#关闭 -l", "#close -l" -> {
                    PluginConfig.live["enable"] = "false"
                    subject.sendMessage("直播转发已关闭")
                }
                "#开启 -l", "#open -l" -> {
                    PluginConfig.live["enable"] = "true"
                    subject.sendMessage("直播转发已开启")
                }
                "#关闭 -r", "#close -r" -> {
                    PluginConfig.group["enable"] = "false"
                    PluginConfig.friend["enable"] = "false"
                    subject.sendMessage("命令回复已关闭（不包括管理群）")
                }
                "#开启 -r", "#open -r" -> {
                    PluginConfig.group["enable"] = "true"
                    PluginConfig.friend["enable"] = "true"
                    subject.sendMessage("命令回复已开启")
                }
                "#关闭 -gr", "#close -gr" -> {
                    PluginConfig.group["enable"] = "false"
                    subject.sendMessage("群命令回复已关闭（不包括管理群）")
                }
                "#开启 -gr", "#open -gr" -> {
                    PluginConfig.group["enable"] = "true"
                    subject.sendMessage("群命令回复已开启")
                }
                "#关闭 -fr", "#close -fr" -> {
                    PluginConfig.friend["enable"] = "false"
                    subject.sendMessage("好友命令回复已关闭")
                }
                "#开启 -fr", "#open -fr" -> {
                    PluginConfig.friend["enable"] = "true"
                    subject.sendMessage("好友命令回复已开启")
                }
            }
        }

        if (!(PluginConfig.botState&&(PluginConfig.group["enable"]=="true"||PluginConfig.friend["enable"]=="true"))){
            return
        }

        when (content) {
            "#?", "#？", "#help", "#帮助", "#功能", "#菜单" -> {
                if (PluginConfig.adminGroup==subject.id){
                    return
                }
                subject.sendMessage(
                    "#? 或 #help 或 #帮助 : 功能列表\n" +
                        "#开启动态推送 / #关闭动态推送\n" +
                        "#r [指定数字] / #骰子 [指定数字]\n" +
                        "#订阅 <UID> [16进制主题色]\n"+
                        "#删除 <UID>\n"+
                        "#订阅列表\n\n"+
                        "说明: <>内为必填, []为选填. 中间用空格隔开! 不要带括号!"
                )
                return
            }
            "#保存数据","#save" -> {
            }
        }

        if (content.contains("#r")||content.contains("#骰子")){
            try {
                val split = content.trim().split(" ")
                if(split.size==2){
                    if(split[1].toInt() in 1..6){
                        subject.sendMessage(Dice(split[1].toInt()))
                    }else{
                        throw Exception()
                    }
                }else{
                    throw Exception()
                }
            }catch (e:Exception){
                subject.sendMessage(Dice((1..6).random()))
                return
            }
        }else if (content.contains("#开启动态推送")){

            var qid = 0L
            var isGroup = true

            try {
                val split = content.trim().split(" ")
                if (split.size==1){
                    qid = subject.id
                    if (subject !is Group){
                        isGroup = false
                    }
                }else if(split.size==2){
                    if (PluginMain.bot.getGroup(split[1].toLong())!=null){
                    }else if(PluginMain.bot.getFriend(split[1].toLong())!=null){
                        isGroup = false
                    }else{
                        subject.sendMessage("QQ号错误")
                        return
                    }
                    qid = split[1].toLong()
                }else{
                    throw Exception()
                }
            }catch (e:Exception){
                subject.sendMessage("指令格式错误")
                return
            }

            if (!PluginData.friendList.contains(qid)&&!PluginData.groupList.contains(qid)) {
                if (isGroup){
                    PluginData.groupList[qid] = mutableListOf()
                }else{
                    PluginData.friendList[qid] = mutableListOf()
                }
            }
            subject.sendMessage("已开启(oﾟvﾟ)ノ")
            return
        }else if(content.contains("#关闭动态推送")){

            var qid = 0L
            var isGroup = true

            try {
                val split = content.trim().split(" ")
                if (split.size==1){
                    qid = subject.id
                    if (subject !is Group){
                        isGroup = false
                    }
                }else if(split.size==2){
                    if (PluginMain.bot.getGroup(split[1].toLong())!=null){
                    }else if(PluginMain.bot.getFriend(split[1].toLong())!=null){
                        isGroup = false
                    }else{
                        subject.sendMessage("QQ号错误")
                        return
                    }
                    qid = split[1].toLong()
                }else{
                    throw Exception()
                }
            }catch (e:Exception){
                subject.sendMessage("指令格式错误")
                return
            }

            try {
                if (PluginData.friendList.contains(qid)||PluginData.groupList.contains(qid)) {
                    if (isGroup){
                        PluginData.groupList.remove(qid)
                    }else{
                        PluginData.friendList.remove(qid)
                    }
                    val uids = mutableListOf<String>()
                    PluginData.followMemberGroup.forEach { (uid, u) ->
                        for(id in u){
                            if (id == qid){
                                u.remove(id)
                                break
                            }
                        }
                        if (PluginData.followMemberGroup[uid]?.size==0){
                            PluginData.followList.remove(uid)
                            uids.add(uid)
                            for (u in PluginData.userData){
                                if (u.uid==uid) {
                                    PluginData.userData.remove(u)
                                    break
                                }
                            }
                        }
                    }
                    for (uid in uids){
                        PluginData.followMemberGroup.remove(uid)
                    }
                }
                subject.sendMessage("已关闭(°ー°〃)")
            }catch (e:Exception){
                subject.sendMessage("关闭失败(°ー°〃)")
            }
            return
        }else if (content.contains("#订阅")||content.contains("#添加")||content.contains("#add")){
            var name = ""
            var uid = ""
            var hex = ""
            var qid = 0L
            var isGroup = true
            try{
                val split = content.trim().split(" ")
                uid = split[1]

                if (split.size==2){
                    qid = subject.id
                    if (subject !is Group){
                        isGroup = false
                    }
                }else if (split.size==3||split.size==4){
                    if (split[2].substring(0,1)=="#"){
                        hex = split[2]
                        qid = subject.id
                        if (subject !is Group){
                            isGroup = false
                        }
                    }else{
                        if (PluginMain.bot.getGroup(split[2].toLong())!=null){
                        }else if(PluginMain.bot.getFriend(split[2].toLong())!=null){
                            isGroup = false
                        }else{
                            subject.sendMessage("QQ号错误")
                            return
                        }
                        qid = split[2].toLong()
                        if (split.size==4){
                            hex = split[3]
                        }
                    }
                }else{
                    throw Exception()
                }
            }catch (e:Exception){
                subject.sendMessage("指令格式错误")
                return
            }

            if (!PluginData.friendList.contains(qid)&&!PluginData.groupList.contains(qid)) {
                subject.sendMessage("要使用动态推送请先回复 #开启动态推送")
                this.intercept()
                return
            }

            try {
                if (!PluginData.followMemberGroup[uid]!!.contains(qid)){
                    PluginData.followMemberGroup[uid]!!.add(qid)
                }
                if(hex==""){
                    PluginData.userData.forEach { item ->
                        if (item.uid == uid){
                            name = item.name
                            return@forEach
                        }
                    }
                    subject.sendMessage("添加 $name 成功\n( •̀ ω •́ )y")
                }else{
                    subject.sendMessage(Image(""+initFollowInfo(uid,User(),hex)))
                }
                return
            }catch (e:Exception){
                subject.sendMessage("添加并初始化信息中，请耐心等待...")
                try {
                    val user = User()
                    val image = initFollowInfo(uid,user,hex)
                    PluginData.userData.add(user)
                    name = user.name
                    if (!PluginData.followList.contains(uid)){
                        PluginData.followList.add(uid)
                    }
                    PluginData.followMemberGroup[uid] = mutableListOf(qid)
                    if (isGroup){
                        PluginData.groupList[qid]?.add("$uid@$name")
                    }else{
                        PluginData.friendList[qid]?.add("$uid@$name")
                    }

                    subject.sendMessage(Image(""+image)+"添加成功")
                }catch (e:Exception){
                    subject.sendMessage("添加 $uid 失败! 内部错误 或 uid错误\n")
                }
            }
            return
        }else if (content.contains("#删除")||content.contains("#del")){

            var uid = ""
            var name = ""
            var qid = 0L
            var isGroup = true

            try {
                val split = content.trim().split(" ")
                uid = split[1]
                if (split.size==2){
                    qid = subject.id
                    if (subject !is Group){
                        isGroup = false
                    }
                }else if(split.size==3){
                    if (PluginMain.bot.getGroup(split[2].toLong())!=null){
                    }else if(PluginMain.bot.getFriend(split[2].toLong())!=null){
                        isGroup = false
                    }else{
                        subject.sendMessage("QQ号错误")
                        return
                    }
                    qid = split[2].toLong()
                }else{
                    throw Exception()
                }
            }catch (e:Exception){
                subject.sendMessage("指令格式错误")
                return
            }

            if (!PluginData.friendList.contains(qid)&&!PluginData.groupList.contains(qid)) {
                subject.sendMessage("要使用动态推送请先回复 #开启动态推送")
                this.intercept()
                return
            }

            try {
                if (isGroup){
                    for (u in PluginData.groupList[qid]!!){
                        if (u.contains(uid)){
                            PluginData.groupList[qid]?.remove(u)
                            break
                        }
                    }
                }else{
                    for (u in PluginData.friendList[qid]!!){
                        if (u.contains(uid)){
                            PluginData.friendList[qid]?.remove(u)
                            break
                        }
                    }
                }
                PluginData.followMemberGroup[uid]?.remove(qid)
                if (PluginData.followMemberGroup[uid]?.size==0){
                    PluginData.followList.remove(uid)
                    PluginData.followMemberGroup.remove(uid)
                    for (u in PluginData.userData){
                        if (u.uid==uid) {
                            PluginData.userData.remove(u)
                            break
                        }
                    }
                }
                subject.sendMessage("删除 $uid 成功")
            }catch (e:Exception){
                subject.sendMessage("删除 $uid 失败! 内部错误 或 检查uid是否正确\n")
            }
            return
        }else if(content.contains("#订阅列表")||content.contains("#list")){

            var qid = 0L
            var isGroup = true

            try {
                val split = content.trim().split(" ")
                if (split.size==1){
                    qid = subject.id
                    if (subject !is Group){
                        isGroup = false
                    }
                }else if(split.size==2){
                    if (PluginMain.bot.getGroup(split[1].toLong())!=null){
                    }else if(PluginMain.bot.getFriend(split[1].toLong())!=null){
                        isGroup = false
                    }else{
                        subject.sendMessage("QQ号错误")
                        return
                    }
                    qid = split[1].toLong()
                }else{
                    throw Exception()
                }
            }catch (e:Exception){
                subject.sendMessage("指令格式错误")
                return
            }

            if (!PluginData.friendList.contains(qid)&&!PluginData.groupList.contains(qid)) {
                subject.sendMessage("要使用动态推送请先回复 #开启动态推送")
                this.intercept()
                return
            }

            try {
                var list = ""
                if (isGroup){
                    for (m in PluginData.groupList[qid]!!){
                        list += m.replace('@',' ')+"\n"
                    }
                }else{
                    for (m in PluginData.friendList[qid]!!){
                        list += m.replace('@',' ')+"\n"
                    }
                }

                subject.sendMessage(list)
            }catch (e:Exception){
                subject.sendMessage("无订阅")
            }
            return
        }
    }
}


//    bot.eventChannel.exceptionHandler { e ->
//        PluginMain.logger.error("检测失败")
//        Thread.sleep(20000)
//    }
