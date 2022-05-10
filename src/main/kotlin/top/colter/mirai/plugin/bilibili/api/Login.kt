package top.colter.mirai.plugin.bilibili.api

import top.colter.mirai.plugin.bilibili.client.BiliLogin
import top.colter.mirai.plugin.bilibili.data.LoginResult

suspend fun BiliLogin.getLoginUrl(): LoginResult = get(LOGIN_URL)
