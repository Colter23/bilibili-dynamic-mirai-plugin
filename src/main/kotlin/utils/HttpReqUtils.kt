package top.colter.mirai.plugin.utils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

fun httpGet(url: String): JSONObject{
    val link = URL(url)
    val response = link.readText()
    return JSON.parseObject(response)
}

fun httpGet(url: String, cookie:String): JSONObject{
    val link = URL(url)
    val connection = link.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.connectTimeout = 300000
    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
    connection.setRequestProperty("cookie", cookie)

    try {
        val reader: BufferedReader = BufferedReader(InputStreamReader(connection.inputStream,"utf-8"))
        val output: String = reader.readLine()

        return JSON.parseObject(output)
    } catch (exception: Exception) {
        throw Exception(exception.message)
    }
}
