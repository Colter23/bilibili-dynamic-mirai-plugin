package top.colter.mirai.plugin.bilibili.utils.translate

import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.utils.json

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

var jp =
    "[ぁあぃいぅうぇえぉおかがきぎくぐけげこごさざしじすずせぜそぞただちぢっつづてでとどなにぬねのはばぱひびぴふぶぷへべぺほぼぽまみむめもゃやゅゆょよらりるれろゎわゐゑをんゔゕゖ゚゛゜ゝゞゟ゠ァアィイゥウェエォオカガキギクグケゲコゴサザシジスズセゼソゾタダチヂッツヅテデトドナニヌネノハバパヒビピフブプヘベペホボポマミムメモャヤュユョヨラリルレロヮワヰヱヲンヴヵヶヷヸヹヺ・ーヽヾヿ㍿]"

//文本翻译
fun trans(text: String): String? {
    if (BiliConfig.enableConfig.translateEnable) {
        if (BiliConfig.translateConfig.baidu.SECURITY_KEY != "") {
            var msg = text
            while (msg.indexOf('[') != -1) {
                msg = msg.replaceRange(msg.indexOf('['), msg.indexOf(']') + 1, "  ")
            }

            if (msg.contains(jp.toRegex()) || !msg.contains("[\u4e00-\u9fa5]".toRegex())) {
                try {
                    val api = TransApi(
                        BiliConfig.translateConfig.baidu.APP_ID,
                        BiliConfig.translateConfig.baidu.SECURITY_KEY
                    )
                    val resMsg = api.getTransResult(msg, "auto", "zh")
                    val transResult = resMsg?.let { json.parseToJsonElement(it) }
                        ?.let { json.decodeFromJsonElement(TransResult.serializer(), it) }
                    if (transResult?.from != "zh") {
                        return buildString {
                            for (item in transResult?.transResult!!) {
                                append(item.dst)
                                append("\n")
                            }
                        }
                    }
                } catch (e: Exception) {
                    BiliBiliDynamic.logger.error("Baidu translation failure! 百度翻译失败!")
                }
            } else {
                return null
            }
        } else {
            BiliBiliDynamic.logger.error("Baidu translation API not configured! 未配置百度翻译API")
        }
    }
    return null
}