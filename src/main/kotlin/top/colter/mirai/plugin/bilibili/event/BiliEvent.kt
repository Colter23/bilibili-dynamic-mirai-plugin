package top.colter.mirai.plugin.bilibili.event

import net.mamoe.mirai.event.Event
import top.colter.mirai.plugin.bilibili.account.AccountUser
import top.colter.mirai.plugin.bilibili.account.BiliAccount

/**
 * B站相关事件
 */
public interface BiliEvent: Event {
    /**
     * ~~事件来源账号(这个账号是本地绑定的账号)~~
     */
    public val account: AccountUser
}