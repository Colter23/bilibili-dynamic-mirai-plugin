package top.colter.mirai.plugin.bilibili.api

// Login
const val LOGIN_URL = "http://passport.bilibili.com/qrcode/getLoginUrl"
const val LOGIN_INFO = "http://passport.bilibili.com/qrcode/getLoginInfo"

// Dynamic
const val NEW_DYNAMIC = "https://api.bilibili.com/x/polymer/web-dynamic/v1/feed/all"

// User
const val USER_INFO = "https://api.bilibili.com/x/space/acc/info"
const val USER_ID = "https://api.bilibili.com/x/web-interface/nav"

// Follow
const val IS_FOLLOW = "https://api.bilibili.com/x/relation"
const val FOLLOW = "https://api.bilibili.com/x/relation/modify"
const val FOLLOW_GROUP = "https://api.bilibili.com/x/relation/tags"

// Group 分组
const val CREATE_GROUP = "https://api.bilibili.com/x/relation/tag/create"
const val DEL_FOLLOW_GROUP = "https://api.bilibili.com/x/relation/tag/del"
const val ADD_USER = "https://api.bilibili.com/x/relation/tags/addUsers"

