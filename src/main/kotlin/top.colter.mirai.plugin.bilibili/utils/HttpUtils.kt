package top.colter.mirai.plugin.bilibili.utils

import kotlinx.serialization.json.JsonElement
import top.colter.mirai.plugin.bilibili.PluginMain
import top.colter.mirai.plugin.bilibili.data.ResultData
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class HttpUtils {

    private val cookie: String = PluginMain.sessData

    private var client: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(10000)).build()

    private val ua = listOf(
        "Mozilla/5.0 (Windows; U; MSIE 9.0; Windows NT 9.0; en-US);",
        "Mozilla/5.0 (compatible; MSIE 10.0; Macintosh; Intel Mac OS X 10_7_3; Trident/6.0)'",
        "Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; GTB7.4; InfoPath.2; SV1; .NET CLR 3.3.69573; WOW64; en-US)",
        "Opera/9.80 (X11; Linux i686; U; ru) Presto/2.8.131 Version/11.11",
        "Mozilla/5.0 (Windows NT 6.2; Win64; x64; rv:16.0.1) Gecko/20121011 Firefox/16.0.1",
        "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:15.0) Gecko/20100101 Firefox/15.0.1",
        "Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5355d Safari/8536.25"
    )

    private fun sendRequest(request: HttpRequest): JsonElement {
        val bodyHandler: HttpResponse.BodyHandler<String> = HttpResponse.BodyHandlers.ofString()
        val response: HttpResponse<String> = client.send(request, bodyHandler)
        val body: String = response.body()
        return json.parseToJsonElement(body)
    }

    fun get(url: String): JsonElement {
        val request: HttpRequest =
            HttpRequest.newBuilder(URI.create(url))
                .header("cookie", cookie)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("user-agent", ua.random())
                .GET().build()

        return sendRequest(request)
    }

    fun post(url: String, postBody: String): JsonElement {
        val request: HttpRequest =
            HttpRequest.newBuilder(URI.create(url))
                .header("cookie", cookie)
                .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                .header("user-agent", ua.random())
                .POST(HttpRequest.BodyPublishers.ofString(postBody)).build()

        return sendRequest(request)
    }

    inline fun <reified T> getAndDecode(url: String): T {
        val js = get(url).decode<ResultData>()
        if (js.code != 0) {
            throw Exception()
        }
        return js.data!!.decode()
    }

}