package top.colter.mirai.plugin.bilibili.api

import io.ktor.client.request.*
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.BiliResult
import top.colter.mirai.plugin.bilibili.utils.actionNotify
import top.colter.mirai.plugin.bilibili.utils.decode

fun twemoji(code: String) = "$TWEMOJI/$code.png"

private var isLogin = true

internal suspend inline fun <reified T> BiliClient.getData(
    url: String,
    crossinline block: HttpRequestBuilder.() -> Unit = {}
): T? {
    val res = get<BiliResult>(url, block)

    return if (res.code == -101) {
        if (isLogin) actionNotify("账号登录失效，请使用 /bili login 重新登录")
        isLogin = false
        throw Exception("账号登录失效，请使用 /bili login 重新登录")
    } else if (res.code != 0 || res.data == null) {
        throw Exception("URL: $url ${res.message}")
    } else {
        isLogin = true
        res.data.decode()
    }
}
