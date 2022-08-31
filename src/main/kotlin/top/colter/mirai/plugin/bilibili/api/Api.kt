package top.colter.mirai.plugin.bilibili.api

// Login
const val LOGIN_URL = "http://passport.bilibili.com/qrcode/getLoginUrl"
const val LOGIN_INFO = "http://passport.bilibili.com/qrcode/getLoginInfo"

// Dynamic
const val NEW_DYNAMIC = "https://api.bilibili.com/x/polymer/web-dynamic/v1/feed/all"
const val SPACE_DYNAMIC = "https://api.bilibili.com/x/polymer/web-dynamic/v1/feed/space"
const val DYNAMIC_DETAIL = "https://api.bilibili.com/x/polymer/web-dynamic/v1/detail"

// Live
const val LIVE_LIST = "https://api.live.bilibili.com/xlive/web-ucenter/v1/xfetter/GetWebList"
const val LIVE_STATUS_BATCH = "https://api.live.bilibili.com/room/v1/Room/get_status_info_by_uids"

// Search
const val SEARCH = "https://api.bilibili.com/x/web-interface/search/type"

// User
const val USER_INFO = "https://api.bilibili.com/x/space/acc/info"
const val USER_ID = "https://api.bilibili.com/x/web-interface/nav"

// Follow
const val IS_FOLLOW = "https://api.bilibili.com/x/relation"
const val FOLLOW = "https://api.bilibili.com/x/relation/modify"

// Group 分组
const val GROUP_LIST = "https://api.bilibili.com/x/relation/tags"
const val CREATE_GROUP = "https://api.bilibili.com/x/relation/tag/create"
const val DEL_FOLLOW_GROUP = "https://api.bilibili.com/x/relation/tag/del"
const val ADD_USER = "https://api.bilibili.com/x/relation/tags/addUsers"

// Pgc 番剧
const val PGC_MEDIA_INFO = "https://api.bilibili.com/pgc/review/user"
const val PGC_INFO = "https://api.bilibili.com/pgc/view/web/season"
const val FOLLOW_PGC = "https://api.bilibili.com/pgc/web/follow/add"
const val UNFOLLOW_PGC = "https://api.bilibili.com/pgc/web/follow/del"

// Twemoji CDN
const val TWEMOJI = "https://twemoji.maxcdn.com/v/14.0.2/72x72"

