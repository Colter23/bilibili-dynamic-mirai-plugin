package top.colter.mirai.plugin.bilibili.service

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeout
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImageTo
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic.save
import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.api.getLoginQrcode
import top.colter.mirai.plugin.bilibili.api.loginInfo
import top.colter.mirai.plugin.bilibili.data.BiliCookie
import top.colter.mirai.plugin.bilibili.draw.loginQrCode
import top.colter.mirai.plugin.bilibili.initTagid
import kotlin.time.Duration.Companion.milliseconds

object LoginService {
    suspend fun login(contact: Contact) {
        val loginData = client.getLoginQrcode()!!

        val image = loginQrCode(loginData.url)
        val qrMsg = image.encodeToData()!!.bytes.toExternalResource().toAutoCloseable().sendAsImageTo(contact)
        val loginMsg = contact.sendMessage("请使用BiliBili手机APP扫码登录 3分钟有效")
        runCatching {
            withTimeout(180000.milliseconds) {
                while (isActive) {
                    delay(3000.milliseconds)
                    val loginInfo = client.loginInfo(loginData.qrcodeKey!!)!!
                    if (loginInfo.code == 0) {
                        // Cookies are captured from the poll response's Set-Cookie headers.
                        val cookie = loginInfo.cookie
                        val biliCookie = BiliCookie().parse(cookie)
                        check(biliCookie.sessData.isNotBlank() && biliCookie.biliJct.isNotBlank()) {
                            "登录响应中缺少 SESSDATA 或 bili_jct"
                        }
                        BiliConfig.accountConfig.cookie = cookie
                        BiliConfig.save()
                        BiliBiliDynamic.cookie = biliCookie
                        initTagid()
                        //getHistoryDynamic()
                        contact.sendMessage("登录成功!")
                        break
                    }
                }
            }
        }.onFailure {
            contact.sendMessage("登录失败 ${it.message}")
        }
        try {
            qrMsg.recall()
            loginMsg.recall()
        }catch (_: Throwable) {}
    }

}
