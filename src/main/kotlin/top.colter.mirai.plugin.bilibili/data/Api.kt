package top.colter.mirai.plugin.bilibili.data

const val NEW_DYNAMIC_COUNT =
    "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/web_cyclic_num?type_list=268435455"
const val NEW_DYNAMIC = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_new?type_list=268435455"

const val DYNAMIC_LIST =
    "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?visitor_uid=0&offset_dynamic_id=0&need_top=0&platform=web&host_uid="

const val LIVE_LIST = "https://api.live.bilibili.com/xlive/web-ucenter/v1/xfetter/GetWebList?page=1&page_size=20"

// 参数 uid
const val USER_INFO = "https://api.bilibili.com/x/space/acc/info?mid="

// 参数 uid
const val IS_FOLLOW = "https://api.bilibili.com/x/relation?fid="

const val FOLLOW = "https://api.bilibili.com/x/relation/modify"


const val FOLLOW_GROUP = "https://api.bilibili.com/x/relation/tags"

// POST 参数 tag: 分组名
const val CREATE_GROUP = "https://api.bilibili.com/x/relation/tag/create"

// POST 参数 tagid
const val DEL_FOLLOW_GROUP = "https://api.bilibili.com/x/relation/tag/del"

// POST 参数 fids  tagids
const val ADD_USER = "https://api.bilibili.com/x/relation/tags/addUsers"


fun USER_INFO(uid: Long): String {
    return USER_INFO + uid
}

fun IS_FOLLOW(uid: Long): String {
    return IS_FOLLOW + uid
}

fun DYNAMIC_LIST(uid: Long): String {
    return DYNAMIC_LIST + uid
}
