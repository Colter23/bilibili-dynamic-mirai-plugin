package top.colter.mirai.plugin.bilibili.filter


///**
// * 过滤器链 [Filter]
// *
// * 可通过 [FilterChainBuilder] 创建
// *
// * TYPE=VIDEO AND V_TYPE=REPLAY OR V_TYPE=COOPERATE AND REGEX=正则 END
// */
//@Serializable(FilterChainSerializer::class)
//class FilterChain(
//    private val container: List<Filter>
//): List<Filter> by container, RandomAccess {
//    companion object {
//        val regex = """((?:V_)?TYPE|REGEX)(!?=)(.+?) (AND|OR|END)""".toRegex()
//        fun form(row: String): FilterChain = buildFilterChain {
//            regex.matches(row).ifFalse { throw IllegalArgumentException("过滤链格式错误: $row") }.ifTrue {
//                regex.findAll(row).forEach {
//                    append(
//                        it.destructured.component1(),
//                        it.destructured.component2(),
//                        it.destructured.component3(),
//                        it.destructured.component4()
//                    )
//                }
//            }
//        }
//    }
//
//    override fun toString(): String = buildString { container.forEach { append(it.toString()) } }
//}
//
///**
// * 构建一个过滤器链, 通过 append 添加一个过滤器
// */
//class FilterChainBuilder private constructor(
//    private val container: MutableList<Filter>
//): MutableList<Filter> by container {
//    constructor() : this(mutableListOf())
//    constructor(size: Int) : this(ArrayList<Filter>(size))
//
//    fun append(element: Filter) = container.add(element)
//
//    fun append(type: String, equal: String, value: String, separator: String) =
//        container.add(Filter.form(type, equal, value, separator))
//
//}
//
///**
// * 构建一个过滤器链
// * @see FilterChainBuilder
// */
//inline fun buildFilterChain(block: FilterChainBuilder.() -> Unit): FilterChain {
//    return FilterChainBuilder().apply(block).toFilterChain()
//}
//
//fun MutableList<Filter>.toFilterChain(): FilterChain = FilterChain(toList())
//
//object FilterChainSerializer: KSerializer<FilterChain> {
//    override val descriptor: SerialDescriptor =
//        PrimitiveSerialDescriptor(FilterChain::class.qualifiedName!!, PrimitiveKind.STRING)
//
//    override fun deserialize(decoder: Decoder): FilterChain {
//        return FilterChain.form(decoder.decodeString())
//    }
//
//    override fun serialize(encoder: Encoder, value: FilterChain) {
//        encoder.encodeString(value.toString())
//    }
//}
//
//
//fun List<FilterChain>.filter(dynamic: BiliDynamic): Boolean {
//    forEach {
//        if (!it.filter(dynamic)) return false
//    }
//    return true
//}
//
//fun FilterChain.filter(dynamic: BiliDynamic): Boolean {
//    forEach {
//        when (it.type) {
//            FilterType.TYPE -> if (!it.typeFilter(dynamic.type)) return false
//            FilterType.REGEX -> if (!it.regexFilter(dynamic.textContent())) return false
//        }
//    }
//    return true
//}
//
//fun Filter.typeFilter(dynamicType: BiliDynamicType): Boolean {
//    val b = value == dynamicType.name
//    return if (equal) b else !b
//}
//
//fun Filter.regexFilter(contact: String): Boolean {
//    val b = value.toRegex().containsMatchIn(contact)
//    return if (equal) b else !b
//}
