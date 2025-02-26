package top.colter.mirai.plugin.bilibili.message

interface HintText {
    val loading: String
    // ...
}

object DefaultHintText: HintText {
    override val loading: String = "加载中..."
    // ...
}

