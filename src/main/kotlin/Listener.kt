package top.colter.mirai.plugin

import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import top.colter.mirai.plugin.bean.Command
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
                            "#添加 <UID> [群/Q号] [16进制主题色]\n"+
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

        if (!PluginConfig.botState){
            return
        }
        if (!(subject is Group && subject.id == PluginConfig.adminGroup)){
            if (subject is Group&&PluginConfig.group["enable"]=="false"){
                return
            }
            if (subject !is Group&&PluginConfig.friend["enable"]=="false"){
                return
            }
        }

        if (content.substring(0,1)=="#"){
            val commandArr = content.trim().split(" ")
            val commandName = commandArr[0].substring(1)
            when(commandName){
                "?", "？", "help", "帮助", "功能", "菜单" -> {
                    if (PluginConfig.adminGroup==subject.id){
                        return
                    }
                    subject.sendMessage(
                        "#? 或 #help 或 #帮助 : 功能列表\n" +
                            "#开启动态推送 / #关闭动态推送\n" +
                            "#r [指定数字] / #骰子 [指定数字]\n" +
                            "#添加 <UID> [16进制主题色]\n"+
                            "#删除 <UID>\n"+
                            "#订阅列表\n\n"+
                            "说明: <>内为必填, []为选填. 中间用空格隔开! 不要带括号!"
                    )
                    return
                }
                "r", "骰子" -> {
                    try {
                        if(commandArr.size==2 && commandArr[1].toInt() in 1..6){
                            subject.sendMessage(Dice(commandArr[1].toInt()))
                            return
                        }
                        subject.sendMessage(Dice((1..6).random()))
                        return
                    }catch (e:Exception){
                        subject.sendMessage("ERROR! "+e.message)
                        return
                    }
                }
                "开启动态推送" -> {
                    val command = resolveCommand(content, "#开启动态推送 [qid]", subject)
                    if (command==null){
                        subject.sendMessage("命令错误")
                        return
                    }
                    if (!PluginData.friendList.contains(command.qid)&&!PluginData.groupList.contains(command.qid)) {
                        if (command.isGroup){
                            PluginData.groupList[command.qid] = mutableListOf()
                        }else{
                            PluginData.friendList[command.qid] = mutableListOf()
                        }
                    }
                    subject.sendMessage("已开启(oﾟvﾟ)ノ")
                    return
                }
                "关闭动态推送" -> {
                    val command = resolveCommand(content, "#关闭动态推送 [qid]", subject)
                    if (command==null){
                        subject.sendMessage("命令错误")
                        return
                    }
                    try {
                        if (PluginData.friendList.contains(command.qid)||PluginData.groupList.contains(command.qid)) {
                            if (command.isGroup){
                                PluginData.groupList.remove(command.qid)
                            }else{
                                PluginData.friendList.remove(command.qid)
                            }
                            val uids = mutableListOf<String>()
                            PluginData.followMemberGroup.forEach { (uid, u) ->
                                for(id in u){
                                    if (id == command.qid){
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
                }
                "订阅", "添加", "add" -> {

                    val command = resolveCommand(content, "#订阅 <uid> [qid] [hex]", subject)
                    if (command==null){
                        subject.sendMessage("命令错误")
                        return
                    }

                    if (!PluginData.friendList.contains(command.qid)&&!PluginData.groupList.contains(command.qid)) {
                        subject.sendMessage("要使用动态推送请先回复 #开启动态推送")
                        this.intercept()
                        return
                    }
                    var name = ""
                    try {
                        PluginData.userData.forEach { item ->
                            if (item.uid == command.uid){
                                name = item.name
                                return@forEach
                            }
                        }
                        if (!PluginData.followMemberGroup[command.uid]!!.contains(command.qid)){
                            PluginData.followMemberGroup[command.uid]!!.add(command.qid)
                            if (command.isGroup){
                                PluginData.groupList[command.qid]?.add("${command.uid}@$name")
                            }else{
                                PluginData.friendList[command.qid]?.add("${command.uid}@$name")
                            }
                        }
                        if(command.hex==""){
                            subject.sendMessage("添加 $name 成功\n( •̀ ω •́ )y")
                        }else{
                            try{
                                subject.sendMessage(Image(""+initFollowInfo(command.uid,User(),command.hex)))
                            }catch (e:Exception){
                                subject.sendMessage("添加 ${command.uid} 失败! 内部错误 或 uid错误\n")
                            }
                        }
                        return
                    }catch (e:Exception){
                        subject.sendMessage("添加并初始化信息中，请耐心等待...")
                        try {
                            val user = User()
                            val image = initFollowInfo(command.uid,user,command.hex)
                            PluginData.userData.add(user)
                            name = user.name
                            if (!PluginData.followList.contains(command.uid)){
                                PluginData.followList.add(command.uid)
                            }
                            PluginData.followMemberGroup[command.uid] = mutableListOf(command.qid)
                            if (command.isGroup){
                                PluginData.groupList[command.qid]?.add("${command.uid}@$name")
                            }else{
                                PluginData.friendList[command.qid]?.add("${command.uid}@$name")
                            }

                            subject.sendMessage(Image(""+image)+"添加成功")
                        }catch (e:Exception){
                            subject.sendMessage("添加 ${command.uid} 失败! 内部错误 或 uid错误\n")
                        }
                    }
                    return
                }
                "删除", "del" -> {
                    val command = resolveCommand(content, "#删除 <uid> [qid]", subject)
                    if (command==null){
                        subject.sendMessage("命令错误")
                        return
                    }
                    if (!PluginData.friendList.contains(command.qid)&&!PluginData.groupList.contains(command.qid)) {
                        subject.sendMessage("要使用动态推送请先回复 #开启动态推送")
                        this.intercept()
                        return
                    }
                    try {
                        if (command.isGroup){
                            for (u in PluginData.groupList[command.qid]!!){
                                if (u.split("@")[0]==command.uid){
                                    PluginData.groupList[command.qid]?.remove(u)
                                    break
                                }
                            }
                        }else{
                            for (u in PluginData.friendList[command.qid]!!){
                                if (u.split("@")[0]==command.uid){
                                    PluginData.friendList[command.qid]?.remove(u)
                                    break
                                }
                            }
                        }
                        PluginData.followMemberGroup[command.uid]?.remove(command.qid)
                        if (PluginData.followMemberGroup[command.uid]?.size==0){
                            PluginData.followList.remove(command.uid)
                            PluginData.followMemberGroup.remove(command.uid)
                            for (u in PluginData.userData){
                                if (u.uid==command.uid) {
                                    PluginData.userData.remove(u)
                                    break
                                }
                            }
                        }
                        subject.sendMessage("删除 ${command.uid} 成功")
                    }catch (e:Exception){
                        subject.sendMessage("删除 ${command.uid} 失败! 内部错误 或 检查uid是否正确\n")
                    }
                    return
                }
                "订阅列表", "list" -> {
                    val command = resolveCommand(content, "#订阅列表 [qid]", subject)
                    if (command==null){
                        subject.sendMessage("命令语法错误")
                        return
                    }
                    if (!PluginData.friendList.contains(command.qid)&&!PluginData.groupList.contains(command.qid)) {
                        subject.sendMessage("要使用动态推送请先回复 #开启动态推送")
                        this.intercept()
                        return
                    }
                    try {
                        var list = ""
                        if (command.isGroup){
                            for (m in PluginData.groupList[command.qid]!!){
                                list += m.replace('@',' ')+"\n"
                            }
                        }else{
                            for (m in PluginData.friendList[command.qid]!!){
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
    }
}

/**
 * 解析命令
 */
fun resolveCommand(rawCommand:String,commandTemplate:String,subject: Contact): Command? {
    val command = Command()

    val commandArr = rawCommand.trim().split(" ")
    val templateArr = commandTemplate.trim().split(" ")
//    val requireParm = mutableListOf<String>()
//    val optionalParm = mutableListOf<String>()
//
//    for (parm in templateArr){
//        if (parm.substring(0,1)=="<"){
//            requireParm.add(parm.substring(1,parm.length-2))
//        }else if (parm.substring(0,1)=="["){
//            optionalParm.add(parm.substring(1,parm.length-2))
//        }
//    }

    command.commandName = commandArr[0].substring(1)
    command.qid = subject.id
    command.isGroup = subject is Group

    if (commandArr.size==1){
        return command
    }
    if (templateArr.size==2){
        if (PluginMain.bot.getGroup(commandArr[1].toLong())!=null){
            command.isGroup = true
        }else if(PluginMain.bot.getFriend(commandArr[1].toLong())!=null){
            command.isGroup = false
        }else{
            return null
        }
        command.qid = commandArr[1].toLong()
        return command
    }else if(templateArr.size==3){
        command.uid = commandArr[1]
        if (commandArr.size==2){
            return command
        }else if (commandArr.size==3){
            if (PluginMain.bot.getGroup(commandArr[2].toLong())!=null){
                command.isGroup = true
            }else if(PluginMain.bot.getFriend(commandArr[2].toLong())!=null){
                command.isGroup = false
            }else{
                return null
            }
            command.qid = commandArr[2].toLong()
            return command
        }else {
            return null
        }
    }else if(templateArr.size==4){
        command.uid = commandArr[1]
        if (commandArr.size==2){
            return command
        }else if (commandArr.size==3){
            if (commandArr[2].substring(0,1)=="#"){
                command.hex = commandArr[2]
                return command
            }
            if (PluginMain.bot.getGroup(commandArr[2].toLong())!=null){
                command.isGroup = true
            }else if(PluginMain.bot.getFriend(commandArr[2].toLong())!=null){
                command.isGroup = false
            }else{
                return null
            }
            command.qid = commandArr[2].toLong()
            return command
        }else if (commandArr.size==4){
            if (PluginMain.bot.getGroup(commandArr[2].toLong())!=null){
                command.isGroup = true
            }else if(PluginMain.bot.getFriend(commandArr[2].toLong())!=null){
                command.isGroup = false
            }else{
                return null
            }
            command.qid = commandArr[2].toLong()
            command.hex = commandArr[3]
            return command
        }else {
            return null
        }
    }
    return null
}

//    bot.eventChannel.exceptionHandler { e ->
//        PluginMain.logger.error("检测失败")
//        Thread.sleep(20000)
//    }
