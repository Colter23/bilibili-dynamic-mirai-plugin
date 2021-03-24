package top.colter.mirai.plugin.bean

import com.alibaba.fastjson.JSONObject

class Dynamic {
    //动态ID、直播号
    var did = ""
    //动态类型
    var type = 0
    //动态时间戳
    var timestamp : Long = 0
    //未解析的动态内容
    var contentJson : JSONObject = JSONObject()
    //解析后的动态内容
    var content = ""
    //是否为动态
    var isDynamic = true
    //图片集合
    var pictures : MutableList<String>? = null
    //b站表情等
    var display : JSONObject = JSONObject()
    //动态信息 图片左下角的信息
    var info = ""
    //动态链接 发送时跟在后面
    var link = ""
}