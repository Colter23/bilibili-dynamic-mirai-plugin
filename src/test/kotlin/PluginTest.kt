package top.colter.mirai.plugin

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.vdurmont.emoji.EmojiParser
import kotlinx.coroutines.runBlocking
import org.junit.Test
import top.colter.mirai.plugin.bilibili.data.SubData
import top.colter.mirai.plugin.bilibili.data.UserInfo
import top.colter.mirai.plugin.bilibili.data.UserProfile
import top.colter.mirai.plugin.bilibili.utils.ImgUtils
import java.net.URL
import java.nio.file.FileSystems
import javax.imageio.ImageIO


internal class PluginTest {

    @Test
    fun test(): Unit = runBlocking {

    }

    val dynamic: MutableMap<Long, SubData> = mutableMapOf(
        0L to SubData("ALL", filter = mutableMapOf(
            "0" to mutableListOf("互动抽奖", "恭喜"),
            "3375582524" to mutableListOf("额")
        )),
        11111111L to SubData("AAA", filter = mutableMapOf(
            "0" to mutableListOf("啊这", "可以"),
            "3375582524" to mutableListOf("和", "看"),
            "2323232323" to mutableListOf("哦", "怕"),
            "7878787878" to mutableListOf("和", "吧"),
        ), containFilter = mutableMapOf(
            "5656566655" to mutableListOf("看"),
        )),
        22222222L to SubData("BBB", filter = mutableMapOf(
            "0" to mutableListOf("啊这", "可以"),
            "5656566655" to mutableListOf("和", "看")
        ))
    )

    @Test
    fun filterTest(): Unit = runBlocking{
        val content = "啊水水水水水水水水水看水水水水水"
        val set = mutableSetOf("3375582524", "2323232323", "5656566655").filterContent(11111111L, content)
        println(set)
        set.forEach(::println)
    }

    fun MutableSet<String>.filterContent(uid: Long, content: String): MutableSet<String> {
        val allContainList = mutableListOf<String>()
        dynamic[0]?.containFilter?.get("0")?.let { allContainList.addAll(it) }
        dynamic[uid]?.containFilter?.get("0")?.let { allContainList.addAll(it) }

        val allFilterList = mutableListOf<String>()
        dynamic[0]?.filter?.get("0")?.let { allFilterList.addAll(it) }
        dynamic[uid]?.filter?.get("0")?.let { allFilterList.addAll(it) }

        return filter { contact ->
            val containList = mutableListOf<String>()
            containList.addAll(allContainList)
            dynamic[0]?.containFilter?.get(contact)?.let { containList.addAll(it) }
            dynamic[uid]?.containFilter?.get(contact)?.let { containList.addAll(it) }
            containList.forEach {
                val b = Regex(it).containsMatchIn(content)
                if (b) return@filter true
            }
            if (containList.size > 0) return@filter false

            val filterList = mutableListOf<String>()
            filterList.addAll(allFilterList)
            dynamic[0]?.filter?.get(contact)?.let { filterList.addAll(it) }
            dynamic[uid]?.filter?.get(contact)?.let { filterList.addAll(it) }
            filterList.forEach {
                val b = Regex(it).containsMatchIn(content)
                if (b) return@filter false
            }
            true
        }.toMutableSet()
    }

    fun testBuildImageMessage(msg: String) {
        ImgUtils.buildImageMessage(
            listOf(
                ImgUtils.textContent(msg, null)!!,
                ImgUtils.videoContent(
                    "https://i0.hdslb.com/bfs/archive/5c20fc634ca754acc6b48a445a2980b9a4942107.jpg",
                    "趣味视频征集活动今日",
                    "视频内容不限，包括但不限于打法攻略、创意玩法等内容视频内容不限，包括但不限于打法攻略、创意玩法等内容",
                    "视频"
                )
            ),
            UserProfile(
                UserInfo(
                    1,
                    "Test",
                    "https://i0.hdslb.com/bfs/face/904bef1b4067335068faba12062f735dda07c1fe.jpg@240w_240h_1c_1s.png"
                )
            ),
            "2021年12月21日", "#9fc7f3", "D:/Code/test.png"
        )//D:/Code/test.png
    }

    @Test
    fun emojiTest(): Unit = runBlocking{
        val msgText = "我的\uD83C\uDF15世界"
        val emojiHex = EmojiParser.parseFromUnicode(msgText) { e ->
            val emojis = e.emoji.htmlHexadecimal.split(";").filter { it.isNotEmpty() }.map { it.substring(3) }.toList()
            val emoji = emojis.joinToString("-")
            runCatching {
                //async {
                //    withTimeout(5000){
                        println(emoji)
                        ImageIO.read(URL("https://twemoji.maxcdn.com/36x36/$emoji.png"))
                    //}
                    //delay(100)
                //}
            }.onFailure {
                println(it.message)
                return@parseFromUnicode e.emoji.unicode
            }
            "[$emoji]"
        }
        println(emojiHex)
    }

    @Test
    fun QRTest(): Unit = runBlocking{
        //QRCode.from("https://passport.bilibili.com/qrcode/h5/login?oauthKey=c3bd5286a2b40a822f5f60e9bf3f602e").withSize(250, 250).file("QRCode").renameTo(
        //    File("D:/Code/QRCode.png")
        //)
        val text = "https://passport.bilibili.com/qrcode/h5/login?oauthKey=c3bd5286a2b40a822f5f60e9bf3f602e"
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 250, 250)
        val path = FileSystems.getDefault().getPath("D:/Code/QRCode.png")

        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path)
    }

}