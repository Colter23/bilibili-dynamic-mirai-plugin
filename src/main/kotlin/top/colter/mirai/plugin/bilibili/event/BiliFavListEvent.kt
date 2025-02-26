package top.colter.mirai.plugin.bilibili.event

import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.CancellableEvent
import top.colter.mirai.plugin.bilibili.account.AccountUser
import top.colter.mirai.plugin.bilibili.account.BiliAccount


/**
 * B站收藏夹更新事件 (之后转移至视频下载插件中)
 * @param account 来源账号
 * @param fid 收藏夹ID
 * @param fname 收藏夹名称
 * @param action 操作
 */
public class BiliFavListEvent(
    override val account: AccountUser,
    public val fid: Long,
    public val fname: String,
    public val action: FavListActionMode,
//    public val
): BiliEvent, AbstractEvent(), CancellableEvent


/**
 * 收藏夹操作模式
 */
public enum class FavListActionMode {
    /**
     * 添加
     */
    ADD,

    /**
     * 删除
     */
    DELETE
}