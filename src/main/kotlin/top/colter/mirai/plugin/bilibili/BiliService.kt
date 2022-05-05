package top.colter.mirai.plugin.bilibili

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import top.colter.mirai.plugin.bilibili.data.DynamicItem
import kotlin.coroutines.CoroutineContext

object BiliService: CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.IO + CoroutineName("DynamicTasker")

    val dynamicChannel = Channel<DynamicItem>(20)


    suspend fun dynamicCheck() {

//        dynamicChannel.send(DynamicItem())

    }

    suspend fun dynamicHandle(dynamic: DynamicItem){
        dynamic.modules.moduleDynamic
    }


}