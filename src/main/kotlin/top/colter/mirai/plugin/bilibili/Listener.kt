package top.colter.mirai.plugin.bilibili

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.CoroutineScopeUtils.childScope
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.selectMessagesUnit
import net.mamoe.mirai.utils.MiraiExperimentalApi

@OptIn(ConsoleExperimentalApi::class)
internal object Listener : CoroutineScope by PluginMain.childScope("Listener") {

    @OptIn(MiraiExperimentalApi::class)
    fun subscribe() {
//        globalEventChannel().subscribeAlways<MessageEvent> {
//            message.filterIsInstance<At>().map { it.target }.first { it == bot.id }.let { _ ->
//                println("At")
//                if (message.content.contains("下载图片")){
//                    println("下载图片")
//
//                    message[QuoteReply.Key]?.source?.originalMessage?.content.let { quote ->
//                        val dynamicRegex = """(?<=t\.bilibili\.com/)(\d+)""".toRegex()
//                        val videoRegex = """((av|AV)\d+|BV[0-9A-z]{8,12})""".toRegex()
//                        dynamicRegex.find(quote!!)?.value.let {
//                            println("动态：$it")
//                            val dynamic = DynamicTasker.httpUtils.getAndDecode<DynamicDetail>(DYNAMIC_DETAIL).dynamic
//                             if (dynamic?.type == DynamicType.DELETE)
//                        }
//                        videoRegex.find(quote)?.value.let{
//                            println("视频：$it")
//                        }
//                    }
//                }
//            }
//        }

        globalEventChannel().subscribeAlways<MyEvent> {
            val c = DynamicTasker.dynamic[uid]?.contacts?.get(subject)
            if (c == null) {
                message.subject.sendMessage("还没有订阅这个人哦")
                return@subscribeAlways
            }
            var cfgStr = "11"
            message.subject.sendMessage("检测动态内容  0:不检测动态  1:动态(包含视频)  2:仅视频  \n请回复 0 或 1 或 2")
            message.selectMessagesUnit {
                "0"{
                    cfgStr = "0${cfgStr[1]}"
                }
                "1"{
                    cfgStr = "1${cfgStr[1]}"
                }
                "2"{
                    cfgStr = "2${cfgStr[1]}"
                }
                defaultReply { "失败(¬_¬ )" }
                timeout(30_000) {
                    message.subject.sendMessage("超时ಠಿ_ಠ")
                }
            }

            message.subject.sendMessage("是否检测直播  0:不检测  1:检测\n请回复 0 或 1")
            message.selectMessagesUnit {
                "0"{
                    cfgStr = "${cfgStr[0]}0"
                }
                "1"{
                    cfgStr = "${cfgStr[0]}1"
                }
                defaultReply { "失败(¬_¬ )" }
                timeout(30_000) { message.subject.sendMessage("超时ಠಿ_ಠ") }
            }

            DynamicTasker.mutex.withLock {
                DynamicTasker.dynamic[uid]?.contacts?.set(subject, cfgStr)
            }
            message.subject.sendMessage("配置结束")
        }
    }

    fun stop() {
        coroutineContext.cancelChildren()
    }
}