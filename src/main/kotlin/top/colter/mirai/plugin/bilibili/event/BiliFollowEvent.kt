package top.colter.mirai.plugin.bilibili.event

import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.CancellableEvent
import top.colter.bilibili.data.user.BiliUser
import top.colter.mirai.plugin.bilibili.account.AccountUser


/**
 * B站关注事件
 * @param account 来源账号
 * @param action 操作模式
 * @param user 操作用户
 */
public class BiliFollowEvent(
    override val account: AccountUser,
    public val action: FollowActionMode,
    public val user: BiliUser
): BiliEvent, AbstractEvent(), CancellableEvent


/**
 * 操作模式
 */
public enum class FollowActionMode {
    /**
     * 关注
     */
    FOLLOW,

    /**
     * 取关
     */
    UNFOLLOW
}