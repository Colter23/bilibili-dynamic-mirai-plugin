package top.colter.mirai.plugin.bilibili.event

import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.CancellableEvent
import top.colter.bilibili.data.dynamic.BiliDynamic
import top.colter.mirai.plugin.bilibili.account.AccountUser


/**
 * B站番剧事件
 * @param account 来源账号
 * @param dynamic B站番剧动态
 */
public class BiliSeasonEvent(
    override val account: AccountUser,
    public val dynamic: BiliDynamic
): BiliEvent, AbstractEvent(), CancellableEvent