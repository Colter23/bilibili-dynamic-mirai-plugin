package top.colter.mirai.plugin.bilibili.client

import top.colter.mirai.plugin.bilibili.api.getLoginUrl

class BiliLogin : BiliClient() {


    suspend fun getLoginQrCode() {
//        val res = useHttpClient {
//            it.get<LoginResult>(LOGIN_URL)
//        }
        val res = getLoginUrl()

        println(res)
    }

}