
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import net.mamoe.mirai.mock.MockBotFactory
import org.jetbrains.skia.*
import org.junit.After
import org.junit.Test
import top.colter.bilibili.client.BiliClient
import top.colter.bilibili.data.LazyImage
import top.colter.bilibili.data.dynamic.BiliDynamic
import top.colter.bilibili.tools.decode
import top.colter.bilibili.tools.forEachLazyImageFields
import java.io.File
import kotlin.contracts.ExperimentalContracts
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KProperty
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
internal class OtherTest {

    private val testResource = File("src/test/resources")
    private val testOutput = testResource.resolve("output").apply {
        if(!exists()) this.mkdirs()
    }

    internal val bot = MockBotFactory.newMockBotBuilder()
        .id(114514L)
        .nick("FFFFFFFFF")
        .create()

//    fun forEachLazyImageFields(obj: Any, block: LazyImage.() -> Unit) {
//        obj.javaClass.declaredFields.forEach { field ->
//            if (field.type == LazyImage::class.java) {
//                (field.apply { isAccessible = true }.get(obj) as LazyImage?)?.block()
//            }else if (field.type.kotlin.isData) {
//                field.apply { isAccessible = true }.get(obj)?.let {
//                    top.colter.mirai.plugin.bilibili.data.forEachLazyImageFields(it, block)
//                }
//            }
//        }
//    }

    @After
    internal fun `close bot`() {
        bot.close()
    }

    @Test
    fun cc(): Unit = runBlocking {



    }

    @Test
    fun mm(): Unit = runBlocking {
//        delay(5000)

        val dynamic = testResource.resolve("json/774783779415785528.json").readText().decode<BiliDynamic>()

//        println("start")
//        var now = TimeSource.Monotonic.markNow()
//        printAllStringFields(dynamic)
//        println("WWWWW: ${now.elapsedNow()}")
        val client = HttpClient(OkHttp)

//        println("start")
//        var now = TimeSource.Monotonic.markNow()
        val imageList = mutableListOf<Deferred<Pair<LazyImage, ByteArray?>>>()
        forEachLazyImageFields(dynamic) {
            if (url.isNotBlank()) {
                imageList.add(async {
                    Pair(this@forEachLazyImageFields, client.get(url).body())
                })
            }
        }
        imageList.awaitAll().forEach {
            it.first.image = it.second
        }

//        val template = "🤣🧑🏻‍🧑🏻‍🧒🏻{draw}{>>}作者：{name}{n}UID：{uid}{n}时间：{time}{n}类型：{type}{n}链接：{links}{r}{content}{r}{images}{<<}VVVVV"
//        val template = "{draw}{r}{name}@{uid}@{type}{n}{time}{n}{links}"
        val template = "{name}@{type}{n}{links}{r}{content}{r}{images}"
        val contact = bot.getFriend(114514L)!!

//        val list = dynamic.toBiliMessage().buildMessage(template, contact)
//        list.forEach {
//            contact.sendMessage(it)
//        }

//        delay(500000)

//        println(dynamic.modules.moduleAuthor.face.image?.width)
//        println(dynamic.modules.moduleAuthor.face.image?.height)
//        println("WWWWW: ${now.elapsedNow()}")
//        list.awaitAll().forEach {
//            it.first.image = it.second
//        }
//
//        println("WWWWW: ${now.elapsedNow()}")
//        println(dynamic.modules.moduleAuthor.face.image?.width)
//        println(dynamic.modules.moduleAuthor.face.image?.height)
    }

    @Test
    fun measureTest(): Unit = runBlocking {
//        val res1 = async { Test1(this.coroutineContext.job).draw("aqfesdgareta") }
//        val res2 = async { Test1(this.coroutineContext.job).draw("aqfesdgareta") }
//
//        withContext(Dispatchers.Default){
//            println(listOf(res1.await(), res2.await()))
//        }

//        println(measureTime {
//
//        })
//        launch {

            val client = HttpClient(OkHttp){
//                install(JsonFeature) {
//                    serializer = KotlinxSerializer()
//                }
            }

            var url: MyImage  = MyImageUrl("https://i0.hdslb.com/bfs/archive/3ab3dcb35f7989a433e300136147a5a1e1549c17.png")
//            client.get("https://www.baidu.com")
            var now = TimeSource.Monotonic.markNow()

            println("GET-A: ${now.elapsedNow()}")
//        now = TimeSource.Monotonic.markNow()
            val b1 = async { client.get("https://i0.hdslb.com/bfs/new_dyn/74cffb09ffdc851b5ca6953ba65fb879168687092.jpg").body<ByteArray>() }
            val b2 = async { client.get("https://i0.hdslb.com/bfs/new_dyn/acfdf0391fb08600461273b918e49828168687092.jpg").body<ByteArray>() }
            println("GET: ${now.elapsedNow()}")
//        now = TimeSource.Monotonic.markNow()
//        withContext(Dispatchers.Default){
            println("WRITE1-A: ${now.elapsedNow()}")
//        val file1 = testOutput.resolve("test1.png")
//        file1.writeBytes(b1.await());
//        println("WRITE1: ${now.elapsedNow()}")
//        val file2 = testOutput.resolve("test2.png")
//        file2.writeBytes(b2.await());

            val i1 = b1.await()
            println("WRITE1-B: ${now.elapsedNow()}")
            val i2 = b2.await()

            println("WRITE2: ${now.elapsedNow()}")
            println(i1.size)
            println(i2.size)

//        }
//        println("WRITE2-B: ${now.elapsedNow()}")

//        }

//        delay(10000L)
    }
    @Test
    fun measureTest2(): Unit = runBlocking {

        val client = HttpClient(OkHttp)

        var url: MyImage  = MyImageUrl("https://i0.hdslb.com/bfs/archive/3ab3dcb35f7989a433e300136147a5a1e1549c17.png")
//        client.get("https://www.baidu.com")
        var now = TimeSource.Monotonic.markNow()

        println("GET-A: ${now.elapsedNow()}")
//        now = TimeSource.Monotonic.markNow()
//        val b1 = client.get("https://i0.hdslb.com/bfs/archive/3ab3dcb35f7989a433e300136147a5a1e1549c17.png").body<ByteArray>()
        val b1 = client.get("https://i0.hdslb.com/bfs/new_dyn/74cffb09ffdc851b5ca6953ba65fb879168687092.jpg").body<ByteArray>()
        println("GET-1: ${now.elapsedNow()}")
//        val b2 =  client.get("https://i0.hdslb.com/bfs/archive/f00eb0944ac62d85b55628c878c20e3c72b29d2f.png").body<ByteArray>()
        val b2 =  client.get("https://i0.hdslb.com/bfs/new_dyn/acfdf0391fb08600461273b918e49828168687092.jpg").body<ByteArray>()
        println("GET: ${now.elapsedNow()}")
//        now = TimeSource.Monotonic.markNow()
//        println("WRITE1: ${now.elapsedNow()}")
//        val file1 = testOutput.resolve("test1.png")
//        file1.writeBytes(b1);
//        println("WRITE1: ${now.elapsedNow()}")
//        val file2 = testOutput.resolve("test2.png")
//        file2.writeBytes(b2);
//        println("WRITE2: ${now.elapsedNow()}")

//        val i1 = b1.await()
//        println("WRITE1-B: ${now.elapsedNow()}")
//        val i2 = b2.await()

        println("WRITE2: ${now.elapsedNow()}")
        println(b1.size)
        println(b2.size)

    }

    @Test
    fun refTest(): Unit = runBlocking {


//        println(measureTime {
//            BCodeState::class.nestedClasses.forEach {
//                println("====> ${it.toString()}")
//            }
//        })

//        println(BCodeState::class.sealedSubclasses.size)
//        BCodeState::class.sealedSubclasses.map { it.objectInstance }.forEach {
//            println(it)
//        }

//        println(measureTime {
//            println(BCodeState::class.sealedSubclasses.map { it.objectInstance as BCodeState }.firstOrNull(){ it.code == 200 } ?: throw Exception("未知状态"))
//
////            BCodeState::class.sealedSubclasses.forEach {
////                println("-----> ${it.objectInstance?.code}")
////            }
//        })

    }
    @Test
    fun bbb(): Unit = runBlocking {
//        println(BiliCookiesStorage().container)

        val temp = "{draw}{>>}作者：{name}\nUID：{uid}\n时间：{time}\n类型：{type}\n链接：{link}\r{content}\r{images}{<<}"

        val forwardRegex = """\{>>}(.*?)\{<<}""".toRegex()
        val tagRegex = """\{([a-z]+)}""".toRegex()

        val name = tagRegex.find(temp)!!.destructured.component1()
        println(name)

    }


    @OptIn(ExperimentalContracts::class)
    @Test
    fun byTest(): Unit = runBlocking {

        class DD(var aa: String = ""){
            operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
                return "GET: ${thisRef} -> ${property.name} -> $aa"
            }

            operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
                aa = value
                println("SET: ${thisRef} -> ${property.name} -> $value")
            }
        }



        val bb: String by lazy {
            ""
        }

        class AAA{
            var aa: String by DD("CCC")

        }
        println(AAA().aa)
//        aa = "AAA"
//        println(aa)

//        class Site(val map: Map<String, Any?>) {
//            val name: String by map
//            val url: String  by map
//        }
//
//        val map = Site(mapOf(
//            "name" to "aaaa",
//            "qqq" to "nnnn",
//        ))
//
//        println(map.name)
//        println(map.url)


    }


    @Test
    fun clientTest(): Unit = runBlocking {

        val client = BiliClient()
//        try {

//            val dd = client.getData<Map<String, ArticleDetail>>("$ARTICLE_LIST?ids=cv18257173")
//            println(dd)

//        }catch (e: Exception) {
//            println(e)
//        }

    }


}

@Serializable
data class DD(
    val name: String
)


sealed interface MyImage
class MyImageUrl(
    val url: String
): MyImage
class MyImageImage(
    val image: Image
): MyImage

class Test1(job: Job?): CoroutineScope, CompletableJob by SupervisorJob(job) {
    override val coroutineContext: CoroutineContext
        get() = this + CoroutineName("Tasker")

    @OptIn(ExperimentalTime::class)
    fun draw(str: String): Image {
        println("=======")
        val res :Image
        println(measureTime {
            res = Surface.makeRasterN32Premul(1000, 1000).apply {
                canvas.apply {
                    val font = Font(Typeface.makeDefault(), 20f)
                    drawString(str, 0f, 0f, font, Paint())
                    drawString(str, 0f, 30f, font, Paint())
                    drawString(str, 0f, 60f, font, Paint())
                    drawString(str, 0f, 90f, font, Paint())
                    drawString(str, 0f, 120f, font, Paint())
                    drawString(str, 0f, 150f, font, Paint())
                }
            }.makeImageSnapshot()
        })
        println("---------")
        return res
    }
}


