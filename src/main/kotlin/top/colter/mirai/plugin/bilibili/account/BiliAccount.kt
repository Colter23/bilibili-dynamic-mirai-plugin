package top.colter.mirai.plugin.bilibili.account

import net.mamoe.mirai.contact.User
import top.colter.bilibili.client.BiliCookiesStorage
import top.colter.bilibili.data.LazyImage
import top.colter.bilibili.data.user.*
import kotlin.reflect.KProperty


public interface BiliAccount {
//    public val name: String
//    public val uid: String
    public val info: AccountUser
    public val owner: User
    public val cookie: BiliCookiesStorage
    public val authApps: List<BiliApplication>

}

data class AccountUser(
    override val mid: Long,
    override val name: String,
    override val face: LazyImage,
    override val pendant: Pendant?,
    override val official: OfficialVerify?,
    override val decorate: Decorate?,
    val vip: BiliVip,
    val level: Int,
    val coins: Int
): BiliUser

//public data class SimpleBiliAccount(
//    override val name: String,
//    override val uid: String
//): BiliAccount
//
//public data class AuthBiliAccount(
//    override val name: String,
//    override val uid: String,
//    private val cookie: Cookie
//): BiliAccount

object BiliAccountManager {
    private val accounts: MutableMap<String, BiliAccount> = mutableMapOf()

    public fun allAuthAccount(app: BiliApplication): List<BiliAccount> {
        return accounts.filter { app in it.value.authApps }.values.toList()
    }

}

class AccountManager {
    operator fun getValue(thisRef: BiliApplication?, property: KProperty<*>): List<BiliAccount> {
        if (thisRef == null) throw Exception("获取失败，请按照规范获取")
        return BiliAccountManager.allAuthAccount(thisRef)
    }
}


