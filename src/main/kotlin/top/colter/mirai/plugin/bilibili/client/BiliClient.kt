package top.colter.mirai.plugin.bilibili.client

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.isActive
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.serializer
import top.colter.mirai.plugin.bilibili.data.BiliCookie
import top.colter.mirai.plugin.bilibili.BiliDynamicConfig.enableConfig
import top.colter.mirai.plugin.bilibili.BiliDynamicConfig.proxy
import top.colter.mirai.plugin.bilibili.utils.isNotBlank
import top.colter.mirai.plugin.bilibili.utils.json

open class BiliClient : Closeable {
    override fun close() = clients.forEach { it.close() }

    val proxys = if (proxy.isNotBlank()) {
        mutableListOf<ProxyConfig>().apply {
            proxy.forEach {
                if (it != "") {
                    add(ProxyBuilder.http(it))
                }
            }
        }
    } else {
        null
    }


    val clients = MutableList(3) { client() }

    var cookie: BiliCookie? = null

    protected fun client() = HttpClient(OkHttp) {
        defaultRequest {
            header(HttpHeaders.Origin, "https://t.bilibili.com")
            header(HttpHeaders.Referrer, "https://t.bilibili.com")
        }
        install(HttpTimeout) {
            socketTimeoutMillis = 10_000L
            connectTimeoutMillis = 10_000L
            requestTimeoutMillis = 10_000L
        }
        BrowserUserAgent()
    }

    suspend inline fun <reified T> get(url: String, crossinline block: HttpRequestBuilder.() -> Unit = {}): T =
        json.decodeFromString(json.serializersModule.serializer(), useHttpClient {
            it.get(url) {
                header(HttpHeaders.Cookie, cookie)
                block()
            }
        })

    private var clientIndex = 0
    private var proxyIndex = 0

    suspend fun <T> useHttpClient(block: suspend (HttpClient) -> T): T = supervisorScope {
        while (isActive) {
            try {
                val client = clients[clientIndex]
                if (proxys != null && enableConfig.proxyEnable) {
                    client.engineConfig.proxy = proxys[proxyIndex]
                    proxyIndex = (proxyIndex + 1) % proxys.size
                }
                return@supervisorScope block(client)
            } catch (throwable: Throwable) {
                if (isActive) {
                    clientIndex = (clientIndex + 1) % clients.size
                } else {
                    throw throwable
                }
            }
        }
        throw CancellationException()
    }

}