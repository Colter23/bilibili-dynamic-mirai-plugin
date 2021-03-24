package top.colter.myplugin.translate

import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

internal object HttpGet {
    internal const val SOCKET_TIMEOUT = 10000 // 10S
    internal const val GET = "GET"
    operator fun get(host: String, params: Map<String?, String?>?): String? {
        try {
            // 设置SSLContext
            val sslcontext = SSLContext.getInstance("TLS")
            sslcontext.init(null, arrayOf(myX509TrustManager), null)
            val sendUrl = getUrlWithQueryString(host, params)

            // System.out.println("URL:" + sendUrl);
            val uri = URL(sendUrl) // 创建URL对象
            val conn = uri.openConnection() as HttpURLConnection
            if (conn is HttpsURLConnection) {
                conn.sslSocketFactory = sslcontext.socketFactory
            }
            conn.connectTimeout = SOCKET_TIMEOUT // 设置相应超时
            conn.requestMethod = GET
            val statusCode = conn.responseCode
            if (statusCode != HttpURLConnection.HTTP_OK) {
                println("Http错误码：$statusCode")
            }

            // 读取服务器的数据
            val `is` = conn.inputStream
            val br = BufferedReader(InputStreamReader(`is`))
            val builder = StringBuilder()
            var line: String? = null
            while (br.readLine().also { line = it } != null) {
                builder.append(line)
            }
            val text = builder.toString()
            close(br) // 关闭数据流
            close(`is`) // 关闭数据流
            conn.disconnect() // 断开连接
            return text
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return null
    }

    fun getUrlWithQueryString(url: String, params: Map<String?, String?>?): String {
        if (params == null) {
            return url
        }
        val builder = StringBuilder(url)
        if (url.contains("?")) {
            builder.append("&")
        } else {
            builder.append("?")
        }
        var i = 0
        for (key in params.keys) {
            val value = params[key]
                    ?: // 过滤空的key
                    continue
            if (i != 0) {
                builder.append('&')
            }
            builder.append(key)
            builder.append('=')
            builder.append(encode(value))
            i++
        }
        return builder.toString()
    }

    internal fun close(closeable: Closeable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 对输入的字符串进行URL编码, 即转换为%20这种形式
     *
     * @param input 原文
     * @return URL编码. 如果编码失败, 则返回原文
     */
    fun encode(input: String?): String {
        if (input == null) {
            return ""
        }
        try {
            return URLEncoder.encode(input, "utf-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return input
    }

    private val myX509TrustManager: TrustManager = object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate>? {
            return null
        }

        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }
    }
}