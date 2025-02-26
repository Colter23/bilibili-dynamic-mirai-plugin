package top.colter.mirai.plugin.bilibili.account

import net.mamoe.mirai.console.plugin.id
import okhttp3.internal.toImmutableMap
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import kotlin.reflect.KProperty

public interface BiliApplication {
    public val id: String
    public val name: String
    public val description: String

    public val accounts: List<BiliAccount>

}

public object BiliApplicationManager {

    private val appMap: MutableMap<String, BiliApplication> = mutableMapOf()

    public val apps: Map<String, BiliApplication> get() = appMap.toImmutableMap()

    public fun register(app: BiliApplication) {
        if (app.id !in appMap) appMap[app.id] = app
    }

}

//object DynamicBiliApplication: BiliApplication {
//    override val id: String = BiliBiliDynamic.id
//    override val name: String = "动态检测"
//    override val description: String = "检测B站动态"
//}