package top.colter.mirai.plugin.bilibili.api

// Login
const val LOGIN_QRCODE = "https://passport.bilibili.com/x/passport-login/web/qrcode/generate"
const val LOGIN_INFO = "https://passport.bilibili.com/x/passport-login/web/qrcode/poll"

// Dynamic
const val NEW_DYNAMIC = "https://api.bilibili.com/x/polymer/web-dynamic/v1/feed/all"
const val SPACE_DYNAMIC = "https://api.bilibili.com/x/polymer/web-dynamic/v1/feed/space"
const val DYNAMIC_DETAIL = "https://api.bilibili.com/x/polymer/web-dynamic/v1/detail"

// Video
const val VIDEO_DETAIL = "https://api.bilibili.com/x/web-interface/view"

// Article
const val ARTICLE_DETAIL = "https://api.bilibili.com/x/article/viewinfo"
const val ARTICLE_LIST = "https://api.bilibili.com/x/article/cards"

// Live
const val LIVE_LIST = "https://api.live.bilibili.com/xlive/web-ucenter/v1/xfetter/GetWebList"
const val LIVE_STATUS_BATCH = "https://api.live.bilibili.com/room/v1/Room/get_status_info_by_uids"
const val LIVE_DETAIL = "https://api.live.bilibili.com/room/v1/Room/get_info"

// Search
const val SEARCH = "https://api.bilibili.com/x/web-interface/search/type"

// Space
const val USER_INFO = "https://api.bilibili.com/x/space/acc/info"
const val USER_INFO_WBI = "https://api.bilibili.com/x/space/wbi/acc/info"
const val USER_ID = "https://api.bilibili.com/x/web-interface/nav"
const val SPACE_SEARCH = "https://api.bilibili.com/x/space/wbi/arc/search"

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

// Short Link
const val SHORT_LINK = "https://api.bilibili.com/x/share/click"

// Twemoji CDN
const val TWEMOJI = "https://cdnjs.cloudflare.com/ajax/libs/twemoji/14.0.2/72x72"

