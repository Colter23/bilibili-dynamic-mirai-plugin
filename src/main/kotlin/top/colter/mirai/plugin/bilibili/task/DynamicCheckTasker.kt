package top.colter.mirai.plugin.bilibili.task

import top.colter.bilibili.api.getNewDynamic
import top.colter.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.account.AccountUser
import top.colter.mirai.plugin.bilibili.database.BiliConfig
import top.colter.mirai.plugin.bilibili.event.BiliDynamicEvent
import top.colter.mirai.plugin.bilibili.filter.DynamicFilter

class DynamicCheckTasker(val account: AccountUser) : BiliCheckTasker("DynamicCheck") {

    override var interval = BiliConfig.checkConfig.interval

//    private val dynamicChannel by BiliBiliDynamic::dynamicChannel
//
//    private val dynamic by BiliData::dynamic
//    private val bangumi by BiliData::bangumi

//    private val user by BiliUser::user
//    private val season by BiliSeason::season

    private val listenAllDynamicMode = false

    val client = BiliClient()

    override suspend fun main() {
        val dynamicList = client.getNewDynamic()
        if (dynamicList != null) {
//            val followingUsers = dynamic.filter { it.value.contacts.isNotEmpty() }.map { it.key }
//            val subUser = user.values.filter { it.subscribeCount > 0 }.map { it.mid }
//            val subSeason = season.map { it.ssid.toString() }
//            val dynamics = dynamicList.items
//                .asSequence()
//                .filter {
//                    !banType.contains(it.type)
//                }.filter {
//                    it.time > lastDynamic
//                }.filter {
//                    !historyDynamic.contains(it.id)
//                }.filter {
//                    if (it.type == BiliDynamicType.PGC)
//                        subSeason.contains(it.owner.mid)
//                    else subUser.contains(it.owner.mid)
//                }.sortedBy {
//                    it.time
//                }.toList()

//            dynamics.forEach {
//
//                BiliDynamicEvent(account, it)
//
//                historyDynamic.add(lastIndex, it.id)
//                lastIndex++
//                if (lastIndex >= capacity) lastIndex = 0
//            }

            val dynamicMap = DynamicFilter.filter(dynamicList.items.sortedBy { it.time })

            dynamicMap.forEach {
                BiliDynamicEvent(account, it)
            }

//            for (d in dynamics) {
//                val c = d.getContact()
//                if (c != null) {
//                    val m = d.buildMessage()
//
//                }
//            }


            // Map<String, Map<Contact, Config>> // biliuid TO qqusers
            // Map<String, BiliMessage>

            // Map<Contact, List<Message>>  // qquser TO msg




            //if (dynamics.isNotEmpty()) lastDynamic = dynamics.last().time
//            dynamicChannel.sendAll(dynamics.map { DynamicDetail(it) })
        }
    }

}