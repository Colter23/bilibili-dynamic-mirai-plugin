package top.colter.mirai.plugin.bilibili.filter

import top.colter.bilibili.data.dynamic.BiliDynamic
import top.colter.mirai.plugin.bilibili.data.DynamicMessage
import top.colter.mirai.plugin.bilibili.data.Receiver


//sealed interface DynamicFilter {
//    companion object {
//        private val filterList = DynamicFilter::class.sealedSubclasses
//
//        fun filter(dynamics: List<BiliDynamic>): List<BiliDynamic>{ // List<Pair<BiliDynamic, List<String>>>
//            var ds = dynamics
//            filterList.forEach {
//                ds = it.objectInstance?.filter(ds)!!
//            }
//            return ds
//        }
//    }
////    fun filter(dynamics: List<BiliDynamic>): List<BiliDynamic>
//}

object DynamicFilter {

    private val firstFilters: List<FirstDynamicFilter> by lazy {
        FirstDynamicFilter::class.sealedSubclasses.mapNotNull { it.objectInstance }.sortedBy { it.priority }
    }
    private val secondFilters: List<SecondDynamicFilter> by lazy {
        SecondDynamicFilter::class.sealedSubclasses.mapNotNull { it.objectInstance }.sortedBy { it.priority }
    }



//    private val firstFilters = FirstDynamicFilter::class.sealedSubclasses
//    private val secondFilters = SecondDynamicFilter::class.sealedSubclasses

//    fun filter(dynamics: List<BiliDynamic>): List<Pair<BiliDynamic, Set<String>>>{ // List<Pair<BiliDynamic, List<String>>>
//        val firstDynamics = dynamics.filter { dynamic ->
//            firstFilters.all { it.objectInstance?.filter(dynamic)!! }
//        }.map { Pair(it, setOf<String>()) }
//
//        val secondDynamics = firstDynamics.map { pair ->
//            var temp = pair
//            secondFilters.forEach { temp = it.objectInstance?.filter(pair)!! }
//            temp
//        }
//        return secondDynamics.filter { it.second.isNotEmpty() }
//    }
    // Map<BiliDynamic, Set<Receiver>>
    fun filter(dynamics: List<BiliDynamic>): List<DynamicMessage>{ // List<Pair<BiliDynamic, List<String>>>
//        val firstDynamics = dynamics.filter { dynamic ->
//            firstFilters.all { it.objectInstance!!.filter(dynamic) }
//        }
        var firstDynamics = dynamics
        firstFilters.forEach { filter ->
            firstDynamics = firstDynamics.filter { filter.filter(it) }
        }
        val secondDynamics = firstDynamics.associateWith { dynamic ->
            var tempReceivers = setOf<Receiver>()
            secondFilters.forEach { tempReceivers = it.filter(dynamic, tempReceivers) }
            tempReceivers
        }
        return secondDynamics.filter { it.value.isNotEmpty() }.map {
            DynamicMessage(it.key, it.value)
        }
    }

}

