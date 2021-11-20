package top.colter.miraiplugin.utils.translate

import top.colter.mirai.plugin.bilibili.PluginMain
import top.colter.mirai.plugin.bilibili.data.BiliPluginConfig
import top.colter.mirai.plugin.bilibili.utils.json
import top.colter.mirai.plugin.bilibili.utils.translate.TransResult
import top.colter.myplugin.translate.MD5

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
    if (BiliPluginConfig.baiduTranslate["enable"] == "true") {
        if (BiliPluginConfig.baiduTranslate["SECURITY_KEY"] != "") {
            var msg = text
            while (msg.indexOf('[') != -1) {
                msg = msg.replaceRange(msg.indexOf('['), msg.indexOf(']') + 1, "  ")
            }

            if (msg.contains(jp.toRegex()) || !msg.contains("[\u4e00-\u9fa5]".toRegex())) {
                try {
                    val api = TransApi(
                        BiliPluginConfig.baiduTranslate["APP_ID"]!!,
                        BiliPluginConfig.baiduTranslate["SECURITY_KEY"]!!
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
                    PluginMain.logger.error("Baidu translation failure! 百度翻译失败!")
                }
            } else {
                return null
            }
        } else {
            PluginMain.logger.error("Baidu translation API not configured! 未配置百度翻译API")
        }
    }
    return null
}

