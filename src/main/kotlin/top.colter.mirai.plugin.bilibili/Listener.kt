package top.colter.mirai.plugin.bilibili

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.console.command.executeCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.CoroutineScopeUtils.childScope
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import top.colter.mirai.plugin.bilibili.command.DynamicCommand.add
import top.colter.mirai.plugin.bilibili.command.DynamicCommand.del

@OptIn(ConsoleExperimentalApi::class)
internal object Listener: CoroutineScope by PluginMain.childScope("Listener") {

    val ac = "/bili add <uid>  添加订阅\n/bili del <uid>  删除订阅\n/bili list  订阅列表\n/bili delAll  删除全部订阅\n< >为必填，不要加这个<>括号"

    val res = listOf<String>("(￣▽￣)\"","( •̀ ω •́ )✧","(oﾟvﾟ)ノ","(o゜▽゜)o☆","(。・ω・)ノ",
        "(੭ˊ꒳ˋ)੭✧","⸜(* ॑꒳ ॑*  )⸝✩°｡⋆","( •̀ ω •́ )y",
        "ヽ(✿ﾟ▽ﾟ)ノ","(๑•̀ㅂ•́)و✧"
    )

    @OptIn(MiraiExperimentalApi::class)
    fun subscribe() {
        globalEventChannel().subscribeMessages {
//            "hello" reply "hi"

        }
        globalEventChannel().subscribeAlways<NewFriendRequestEvent>{
            //自动同意好友申请
            accept()
        }
        globalEventChannel().subscribeAlways<BotInvitedJoinGroupRequestEvent>{
            //自动同意加群申请
            accept()
        }
        globalEventChannel().subscribeAlways<MemberJoinEvent>{
            //入群

        }

        globalEventChannel().subscribeAlways<MyEvent>{
            val c = DynamicTasker.dynamic[uid]?.contacts?.get(subject)
            if (c == null){
                message.sender.sendMessage("还没有订阅这个人哦")
                return@subscribeAlways
            }

            var cfgStr = "11"
            message.sender.sendMessage("检测动态内容  0:不检测动态  1:动态(包含视频)  2:仅视频  \n请回复 0 或 1 或 2")
            message.selectMessagesUnit{
                "0"{
                    cfgStr = "0${cfgStr[1]}"
                }
                "1"{
                    cfgStr = "1${cfgStr[1]}"
                }
                defaultReply { "失败(¬_¬ )" }
                timeout(30_000) {
                    message.sender.sendMessage("超时ಠಿ_ಠ")
                }
            }

            message.sender.sendMessage("是否检测直播  0:不检测  1:检测\n请回复 0 或 1")
            message.selectMessagesUnit{
                "0"{
                    cfgStr = "${cfgStr[0]}0"
                }
                "1"{
                    cfgStr = "${cfgStr[0]}1"
                }
                defaultReply { "失败(¬_¬ )" }
                timeout(30_000) { message.sender.sendMessage("超时ಠಿ_ಠ") }
            }

            DynamicTasker.mutex.withLock {
                DynamicTasker.dynamic[uid]?.contacts?.set(subject, cfgStr)
            }
            message.sender.sendMessage("配置结束")
        }

        globalEventChannel().subscribeAlways<FriendMessageEvent>{
            val content = message.content
            if (content == "?" || content == "？"){
                sender.sendMessage(ac)
            }

        }
        globalEventChannel().subscribeAlways<GroupMessageEvent>{

            val content = message.content
//            logger.info(content)
            val qq = Bot.instances[0].id

            if (content == "#r"){
                group.sendMessage(QuoteReply(source) + Dice((1..6).random()))
            }

//            val regex1 = Regex("""#每日总结 \d{1,2}[./-]\d{1,2}""")
//            val regex2 = Regex("""\d{2,4}.\d{1,2}.\d{1,2}总结""")
//            if (content.contains(regex1)){
//                logger.info("匹配成功")
//            }
//
            if (content.contains("@$qq")){
                val msg = content.replace("@$qq","").replace(" ","")
                if (msg == ""){
                    group.sendMessage(res[(res.indices).random()])
                }
            }

            if (content.contains("#无")){
                group.sendMessage(buildForwardMessage {
                    1109114769 named "【西城樹里P】携带鞋带" says "群里的肉我全包了"
                })
            }

            if (content.contains("#无中生有") && group.id == 391163028L){
                val sp = content.split(" ")
                var str = "无中生有瞒天过海"
                if (sp.size>1){
                    str = sp[1]
                }
                var adminList = group.members.filter { it.permission.level==2||it.permission.level==1 }
                group.sendMessage(buildForwardMessage {
                    for ((i,c) in str.withIndex()){
                        val user = adminList[i%adminList.size]
                        user.id named user.nick says c.toString()
                    }
                })
            }




//            val regex1 = Regex("""\d{1,2}.\d{1,2}总结""")
//            val regex2 = Regex("""\d{2,4}.\d{1,2}.\d{1,2}总结""")
//            if (content.contains(regex1) || content.contains(regex2)) {
//                val dateArr = content.substring(0, content.indexOf('总')).split('.').toMutableList()
//                var date = ""
//                if (dateArr.size == 2) {
//                    date += SimpleDateFormat("yyyy").format(System.currentTimeMillis())
//                } else if (dateArr.size == 3) {
//                    if (dateArr[0].length == 2) dateArr[0] = "20" + dateArr[0]
//                }
//                for (d in dateArr) {
//                    date += if (d.length == 1) "0$d"
//                    else d
//                }
//
//                try {
//                    val resImg : File = if (subject.id==831769633L){
//                        File("${PluginData.runPath}${PluginConfig.basePath}/img/koinoyasummary/$date.png")
//                    }else if(subject.id==583935896L){
//                        File("${PluginData.runPath}${PluginConfig.basePath}/img/hanakumosummary/$date.jpg")
//                    }else  if(subject.id==714376687L){
//                        File("${PluginData.runPath}${PluginConfig.basePath}/img/shiroseaoisummary/$date.jpg")
//                    }else{
//                        File("${PluginData.runPath}${PluginConfig.basePath}/img/summary/$date.jpg")
//                    }
//
//                    resImg.sendAsImageTo(subject)
//                } catch (e: Exception) {
//                    subject.sendMessage("没有找到此日的总结(*>﹏<*)\n格式: 2020.12.1总结 或 1.1总结\n注意: 搜索今年的可以不用加年份，往年的需要加年份(两位或四位都可)")
//                }
//            }

        }

    }

    fun stop()  {
        coroutineContext.cancelChildren()
    }
}