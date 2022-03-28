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

    suspend fun testBuildImageMessage(msg: String) {
        ImgUtils.buildImageMessage(
            listOf(
                ImgUtils.textContent(msg, null)!!,
                ImgUtils.videoContent(
                    "https://i0.hdslb.com/bfs/archive/5c20fc634ca754acc6b48a445a2980b9a4942107.jpg",
                    "趣味视频征集活动今日",
                    "视频内容不限，包括但不限于打法攻略、创意玩法等内容视频内容不限，包括但不限于打法攻略、创意玩法等内容",
                    "视频"
                ),
                //ImgUtils.imageContent(
                //    listOf(
                //        DynamicPictureInfo(1080,null,"https://i0.hdslb.com/bfs/album/648fb2527f49fd42c6ecbf991151e1593e7225ad.jpg",1920),
                //        DynamicPictureInfo(816,null,"https://i0.hdslb.com/bfs/album/0ae6fee9eaeafe614377cf5451e78c2430d5a6e4.gif",499),
                //        DynamicPictureInfo(816,null,"https://i0.hdslb.com/bfs/album/56c135d05c0d4a77964db04a07f039d7fe945f14.gif",499),
                //        DynamicPictureInfo(816,null,"https://i0.hdslb.com/bfs/album/1971358d4d71ded8c1b287c7377a32c397190490.gif",499),
                //        DynamicPictureInfo(816,null,"https://i0.hdslb.com/bfs/album/554027b1f38ad88040e315cdf55ee98ed5a20335.gif",499),
                //        DynamicPictureInfo(816,null,"https://i0.hdslb.com/bfs/album/2180d689ad3de225f70e37f6ae3e953f583b012a.gif",499),
                //        DynamicPictureInfo(816,null,"https://i0.hdslb.com/bfs/album/d009f31357f049f7f8134e4e74e208db4284c2eb.gif",499),
                //    )),
                ImgUtils.footer("动态ID：12313246432132")
            ),
            UserProfile(
                UserInfo(
                    1,
                    "Test",
                    "https://i0.hdslb.com/bfs/face/904bef1b4067335068faba12062f735dda07c1fe.jpg"
                )
            ),
            "2021年12月21日", "#9fc7f3", "D:/Code/test.png"
        )//D:/Code/test.png
    }

    @Test
    fun imgTest(): Unit = runBlocking {
        testBuildImageMessage("#明日方舟#\n" +
            "【新增服饰】\n" +
            "//无拘无束 - 刻俄柏\n" +
            "0011子品牌，飙系列新款/无拘无束。为了奖励近期十分听话的刻俄柏，火神托人从哥伦比亚弄来了一套最新潮流......童装？\n" +
            "\n" +
            "_____________\n" +
            "火神从包裹里拿出一件，刻俄柏就穿上一件，然而，当火神想再找条长裤给刻俄柏穿上的时候......她早就跑得没影啦！ ")
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