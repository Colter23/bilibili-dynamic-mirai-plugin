package top.colter.mirai.plugin.bilibili.event

import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.CancellableEvent
import top.colter.mirai.plugin.bilibili.account.AccountUser
import top.colter.mirai.plugin.bilibili.account.BiliAccount
import top.colter.mirai.plugin.bilibili.data.BiliMessage

/**
 * B站消息事件 (之后转移至B站管理插件中)
 * @param account 来源账号
 * @param message B站消息
 */
public class BiliMessageEvent(
    override val account: AccountUser,
    public val message: BiliMessage
): BiliEvent, AbstractEvent(), CancellableEvent

