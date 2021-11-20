package top.colter.mirai.plugin.bilibili

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.console.util.CoroutineScopeUtils.childScope
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.selectMessagesUnit
import net.mamoe.mirai.utils.MiraiExperimentalApi

internal object Listener : CoroutineScope by PluginMain.childScope("Listener") {

    @OptIn(MiraiExperimentalApi::class)
    fun subscribe() {
        globalEventChannel().subscribeAlways<MyEvent> {
            val c = DynamicTasker.dynamic[uid]?.contacts?.get(subject)
            if (c == null) {
                message.sender.sendMessage("还没有订阅这个人哦")
                return@subscribeAlways
            }
            var cfgStr = "11"
            message.sender.sendMessage("检测动态内容  0:不检测动态  1:动态(包含视频)  2:仅视频  \n请回复 0 或 1 或 2")
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
                    message.sender.sendMessage("超时ಠಿ_ಠ")
                }
            }

            message.sender.sendMessage("是否检测直播  0:不检测  1:检测\n请回复 0 或 1")
            message.selectMessagesUnit {
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
    }

    fun stop() {
        coroutineContext.cancelChildren()
    }
}