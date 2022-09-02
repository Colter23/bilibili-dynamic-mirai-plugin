package top.colter

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.serializer
import org.jetbrains.skia.*
import org.junit.Test
import top.colter.mirai.plugin.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.data.DynamicItem
import top.colter.mirai.plugin.bilibili.data.ModuleAuthor
import top.colter.mirai.plugin.bilibili.data.ModuleDynamic
import top.colter.mirai.plugin.bilibili.draw.*
import top.colter.mirai.plugin.bilibili.utils.json
import top.colter.mirai.plugin.bilibili.utils.json2DataClassFile
import java.io.File
import java.nio.file.Path
import kotlin.io.path.*


internal class PluginTest {

    @Test
    fun jsonToDataClass(): Unit = runBlocking {
        val url = "https://api.live.bilibili.com/room/v1/Room/get_info?room_id=21448649"
        json2DataClassFile(url, "Liiii", Path("src/main/kotlin/top/colter/mirai/plugin/bilibili/data"))
    }

    @Test
    fun httpTest(): Unit = runBlocking {
        val client = HttpClient(OkHttp)
        val c = client.get("https://api.bilibili.com/x/polymer/web-dynamic/v1/feed/all?timezone_offset=-480&type=all&page=1").body<String>()
        println(c)
    }

    @Test
    fun cookieTest(): Unit = runBlocking {

        val client = BiliClient()

        println(client.useHttpClient {
            it.get("http://passport.bilibili.com/qrcode/getLoginUrl").body<String>()
        })

    }

    @Test
    fun timeTest(): Unit = runBlocking {
        val DYNAMIC_START = 1498838400L
        fun dynamictime(id: Long): Long = (id shr 32) + DYNAMIC_START
        println(dynamictime(649955687456047124))
    }


    @Test
    fun drawTest(): Unit = runBlocking {

        loginQrCode("https://passport.bilibili.com/qrcode/h5/login?oauthKey=c3bd5286a2b40a822f5f60e9bf3f602e")
    }

    sealed class RichText {
        data class Text(
            val value: String
        ) : RichText()

        data class Emoji(
            val value: String
        ) : RichText()
    }


    @Test
    fun emojiTest(): Unit = runBlocking {


        //val text = "AAAAA👩🏻‍⚕️👩🏻‍🏫👩🏻‍⚕️👩🏻‍🏫AAvv"
        //val text = "\uD83D\uDE05\uD83E\uDD21\uD83E\uDD16\uD83E\uDDA2\uD83D\uDC11\uD83C\uDF8B\uD83C\uDF34\uD83E\uDD69\uD83E\uDD5C\uD83D\uDC40\uD83E\uDD1E\uD83D\uDC98\uD83C\uDF81\uD83C\uDF83\uD83D\uDE8C\uD83D\uDEB2\uD83D\uDE94\uD83D\uDD73\uD83D\uDD73\uD83D\uDC2D\uD83D\uDD73\uD83D\uDD73\uD83D\uDC48\uD83D\uDC49\uD83D\uDC46\uD83D\uDC47\uD83E\uDD75"
        val text = "🤡"

        val textNode = mutableListOf<RichText>()
        var index = 0

        emojiRegex.findAll(text).forEach {
            if (index != it.range.first) {
                textNode.add(RichText.Text(text.substring(index, it.range.first)))
            }
            textNode.add(RichText.Emoji(it.value))
            index = it.range.last + 1
        }

        if (index != text.length) {
            textNode.add(RichText.Text(text.substring(index, text.length)))
        }


        val font = Font(
            Typeface.makeFromFile("E:/Desktop/资源/字体/HarmonyOS Sans/HarmonyOS_Sans_SC/HarmonyOS_Sans_SC_Medium.ttf"),
            22f
        )

        Surface.makeRasterN32Premul(500, 500).apply {
            canvas.apply {
                var y = 20f

                textNode.forEach {
                    when (it) {
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
    fun drawDynamicTest(): Unit = runBlocking {
        val dynamic = DynamicItem(
//            "DYNAMIC_TYPE_WORD",
            "DYNAMIC_TYPE_FORWARD",
            "652271005324017683",
            true,
            null,
            DynamicItem.Modules(
                ModuleAuthor(
                    "AUTHOR_TYPE_NORMAL",
                    487550002,
                    "猫芒ベル_Official",
                    "https://i1.hdslb.com/bfs/face/652385c47e4742b6e26e19995a2407c83756b1f7.jpg",
                    1650707078,
                    "2022-04-23 17:44",
                    "",
                    false,
                    true,
                    "",
                    "//space.bilibili.com/487550002/dynamic",
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
                        6562,
                        2,
                        "花园Serena2粉丝",
                        "http://i0.hdslb.com/bfs/garb/item/5ebada630d1897124a9f33dd2d5c9566d02fcc72.png",
                        //"http://i0.hdslb.com/bfs/garb/item/5db26595431f8af25ae269e47da9f1d8c06bb657.png",
                        "https://www.bilibili.com/h5/mall/fans/recommend/2452?navhide=1&mid=186463&from=dynamic&isdiy=0",
                        ModuleAuthor.Decorate.Fan(
                            "#ffb48d",
                            false,
                            "000001",
                            1
                        )
                    )
                ),

                ModuleDynamic(
                    topic = ModuleDynamic.Topic(
                        1,
                        "测试主题",
                        ""
                    ),
                    desc = ModuleDynamic.Desc(
                        listOf(
                            ModuleDynamic.Desc.RichTextNode(
                                "RICH_TEXT_NODE_TYPE_TEXT",
                                "AAAAAA好唯美的😶‍🌫️曲调，好温柔👩🏻‍⚕️🙃的歌声",
                                "AAAAAA好唯美的\uD83D\uDE36\u200D\uD83C\uDF2B️曲调，好温柔\uD83D\uDC69\uD83C\uDFFB\u200D⚕️\uD83D\uDE43的歌声",
                            ),
                            ModuleDynamic.Desc.RichTextNode(
                                "RICH_TEXT_NODE_TYPE_EMOJI",
                                "[tv_难过]",
                                "[tv_难过]",
                                emoji = ModuleDynamic.Desc.RichTextNode.Emoji(
                                    1,
                                    "http://i0.hdslb.com/bfs/emote/87f46748d3f142ebc6586ff58860d0e2fc8263ba.png",
                                    1,
                                    "[tv_难过]"
                                )
                            ),
                            ModuleDynamic.Desc.RichTextNode(
                                "RICH_TEXT_NODE_TYPE_TEXT",
                                "测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试",
                                "测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试",
                            ),
                        ),
                        "AAAAAA好唯美的\uD83D\uDE36\u200D\uD83C\uDF2B️曲调，好温柔\uD83D\uDC69\uD83C\uDFFB\u200D⚕️\uD83D\uDE43的歌声[tv_难过]测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试"
                    ),
                    //major = ModuleDynamic.Major(
                    //    "MAJOR_TYPE_ARCHIVE",
                    //    ModuleDynamic.Major.Archive(
                    //        1,
                    //        "341097266",
                    //        "BV14R4y1P7Me",
                    //        "【杂谈】她好像知道自己很可爱",
                    //        "https://i2.hdslb.com/bfs/archive/f1d0fb9dbee0066997546a1951f90aeccc95808c.jpg",
                    //        "20220329－－封面：早乙女aku 翻译：土間うまる 时轴：予之笑颜 校对：剪辑也很可爱 剪辑：Canizza 压制：伊落校对：剪辑也很可爱 剪辑：Canizza 压制：伊落校对：剪辑也很可爱 剪辑：Canizza 压制：伊落",
                    //        false,
                    //        "04:02",
                    //        "",
                    //        ModuleDynamic.Major.Stat(
                    //            "1236",
                    //            "1.2万"
                    //        ),
                    //        ModuleDynamic.Major.Badge(
                    //            "",
                    //            "",
                    //            "投稿视频"
                    //        )
                    //    ),
                    //),
                    additional = ModuleDynamic.Additional(
                        "ADDITIONAL_TYPE_COMMON",
                        common = ModuleDynamic.Additional.Common(
                            "80",
                            "阴阳师",
                            "https://i0.hdslb.com/bfs/game/5a163e68a73a074cd48db9eab8a2fc1ad1a6841f.png",
                            "game",
                            "卡牌/二次元/唯美",
                            "神堕八岐大蛇 5月18日上线",
                            "相关游戏",
                            "https://www.biligame.com/detail?id=80&sourceFrom=1005",
                            1,
                            ModuleDynamic.Additional.Button(1,1,null,null,null,null)
                        ),
                        reserve = ModuleDynamic.Additional.Reserve(
                            564323,
                            1802011210,
                            "直播预约：玥玥春日新衣发布",
                            2623,
                            ModuleDynamic.Additional.Reserve.Desc("04-17 20:00 直播", 0),
                            ModuleDynamic.Additional.Reserve.Desc("2623人预约", 0),
                            ModuleDynamic.Additional.Reserve.Desc("预约有奖：新衣立牌*3份、玥玥钥匙扣*3份", 0),

                            null,
                            0,
                            2,
                            "",
                            ModuleDynamic.Additional.Button(1,1,null,null,null,null)
                        )

                    )

                )

            ),
            orig = DynamicItem(
//            "DYNAMIC_TYPE_WORD",
                "DYNAMIC_TYPE_WORD",
                "652271005324017683",
                true,
                null,
                DynamicItem.Modules(
                    ModuleAuthor(
                        "AUTHOR_TYPE_NORMAL",
                        487550002,
                        "猫芒ベル_Official",
                        "https://i1.hdslb.com/bfs/face/652385c47e4742b6e26e19995a2407c83756b1f7.jpg",
                        1650707078,
                        "2022-04-23 17:44",
                        "",
                        false,
                        true,
                        "",
                        "//space.bilibili.com/487550002/dynamic",
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
                        desc = ModuleDynamic.Desc(
                            listOf(
                                ModuleDynamic.Desc.RichTextNode(
                                    "RICH_TEXT_NODE_TYPE_TEXT",
                                    "好唯美的😶‍🌫️曲调，好温柔👩🏻‍⚕️🙃的歌声",
                                    "好唯美的\uD83D\uDE36\u200D\uD83C\uDF2B️曲调，好温柔\uD83D\uDC69\uD83C\uDFFB\u200D⚕️\uD83D\uDE43的歌声",
                                ),
                                //ModuleDynamic.Desc.RichTextNode(
                                //    "RICH_TEXT_NODE_TYPE_EMOJI",
                                //    "[tv_难过]",
                                //    "[tv_难过]",
                                //    emoji = ModuleDynamic.Desc.RichTextNode.Emoji(
                                //        1,
                                //        "http://i0.hdslb.com/bfs/emote/87f46748d3f142ebc6586ff58860d0e2fc8263ba.png",
                                //        1,
                                //        "[tv_难过]"
                                //    )
                                //),
                                ModuleDynamic.Desc.RichTextNode(
                                    "RICH_TEXT_NODE_TYPE_TEXT",
                                    "感受到雨中\n\n的茶香了吗？",
                                    "感受到雨中\n\n的茶香了吗？",
                                ),
                                // 𓂚𓈖𓇋𓂝𓎛𓇹 鬼
                                ModuleDynamic.Desc.RichTextNode(
                                    "RICH_TEXT_NODE_TYPE_TEXT",
                                    "𓂚𓈖𓇋𓂝𓎛𓇹\n",
                                    "𓂚𓈖𓇋𓂝𓎛𓇹\n",
                                ),
                                ModuleDynamic.Desc.RichTextNode(
                                    "RICH_TEXT_NODE_TYPE_TEXT",
                                    "नमस्ते\n",
                                    "नमस्ते\n",
                                ),
                                ModuleDynamic.Desc.RichTextNode(
                                    "RICH_TEXT_NODE_TYPE_TEXT",
                                    "Olá\n",
                                    "Olá\n",
                                ),
                                ModuleDynamic.Desc.RichTextNode(
                                    "RICH_TEXT_NODE_TYPE_TEXT",
                                    "Здравствуйте\n",
                                    "Здравствуйте\n",
                                ),
                                ModuleDynamic.Desc.RichTextNode(
                                    "RICH_TEXT_NODE_TYPE_TEXT",
                                    "【𝕭𝖊𝖙 𝖔𝖓 𝖒𝖊】\n",
                                    "【𝕭𝖊𝖙 𝖔𝖓 𝖒𝖊】\n",
                                ),
                                ModuleDynamic.Desc.RichTextNode(
                                    "RICH_TEXT_NODE_TYPE_TEXT",
                                    "ᵉᵛᵉʳʸ ˡⁱᶠᵉ ᵗʰᵃᵗ ᵍᵒᵉˢ ᵗᵒ ᵈᵉᵃᵗʰ ⁱˢ ᵍʳᵒʷⁱⁿᵍ ᵖᵃˢˢⁱᵒⁿᵃᵗᵉˡʸ.\n",
                                    "ᵉᵛᵉʳʸ ˡⁱᶠᵉ ᵗʰᵃᵗ ᵍᵒᵉˢ ᵗᵒ ᵈᵉᵃᵗʰ ⁱˢ ᵍʳᵒʷⁱⁿᵍ ᵖᵃˢˢⁱᵒⁿᵃᵗᵉˡʸ.\n",
                                ),
                                ModuleDynamic.Desc.RichTextNode(
                                    "RICH_TEXT_NODE_TYPE_TOPIC",
                                    "#原创歌曲#",
                                    "#原创歌曲#",
                                ),
                                ModuleDynamic.Desc.RichTextNode(
                                    "RICH_TEXT_NODE_TYPE_TOPIC",
                                    "#虚拟歌手#",
                                    "#虚拟歌手#",
                                ),
                                ModuleDynamic.Desc.RichTextNode(
                                    "RICH_TEXT_NODE_TYPE_WEB",
                                    "https://www.bilibili.com/medialist/play/ml1604262874",
                                    "网页链接",
                                    jumpUrl = "https://www.bilibili.com/medialist/play/ml1604262874"
                                ),
                            ),
                            "好唯美的\uD83D\uDE36\u200D\uD83C\uDF2B️曲调，好温柔\uD83D\uDC69\uD83C\uDFFB\u200D⚕️\uD83D\uDE43的歌声[tv_难过]感受到雨中\n\n\n\n\n\n\n\n\n\n\n的茶香了吗？#原创歌曲##虚拟歌手#网页链接"
                        ),

                        major = ModuleDynamic.Major(
                            "MAJOR_TYPE_ARCHIVE",
                            ModuleDynamic.Major.Archive(
                                1,
                                "341097266",
                                "BV14R4y1P7Me",
                                "【杂谈】她好像知道自己很可爱",
                                "https://i2.hdslb.com/bfs/archive/f1d0fb9dbee0066997546a1951f90aeccc95808c.jpg",
                                "20220329－－封面：早乙女aku 翻译：土間うまる 时轴：予之笑颜 校对：剪辑也很可爱 剪辑：Canizza 压制：伊落校对：剪辑也很可爱 剪辑：Canizza 压制：伊落校对：剪辑也很可爱 剪辑：Canizza 压制：伊落",
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
                            ),
                        ),

                    )

                )
            )

        )
        val dynamics = dynamic.drawDynamic(Color.makeRGB("#d3edfa"), false)
        val img = makeCardBg(dynamics.height, listOf(Color.makeRGB("#d3edfa"))) {
            it.drawImage(dynamics, 0f, 0f)
        }
        File("dynamic.png").writeBytes(img.encodeToData()!!.bytes)


        //DynamicDraw.makeDynamic(dynamic)

    }

    @Test
    fun jsonTest(): Unit = runBlocking {

        val jsonStr =
            "{\"basic\": {\"comment_id_str\": \"193712377\",\"comment_type\": 11,\"like_icon\": {\"action_url\": \"\",\"end_url\": \"\",\"id\": 0,\"start_url\": \"\"},\"rid_str\": \"193712377\"},\"id_str\": \"656453757984309256\",\"modules\": {\"module_author\": {\"face\": \"http://i1.hdslb.com/bfs/face/5cf0b8f6acb15c6051e57e31503fb3d3ad945f96.jpg\",\"face_nft\": false,\"following\": true,\"jump_url\": \"//space.bilibili.com/697091119/dynamic\",\"label\": \"\",\"mid\": 697091119,\"name\": \"猫雷NyaRu_Official\",\"official_verify\": {\"desc\": \"\",\"type\": 0},\"pendant\": {\"expire\": 0,\"image\": \"\",\"image_enhance\": \"\",\"image_enhance_frame\": \"\",\"name\": \"\",\"pid\": 0},\"pub_action\": \"\",\"pub_time\": \"2022-05-05 00:15\",\"pub_ts\": 1651680951,\"type\": \"AUTHOR_TYPE_NORMAL\",\"vip\": {\"avatar_subscript\": 1,\"avatar_subscript_url\": \"http://i0.hdslb.com/bfs/vip/icon_Certification_big_member_22_3x.png\",\"due_date\": 1711123200000,\"label\": {\"bg_color\": \"#FB7299\",\"bg_style\": 1,\"border_color\": \"\",\"label_theme\": \"annual_vip\",\"path\": \"\",\"text\": \"年度大会员\",\"text_color\": \"#FFFFFF\"},\"nickname_color\": \"#FB7299\",\"status\": 1,\"theme_type\": 0,\"type\": 2}},\"module_dynamic\": {\"additional\": null,\"desc\": {\"rich_text_nodes\": [{\"orig_text\": \"みんなは今何してる〜？\\n今日はおでかけでつかれちゃった！\\nおやすみ\uD83D\uDC99\uD83D\uDC99mua\",\"text\": \"みんなは今何してる〜？\\n今日はおでかけでつかれちゃった！\\nおやすみ\uD83D\uDC99\uD83D\uDC99mua\",\"type\": \"RICH_TEXT_NODE_TYPE_TEXT\"}],\"text\": \"みんなは今何してる〜？\\n今日はおでかけでつかれちゃった！\\nおやすみ\uD83D\uDC99\uD83D\uDC99mua\"},\"major\": {\"draw\": {\"id\": 193712377,\"items\": [{\"height\": 1000,\"size\": 345.55078,\"src\": \"https://i0.hdslb.com/bfs/album/874ecf3eb8681d8a4b73ec70006ab2d0f8066a96.jpg\",\"tags\": [],\"width\": 1000}]},\"type\": \"MAJOR_TYPE_DRAW\"},\"topic\": null},\"module_more\": {\"three_point_items\": [{\"label\": \"举报\",\"type\": \"THREE_POINT_REPORT\"}]},\"module_stat\": {\"comment\": {\"count\": 180,\"forbidden\": false},\"forward\": {\"count\": 4,\"forbidden\": false},\"like\": {\"count\": 1150,\"forbidden\": false,\"status\": false}}},\"type\": \"DYNAMIC_TYPE_DRAW\",\"visible\": true}"

        val item = json.decodeFromString<DynamicItem>(json.serializersModule.serializer(), jsonStr)
        //item.draw()
        println()
        println(item.type)

    }

    @Test
    fun colorsTest(): Unit = runBlocking {

        Surface.makeRasterN32Premul(500, 700).apply {
            canvas.apply {

                val rrect = RRect.makeXYWH(10f, 10f, 480f, 680f, 10f)

                val colors = IntArray(8)

                for (i in 0 until 8) {
                    val rgb = hsb2rgb(i * 360 / 8f,  1f, 1f)
                    colors[i] = Color.makeRGB(rgb[0],rgb[1],rgb[2])
                }

                drawRRect(rrect, Paint().apply {
                    color = Color.WHITE
                    mode = PaintMode.STROKE
                    strokeWidth = 5f
                    isAntiAlias = true
                    shader = Shader.makeSweepGradient(
                        rrect.left+rrect.width/2,
                        rrect.top+rrect.height/2,
                        //colors
                        intArrayOf(
                            0xFFff0000.toInt(),
                            0xFFff00ff.toInt(),
                            0xFF0000ff.toInt(),
                            0xFF00ffff.toInt(),
                            0xFF00ff00.toInt(),
                            0xFFffff00.toInt(),
                            0xFFff0000.toInt(),
                        )
                    )
                    imageFilter = ImageFilter.makeBlur(
                        10f,
                        10f,
                        FilterTileMode.CLAMP
                    )
                })
                drawRRect(rrect, Paint().apply {
                    color = Color.WHITE
                    mode = PaintMode.FILL
                    isAntiAlias = true
                    //shader = Shader.makeSweepGradient(
                    //    rrect.left+rrect.width/2,
                    //    rrect.top+rrect.height/2,
                    //    intArrayOf(
                    //        0xFFff0000.toInt(),
                    //        0xFFff00ff.toInt(),
                    //        0xFF0000ff.toInt(),
                    //        0xFF00ffff.toInt(),
                    //        0xFF00ff00.toInt(),
                    //        0xFFffff00.toInt(),
                    //        0xFFff0000.toInt(),
                    //    )
                    //)
                    //intArrayOf(
                    //    0xFFfd004c.toInt(),
                    //    0xFFfe9000.toInt(),
                    //    0xFFfff020.toInt(),
                    //    0xFF3edf4b.toInt(),
                    //    0xFF3363ff.toInt(),
                    //    0xFFb102b7.toInt(),
                    //    0xFFfd004c.toInt(),
                    //)
                })
                drawRRect(rrect, Paint().apply {
                    color = Color.WHITE
                    mode = PaintMode.STROKE
                    strokeWidth = 5f
                    isAntiAlias = true
                    shader = Shader.makeSweepGradient(
                        rrect.left+rrect.width/2,
                        rrect.top+rrect.height/2,
                        //colors
                        intArrayOf(
                            0xFFff0000.toInt(),
                            0xFFff00ff.toInt(),
                            0xFF0000ff.toInt(),
                            0xFF00ffff.toInt(),
                            0xFF00ff00.toInt(),
                            0xFFffff00.toInt(),
                            0xFFff0000.toInt(),
                        )
                    )
                })

            }
        }.saveImage("colors.png")

    }

    @Test
    fun charTest(): Unit = runBlocking {

        //val text = "\uD80C\uDC9A\uD80C\uDE16\uD80C\uDDCB\uD80C\uDC9D\uD80C\uDF9B\uD80C\uDDF9"
        //val text = "\uD83D\uDE05\uD83E\uDD21\uD83E\uDD16\uD83E\uDDA2\uD83D\uDC11\uD83C\uDF8B\uD83C\uDF34"
        val text = "\uD83D\uDE36\u200D\uD83C\uDF2B️" //
        //val text = "啊这"
        println(text)

        for (c in text.codePoints()) {
            println(String(intArrayOf(c), 0, intArrayOf(c).size))
            println(c.toString(16))
        }

    }

    @Test
    fun colorTest(): Unit = runBlocking {

        println(generateLinearGradient(listOf(0xFFffffb2.toInt(), 0xFFd9ffb2.toInt())).toList())

    }


    @Test
    fun templateTest(): Unit = runBlocking {

        val msgTemplate = "【{name}】{type}\n{draw}\n{link} {>>}作者：{name}\nUID：{uid}\n时间：{time}\n类型：{type}\n链接：{link}\r{content}\r{images}{<<}aaaa{link}".replace("\n", "\\n").replace("\r", "\\r")

        val forwardRegex = """\{>>}(.*?)\{<<}""".toRegex()

        val tagRegex = """\{([a-z]+)}""".toRegex()

        val res = forwardRegex.findAll(msgTemplate)

        fun buildMsg(ms: String): String{
            var p = 0
            var content = ms

            while (true){
                val key = tagRegex.find(content, p) ?: break
                val rep = when (key.destructured.component1()){
                    "name" -> {
                        "猫芒ベル_Official"
                    }
                    "uid" -> {
                        "487550002"
                    }
                    "did" -> {
                        "664612572403597363"
                    }
                    "time" -> {
                        "2022年05月28日 10:46:01"
                    }
                    "type" -> {
                        "动态"
                    }
                    "content" -> {
                        "晚安！！！！！！[tv_腼腆]\n" +
                            "\n" +
                            "↓今天的封面[呆]"
                    }
                    "link" -> {
                        "https://t.bilibili.com/664612572403597363"
                    }
                    "images" -> {
                        "[mirai:image:{693B9DBC-0997-B38B-89C1-108401BCDBCA}.jpg]"
                    }
                    "draw" -> {
                        "[mirai:image:{D4D8346D-97C7-559D-FC4F-B8FCC37A721F}.jpg]"
                    }
                    else -> {
                        "不支持的类型: ${key.destructured.component1()}"
                    }
                }
                content = content.replaceRange(key.range, rep)
                p = key.range.first + rep.length
            }

            return content
        }

        var index = 0

        res.forEach { mr ->
            if (mr.range.first > index){
                val msgStr = msgTemplate.substring(index, mr.range.first)

                val msgs = msgStr.split("\\r", "\r")

                msgs.forEach{ ms ->

                    val content = buildMsg(ms)
                    //MiraiCode.deserializeMiraiCode(content)
                    println(content)
                    println()

                    //[mirai:origin:FORWARD,HkGpLUGt3szZfWf76JMyCoerYer0HXTbTdarETiAyC0BP7zRv5K/P9wQ99N3Xzfu]ForwardMessage(preview=[Colter:  dream by wombo, Colter:  http://www.ruanyifeng.com/blog/2016/0…, Colter:  https://ecchi.iwara.tv/users/xinhai99…], title=群聊的聊天记录, brief=[聊天记录], source=聊天记录, summary=查看3条转发消息, nodeList=[Node(senderId=3375582524, time=1653576908, senderName=Colter, messageChain=dream by wombo), Node(senderId=3375582524, time=1653616935, senderName=Colter, messageChain=http://www.ruanyifeng.com/blog/2016/04/same-origin-policy.html), Node(senderId=3375582524, time=1653629438, senderName=Colter, messageChain=https://ecchi.iwara.tv/users/xinhai999/videos?page=1)])

                }
            }
            //buildForwardMessage(){
            val fmsgs = mr.destructured.component1().split("\\r", "\r")

            fmsgs.forEach { ms ->
                val content = buildMsg(ms)
                //MiraiCode.deserializeMiraiCode(content)
                println("forward")
                println(content)
                println()
            }
            //}

            index = mr.range.last + 1

        }

        if (index < msgTemplate.length){
            val msgStr = msgTemplate.substring(index, msgTemplate.length)

            val msgs = msgStr.split("\\r", "\r")

            msgs.forEach{ ms ->

                val content = buildMsg(ms)
                //MiraiCode.deserializeMiraiCode(content)
                println(content)
                println()

            }
        }

    }

    @Test
    fun otherTest(): Unit = runBlocking {

        val url = "http://i0.hdslb.com/bfs/archive/0ffac4d927f2e1b1aef32ae7e73a887405018d50.jpg@672w_378h_1c?aa=11&bb=22"
        //val url = "0ffac4d927f2e1b1aef32ae7e73a887405018d50.jpg"
        println(url.split("?").first().split("@").first().split("/").last())

    }

    @Test
    fun pathTest(): Unit = runBlocking {

        //val path = kotlin.io.path.Path("src/main/kotlin/top/colter/mirai/plugin/bilibili")
        val path = kotlin.io.path.Path("src")

        println(path.absolutePathString())
        println(path.exists())
        println()

        println(path.findFile("DynamicDraw.kt")?.absolutePathString())

    }

    fun java.nio.file.Path.findFile(file: String): Path? {
        forEachDirectoryEntry {
            if (it.isDirectory()){
                val path = it.findFile(file)
                if (path != null) return path
            }else{
                if (it.name == file) return it
            }
        }
        return null
    }


}

const val emojiCharacter =
    "(?:[\\uD83C\\uDF00-\\uD83D\\uDDFF]|[\\uD83E\\uDD00-\\uD83E\\uDDFF]|[\\uD83D\\uDE00-\\uD83D\\uDE4F]|[\\uD83D\\uDE80-\\uD83D\\uDEFF]|[\\u2600-\\u26FF]\\uFE0F?|[\\u2700-\\u27BF]\\uFE0F?|\\u24C2\\uFE0F?|[\\uD83C\\uDDE6-\\uD83C\\uDDFF]{1,2}|[\\uD83C\\uDD70\\uD83C\\uDD71\\uD83C\\uDD7E\\uD83C\\uDD7F\\uD83C\\uDD8E\\uD83C\\uDD91-\\uD83C\\uDD9A]\\uFE0F?|[\\u0023\\u002A\\u0030-\\u0039]\\uFE0F?\\u20E3|[\\u2194-\\u2199\\u21A9-\\u21AA]\\uFE0F?|[\\u2B05-\\u2B07\\u2B1B\\u2B1C\\u2B50\\u2B55]\\uFE0F?|[\\u2934\\u2935]\\uFE0F?|[\\u3030\\u303D]\\uFE0F?|[\\u3297\\u3299]\\uFE0F?|[\\uD83C\\uDE01\\uD83C\\uDE02\\uD83C\\uDE1A\\uD83C\\uDE2F\\uD83C\\uDE32-\\uD83C\\uDE3A\\uD83C\\uDE50\\uD83C\\uDE51]\\uFE0F?|[\\u203C\\u2049]\\uFE0F?|[\\u25AA\\u25AB\\u25B6\\u25C0\\u25FB-\\u25FE]\\uFE0F?|[\\u00A9\\u00AE]\\uFE0F?|[\\u2122\\u2139]\\uFE0F?|\\uD83C\\uDC04\\uFE0F?|\\uD83C\\uDCCF\\uFE0F?|[\\u231A\\u231B\\u2328\\u23CF\\u23E9-\\u23F3\\u23F8-\\u23FA]\\uFE0F?)(?:[\\uD83C\\uDFFB-\\uD83C\\uDFFF]|[\\uD83E\\uDDB0-\\uD83E\\uDDB3])?"

val emojiRegex = "${emojiCharacter}(?:\\u200D${emojiCharacter})*".toRegex()

