package top.colter.mirai.plugin.bilibili.event

import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.CancellableEvent
import top.colter.mirai.plugin.bilibili.account.AccountUser
import top.colter.mirai.plugin.bilibili.data.DynamicMessage

/**
 * B站动态事件
 * @param account 来源账号
 * @param dynamic B站动态
 */
public class BiliDynamicEvent(
    override val account: AccountUser,
    public val message: DynamicMessage
): BiliEvent, AbstractEvent(), CancellableEvent