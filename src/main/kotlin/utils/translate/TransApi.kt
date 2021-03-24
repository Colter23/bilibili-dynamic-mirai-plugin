package top.colter.myplugin.translate

import java.util.HashMap

class TransApi(private val appid: String, private val securityKey: String) {
    fun getTransResult(query: String, from: String, to: String): String? {
        val params = buildParams(query, from, to)
        return HttpGet[TRANS_API_HOST, params]
    }

    private fun buildParams(query: String, from: String, to: String): Map<String?, String?> {
        val params: MutableMap<String?, String?> = HashMap()
        params["q"] = query
        params["from"] = from
        params["to"] = to
        params["appid"] = appid

        // 随机数
        val salt = System.currentTimeMillis().toString()
        params["salt"] = salt

        // 签名
        val src = appid + query + salt + securityKey // 加密前的原文
        params["sign"] = MD5.md5(src)
        return params
    }

    companion object {
        private const val TRANS_API_HOST = "http://api.fanyi.baidu.com/api/trans/vip/translate"
    }
}