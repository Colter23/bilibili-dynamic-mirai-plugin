import kotlinx.coroutines.runBlocking
import org.junit.Test
import top.colter.bilibili.api.getDynamicDetail
import top.colter.bilibili.client.BiliClient


internal class ApiTest {

    private val client = BiliClient()

    @Test
    fun dynamicDetailTest(): Unit = runBlocking {
        println(client.getDynamicDetail(683357961644408871))

    }

}