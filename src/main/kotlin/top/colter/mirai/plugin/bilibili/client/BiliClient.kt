package top.colter.mirai.plugin.bilibili.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.isActive
import kotlinx.coroutines.supervisorScope
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.BiliConfig.enableConfig
import top.colter.mirai.plugin.bilibili.BiliConfig.proxyConfig
import top.colter.mirai.plugin.bilibili.utils.decode
import top.colter.mirai.plugin.bilibili.utils.isNotBlank
import top.colter.mirai.plugin.bilibili.utils.json
import java.io.IOException

open class BiliClient : Closeable {
    override fun close() = clients.forEach { it.close() }

    val proxys = if (proxyConfig.proxy.isNotBlank()) {
        mutableListOf<ProxyConfig>().apply {
            proxyConfig.proxy.forEach {
                if (it != "") {
                    add(ProxyBuilder.http(it))
                }
            }
        }
    } else {
        null
    }

    val clients = MutableList(3) { client() }

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
        expectSuccess = true
        install(ContentNegotiation) {
            json(json = json)
        }
        BrowserUserAgent()
    }

    suspend inline fun <reified T> get(url: String, crossinline block: HttpRequestBuilder.() -> Unit = {}): T =
        useHttpClient<String> {
            it.get(url) {
                header(HttpHeaders.Cookie, BiliBiliDynamic.cookie.toString())
                block()
            }.body()
        }.decode()

    suspend inline fun <reified T> post(url: String, crossinline block: HttpRequestBuilder.() -> Unit = {}): T =
        useHttpClient<String> {
            it.post(url) {
                header(HttpHeaders.Cookie, BiliBiliDynamic.cookie.toString())
                block()
            }.body()
        }.decode()

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
                if (isActive && (throwable is IOException || throwable is HttpRequestTimeoutException)) {
                    clientIndex = (clientIndex + 1) % clients.size
                } else {
                    throw throwable
                }
            }
        }
        throw CancellationException()
    }

}