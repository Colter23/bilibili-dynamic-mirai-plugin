package top.colter

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.jetbrains.skia.*
import org.junit.Test
import top.colter.mirai.plugin.bilibili.BiliClient
import top.colter.mirai.plugin.bilibili.BiliDynamicConfig.font
import top.colter.mirai.plugin.bilibili.BiliLogin
import top.colter.mirai.plugin.bilibili.data.*
import top.colter.mirai.plugin.bilibili.draw.*
import top.colter.mirai.plugin.bilibili.utils.json


internal class PluginTest {



    @Test
    fun httpTest(): Unit = runBlocking{

//        val client = HttpClient()
//        client.get<HttpResponse>("aa")
        BiliLogin().getLoginQrCode()

    }

    @Test
    fun cookieTest(): Unit = runBlocking{

        val client = BiliClient()

        println(client.useHttpClient {
            it.get<String>("http://passport.bilibili.com/qrcode/getLoginUrl")
        })

//        json.parseToJsonElement()

    }

    @Test
    fun drawTest(): Unit = runBlocking{

        LoginQrCodeDraw.qrCode("https://passport.bilibili.com/qrcode/h5/login?oauthKey=c3bd5286a2b40a822f5f60e9bf3f602e")
    }

    sealed class RichText {
        data class Text(
            val value: String
        ):RichText()

        data class Emoji(
            val value: String
        ):RichText()
    }


    @Test
    fun emojiTest(): Unit = runBlocking{



        val text = "AAAAA👩🏻‍⚕️👩🏻‍🏫👩🏻‍⚕️👩🏻‍🏫AAvv"


        val textNode = mutableListOf<RichText>()
        var index = 0

        emojiRegex.findAll(text).forEach {
            if (index != it.range.first){
                textNode.add(RichText.Text(text.substring(index, it.range.first)))
            }
            textNode.add(RichText.Emoji(it.value))
            index = it.range.last + 1
        }

        if (index != text.length){
            textNode.add(RichText.Text(text.substring(index, text.length)))
        }


        val font = Font(Typeface.makeFromFile("E:/Desktop/资源/字体/HarmonyOS Sans/HarmonyOS_Sans_SC/HarmonyOS_Sans_SC_Medium.ttf"), 22f)

        Surface.makeRasterN32Premul(500,500).apply {
            canvas.apply {
                var y = 20f

                textNode.forEach {
                    when (it){
                        is RichText.Text -> {
                            val tl = TextLine.make(it.value, font)
                            drawTextLine(tl, 20f, y, Paint().apply {
                                color = Color.WHITE
                                isAntiAlias = true
                            })
                            y += 30f
                        }
                        is RichText.Emoji -> {
                            val tl = TextLine.make(it.value, font)
                            drawTextLine(tl, 20f, y, Paint().apply {
                                color = Color.WHITE
                                isAntiAlias = true
                            })
                            y += 30f
                        }
                    }

                }
            }

        }.saveImage("test.png")


    }

    @Test
    fun drawDynamicTest(): Unit = runBlocking{
        val dynamic = DynamicItem(
//            "DYNAMIC_TYPE_WORD",
            DynamicType.DYNAMIC_TYPE_WORD,
            "652271005324017683",
            true,
            null,
            DynamicItem.Modules(
                ModuleAuthor(
                    "AUTHOR_TYPE_NORMAL",
                    487550002,
                    "猫芒ベル_Official",
                    "https://i1.hdslb.com/bfs/face/652385c47e4742b6e26e19995a2407c83756b1f7.jpg",
                    false,
                    true,
                    "",
                    "//space.bilibili.com/487550002/dynamic",
                    "",
                    "2022-04-23 17:44",
                    1650707078,
                    ModuleAuthor.OfficialVerify(
                        0,
                        ""
                    ),
                    ModuleAuthor.Vip(
                        2,
                        1,
                        "http://i0.hdslb.com/bfs/vip/icon_Certification_big_member_22_3x.png",
                        1658160000000,
                        ModuleAuthor.Vip.Label(
                            "#FB7299",
                            1,
                            "",
                            "annual_vip",
                            "",
                            "年度大会员",
                            "#FFFFFF"
                        ),
                        "#FB7299",
                        1,
                        0
                    ),
                    ModuleAuthor.Pendant(
                        258,
                        "梦100",
                        0,
                        "http://i1.hdslb.com/bfs/face/d3587e6f3b534499fc08a71296bafa74a159fa33.png",
                        "http://i1.hdslb.com/bfs/face/d3587e6f3b534499fc08a71296bafa74a159fa33.png",
                        ""
                    ),
                    ModuleAuthor.Decorate(
                        2426,
                        1,
                        "湊-阿库娅",
                        "http://i0.hdslb.com/bfs/garb/item/5ebada630d1897124a9f33dd2d5c9566d02fcc72.png",
                        "https://www.bilibili.com/h5/mall/fans/recommend/2452?navhide=1&mid=186463&from=dynamic&isdiy=0",
                        ModuleAuthor.Decorate.Fan(
                            "",
                            false,
                            "",
                            0
                        )
                    )
                ),

                ModuleDynamic(
                    major = ModuleDynamic.Major(
                        "MAJOR_TYPE_ARCHIVE",
                        ModuleDynamic.Major.Archive(
                            1,
                            "341097266",
                            "BV14R4y1P7Me",
                            "【杂谈】她好像知道自己很可爱",
                            "",
                            "20220329－－封面：早乙女aku 翻译：土間うまる 时轴：予之笑颜 校对：剪辑也很可爱 剪辑：Canizza 压制：伊落",
                            false,
                            "04:02",
                            "",
                            ModuleDynamic.Major.Stat(
                                "1236",
                                "1.2万"
                            ),
                            ModuleDynamic.Major.Badge(
                                "",
                                "",
                                "投稿视频"
                            )
                        )
                    )
                )

            )
        )
        makeCardBg(dynamic)
    }

    @Test
    fun jsonTest(): Unit = runBlocking{

        val jsonStr = "{\"basic\": {\"comment_id_str\": \"193712377\",\"comment_type\": 11,\"like_icon\": {\"action_url\": \"\",\"end_url\": \"\",\"id\": 0,\"start_url\": \"\"},\"rid_str\": \"193712377\"},\"id_str\": \"656453757984309256\",\"modules\": {\"module_author\": {\"face\": \"http://i1.hdslb.com/bfs/face/5cf0b8f6acb15c6051e57e31503fb3d3ad945f96.jpg\",\"face_nft\": false,\"following\": true,\"jump_url\": \"//space.bilibili.com/697091119/dynamic\",\"label\": \"\",\"mid\": 697091119,\"name\": \"猫雷NyaRu_Official\",\"official_verify\": {\"desc\": \"\",\"type\": 0},\"pendant\": {\"expire\": 0,\"image\": \"\",\"image_enhance\": \"\",\"image_enhance_frame\": \"\",\"name\": \"\",\"pid\": 0},\"pub_action\": \"\",\"pub_time\": \"2022-05-05 00:15\",\"pub_ts\": 1651680951,\"type\": \"AUTHOR_TYPE_NORMAL\",\"vip\": {\"avatar_subscript\": 1,\"avatar_subscript_url\": \"http://i0.hdslb.com/bfs/vip/icon_Certification_big_member_22_3x.png\",\"due_date\": 1711123200000,\"label\": {\"bg_color\": \"#FB7299\",\"bg_style\": 1,\"border_color\": \"\",\"label_theme\": \"annual_vip\",\"path\": \"\",\"text\": \"年度大会员\",\"text_color\": \"#FFFFFF\"},\"nickname_color\": \"#FB7299\",\"status\": 1,\"theme_type\": 0,\"type\": 2}},\"module_dynamic\": {\"additional\": null,\"desc\": {\"rich_text_nodes\": [{\"orig_text\": \"みんなは今何してる〜？\\n今日はおでかけでつかれちゃった！\\nおやすみ\uD83D\uDC99\uD83D\uDC99mua\",\"text\": \"みんなは今何してる〜？\\n今日はおでかけでつかれちゃった！\\nおやすみ\uD83D\uDC99\uD83D\uDC99mua\",\"type\": \"RICH_TEXT_NODE_TYPE_TEXT\"}],\"text\": \"みんなは今何してる〜？\\n今日はおでかけでつかれちゃった！\\nおやすみ\uD83D\uDC99\uD83D\uDC99mua\"},\"major\": {\"draw\": {\"id\": 193712377,\"items\": [{\"height\": 1000,\"size\": 345.55078,\"src\": \"https://i0.hdslb.com/bfs/album/874ecf3eb8681d8a4b73ec70006ab2d0f8066a96.jpg\",\"tags\": [],\"width\": 1000}]},\"type\": \"MAJOR_TYPE_DRAW\"},\"topic\": null},\"module_more\": {\"three_point_items\": [{\"label\": \"举报\",\"type\": \"THREE_POINT_REPORT\"}]},\"module_stat\": {\"comment\": {\"count\": 180,\"forbidden\": false},\"forward\": {\"count\": 4,\"forbidden\": false},\"like\": {\"count\": 1150,\"forbidden\": false,\"status\": false}}},\"type\": \"DYNAMIC_TYPE_DRAW\",\"visible\": true}"

        val item = json.decodeFromString<DynamicItem>(json.serializersModule.serializer(), jsonStr)
        item.draw()
        println()
        println(item.type)

    }


}

const val emojiCharacter = "(?:[\\uD83C\\uDF00-\\uD83D\\uDDFF]|[\\uD83E\\uDD00-\\uD83E\\uDDFF]|[\\uD83D\\uDE00-\\uD83D\\uDE4F]|[\\uD83D\\uDE80-\\uD83D\\uDEFF]|[\\u2600-\\u26FF]\\uFE0F?|[\\u2700-\\u27BF]\\uFE0F?|\\u24C2\\uFE0F?|[\\uD83C\\uDDE6-\\uD83C\\uDDFF]{1,2}|[\\uD83C\\uDD70\\uD83C\\uDD71\\uD83C\\uDD7E\\uD83C\\uDD7F\\uD83C\\uDD8E\\uD83C\\uDD91-\\uD83C\\uDD9A]\\uFE0F?|[\\u0023\\u002A\\u0030-\\u0039]\\uFE0F?\\u20E3|[\\u2194-\\u2199\\u21A9-\\u21AA]\\uFE0F?|[\\u2B05-\\u2B07\\u2B1B\\u2B1C\\u2B50\\u2B55]\\uFE0F?|[\\u2934\\u2935]\\uFE0F?|[\\u3030\\u303D]\\uFE0F?|[\\u3297\\u3299]\\uFE0F?|[\\uD83C\\uDE01\\uD83C\\uDE02\\uD83C\\uDE1A\\uD83C\\uDE2F\\uD83C\\uDE32-\\uD83C\\uDE3A\\uD83C\\uDE50\\uD83C\\uDE51]\\uFE0F?|[\\u203C\\u2049]\\uFE0F?|[\\u25AA\\u25AB\\u25B6\\u25C0\\u25FB-\\u25FE]\\uFE0F?|[\\u00A9\\u00AE]\\uFE0F?|[\\u2122\\u2139]\\uFE0F?|\\uD83C\\uDC04\\uFE0F?|\\uD83C\\uDCCF\\uFE0F?|[\\u231A\\u231B\\u2328\\u23CF\\u23E9-\\u23F3\\u23F8-\\u23FA]\\uFE0F?)(?:[\\uD83C\\uDFFB-\\uD83C\\uDFFF]|[\\uD83E\\uDDB0-\\uD83E\\uDDB3])?"

val emojiRegex = "${emojiCharacter}(?:\\u200D${emojiCharacter})*".toRegex()

