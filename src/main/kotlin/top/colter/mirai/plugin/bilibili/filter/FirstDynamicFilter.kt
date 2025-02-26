package top.colter.mirai.plugin.bilibili.filter

import top.colter.bilibili.data.dynamic.BiliDynamic
import top.colter.bilibili.data.dynamic.type.BiliDynamicType
import top.colter.mirai.plugin.bilibili.database.BiliSeason
import top.colter.mirai.plugin.bilibili.database.BiliUserData
import top.colter.mirai.plugin.bilibili.database.BlackWhiteList
import java.time.Instant


/**
 * 初级过滤器，用来过滤动态
 *
 * 通过 object 实现, 在 [DynamicFilter] 中被调用
 *
 * **true** 通过
 * **false** 拒绝
 */
sealed interface FirstDynamicFilter {
    val priority: Int

    fun filter(dynamic: BiliDynamic): Boolean
}


/**
 * 时间过滤器
 */
object TimeFilter: FirstDynamicFilter {
    override val priority: Int get() = 1

    private val startTime = Instant.now().epochSecond

    override fun filter(dynamic: BiliDynamic): Boolean {
        return if (dynamic.time < startTime) false
        else dynamic.time >= (BiliUserData.user[dynamic.mid]?.laseUpdate ?: 0L)
    }
}

/**
 * 订阅过滤器
 */
object SubscribeFilter: FirstDynamicFilter {
    override val priority: Int get() = 10

    private val user by BiliUserData::user

    override fun filter(dynamic: BiliDynamic): Boolean {
        return user.containsKey(dynamic.mid)
    }
}


/**
 * 类型过滤器
 */
object DynamicTypeFilter: FirstDynamicFilter {
    override val priority: Int get() = 20

    private val banType = listOf(
        BiliDynamicType.LIVE
    )

    override fun filter(dynamic: BiliDynamic): Boolean {
        return !banType.contains(dynamic.type)
    }
}

/**
 * 番剧过滤器 TODO("番剧")
 */
object SeasonFilter: FirstDynamicFilter {
    override val priority: Int get() = 30

    private val season by BiliSeason::season

    override fun filter(dynamic: BiliDynamic): Boolean {
        val subSeason = season.map { it.ssid.toLong() }
        return if (dynamic.type == BiliDynamicType.PGC)
            subSeason.contains(dynamic.mid)
        else true
    }
}

/**
 * 全局黑白名单过滤 [BlackWhiteList.global]
 */
object BlackWhiteListFilter: FirstDynamicFilter {
    override val priority: Int get() = 40

    private val globalConfig by BlackWhiteList::global

    override fun filter(dynamic: BiliDynamic): Boolean {
        return globalConfig.biliUser.contains(dynamic.mid).let {
            if (globalConfig.isBlackList) !it else it
        }
    }
}


/**
 * 历史过滤器 (防止重复推送)
 */
object HistoryFilter: FirstDynamicFilter {
    override val priority: Int get() = 50

    private const val capacity = 300
    private val historyDynamic = ArrayList<Long>(capacity)
    private var lastIndex = 0

    override fun filter(dynamic: BiliDynamic): Boolean {
        if (historyDynamic.contains(dynamic.id)) return false

        historyDynamic.add(lastIndex, dynamic.id)
        lastIndex++
        if (lastIndex >= capacity) lastIndex = 0

        return true
    }
}