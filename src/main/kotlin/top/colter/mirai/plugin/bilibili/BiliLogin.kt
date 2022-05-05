package top.colter.mirai.plugin.bilibili

import io.ktor.client.request.*
import top.colter.mirai.plugin.bilibili.api.LOGIN_URL
import top.colter.mirai.plugin.bilibili.api.getLoginUrl
import top.colter.mirai.plugin.bilibili.data.LoginResult

class BiliLogin: BiliClient() {


    suspend fun getLoginQrCode(){
//        val res = useHttpClient {
//            it.get<LoginResult>(LOGIN_URL)
//        }
        val res = getLoginUrl()

        println(res)
    }

}