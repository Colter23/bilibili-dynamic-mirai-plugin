package top.colter.mirai.plugin.bilibili.exception


open class InteractException(message: String = "交互异常"): Exception(message)

open class InteractTryLimitException(message: String = "超出交互次数"): InteractException(message)

open class InteractTimeoutException(message: String = "交互超时"): InteractException(message)