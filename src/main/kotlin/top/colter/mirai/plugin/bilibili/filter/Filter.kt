package top.colter.mirai.plugin.bilibili.filter


///**
// * 单个过滤器
// * @see FilterChain
// */
//@Serializable
//data class Filter(
//    val type: FilterType,
//    val equal: Boolean,
//    val value: String,
//    val separator: FilterSeparator
//){
//    companion object {
//        fun form(type: String, equal: String, value: String, separator: String): Filter {
//            return Filter(
//                FilterType.valueOf(type),
//                when (equal){ "=" -> true; "!=" -> false; else -> throw Exception() },
//                value,
//                FilterSeparator.valueOf(separator)
//            )
//        }
//    }
//
//    override fun toString(): String = "$type${if (equal) "=" else "!="}$value $separator${if (separator != FilterSeparator.END) " " else ""}"
//}
//
///**
// * 过滤器类型
// */
//enum class FilterType(val text: String){
//    TYPE("类型"),
////    V_TYPE("视频类型"),
//    REGEX("内容匹配")
//}
//
///**
// * 过滤器连接符
// */
//enum class FilterSeparator(val text: String){
//    AND("与"),
//    OR("或"),
//    END("结束")
//}
