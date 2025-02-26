
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Color
import org.junit.Before
import org.junit.Test
import top.colter.bilibili.api.getDynamicDetail
import top.colter.bilibili.client.BiliClient
import top.colter.bilibili.data.EditCookie
import top.colter.bilibili.data.toCookie
import top.colter.bilibili.data.user.OfficialVerifyType
import top.colter.bilibili.tools.decode
import top.colter.mirai.plugin.bilibili.draw.DynamicDraw
import top.colter.mirai.plugin.bilibili.draw.component.Author
import top.colter.skiko.*
import top.colter.skiko.data.LayoutAlignment
import top.colter.skiko.data.Shadow
import top.colter.skiko.layout.*
import java.io.FileNotFoundException


internal class DrawTest {

    private val client = BiliClient()

    @Before
    fun init() {
        Dp.factor = 1f

        FontUtils.loadTypeface(loadTestResource("font", "HarmonyOS_Sans_SC_Medium.ttf").absolutePath)
        FontUtils.loadEmojiTypeface(loadTestResource("font", "NotoColorEmoji.ttf").absolutePath)

        try {
            val cookies = loadTestText(fileName = "cookie.json").decode<List<EditCookie>>().map { it.toCookie() }
            client.storage.initialize(cookies)
        }catch (e: FileNotFoundException) {
            println("未找到cookie文件，将无法使用部分api")
        }
    }

    @Test
    fun `test dynamic`(): Unit = runBlocking {
        val dynamic = client.getDynamicDetail(683357961644408871)
        DynamicDraw(dynamic)
    }

    @Test
    fun `test dynamic style1`(): Unit = runBlocking {
        val face = loadTestImage("image", "avatar.jpg")
        val pendant = loadTestImage("image", "pendant.png")
        val ornament1 = loadTestImage("image", "ornament1.png")
        val ornament2 = loadTestImage("image", "ornament2.png")

        val cover = loadTestImage("image", "bg1.jpg")

        View(
            file = testOutput.resolve("style1.png"),
            modifier = Modifier().width(900.dp).padding(top = 80.dp, right = 40.dp, bottom = 40.dp, left = 40.dp).background(Color.makeRGB(204, 217, 255))
        ) {

            Row(
                alignment = LayoutAlignment.TOP_CENTER,
                modifier = Modifier()
//                        .fillRatioWidth(0.5f)
                    .margin(top = modifier.padding.top * -1, right = modifier.padding.right * -1, left = modifier.padding.left * -1)
                    .fillMaxWidth()
                    .padding(10.dp)
                    .background(Color.WHITE.withAlpha(0.5f))
                    .border(2.dp, listOf(0.dp, 0.dp, 25.dp, 25.dp))
                    .shadows(Shadow.ELEVATION_2)
            ) {
                Box(Modifier().fillMaxWidth()) {
                    Text(
                        text = "动态",
                        fontSize = 22.dp,
                        alignment = LayoutAlignment.CENTER
                    )
                }

            }

            Column(modifier = Modifier()
                .fillMaxWidth()
                .padding(20.dp)
                .background(Color.WHITE.withAlpha(0.6f))
                .border(3.dp, 15.dp)
            ) {
//                Row(
//                    alignment = LayoutAlignment.TOP_CENTER,
//                    modifier = Modifier()
////                        .fillRatioWidth(0.5f)
//                        .margin(top = modifier.padding.top * -1, right = modifier.padding.right * -1, left = modifier.padding.left * -1)
//                        .fillMaxWidth()
//                        .padding(5.dp)
//                        .background(Color.WHITE.withAlpha(0.5f))
//                        .border(2.dp, 15.dp)
//                        .shadows(Shadow.ELEVATION_1)
//                ) {
//                    Box(Modifier().fillMaxWidth()) {
//                        Text(
//                            text = "动态",
//                            alignment = LayoutAlignment.CENTER
//                        )
//                    }
//
//                }
                Author(
                    face = face,
                    pendant = pendant,
                    verify = OfficialVerifyType.PERSONA,
                    name = "猫芒ベル_Official",
                    time = "2023年03月14月 22:00:45",
                    ornament = ornament2,
                    numStr = "000001",
                    color = Color.makeRGB(240, 146, 218),
                    modifier = Modifier().fillMaxWidth().height(100.dp).margin(horizontal = (-15).dp, vertical = 20.dp) // .background(Color.RED)
                )

                Text(
                    text = "#原神# #神里绫华# #白鹭之庭# ",
                    color = Color.makeRGB(23, 139, 207),
                    fontSize = 22.dp,
                    modifier = Modifier().margin(vertical = 10.dp)
                )
                Text(
                    fontSize = 22.dp,
                    maxLinesCount = 100,
                    text = "亲爱的旅行者，「白鹭之庭」活动祈愿即将开启，「白鹭霜华·神里绫华(冰)」概率UP！\n" +
                        "\n" +
                        "活动期间，旅行者可以在活动祈愿中获得更多角色与武器，组建强大的队伍！ \n" +
                        "\n" +
                        "〓祈愿时间〓\n" +
                        "2023/03/21 18:00 ~ 2023/04/11 14:59  \n" +
                        "\n" +
                        "〓祈愿介绍〓 \n" +
                        "●活动期间，限定5星角色「白鹭霜华·神里绫华(冰)」的祈愿获取概率将大幅提升！\n" +
                        "●活动期间，4星角色「晴霜的标绘·米卡(冰)」「无害甜度·砂糖(风)」「猫尾特调·迪奥娜(冰)」的祈愿获取概率将大幅提升！\n" +
                        "●活动结束后，4星角色「晴霜的标绘·米卡(冰)」将在下一版本进入「奔行世间」常驻祈愿。\n" +
                        "※ 以上角色中，限定角色不会进入「奔行世间」常驻祈愿。\n" +
                        "※ 本祈愿属于「角色活动祈愿-2」，「角色活动祈愿」和「角色活动祈愿-2」的祈愿次数保底完全共享，会一直共同累计在「角色活动祈愿」和「角色活动祈愿-2」中，与其他祈愿的祈愿次数保底相互独立计算，互不影响。\n" +
                        "※ 祈愿开启期间，还将开启相应的「且试身手」角色试用活动，旅行者可以使用包含试用角色的固定阵容进入指定的关卡进行体验，挑战成功后即可获得对应奖励！\n" +
                        "※ 更多祈愿信息可点击祈愿界面左下角【详情】按钮进行查询。")

                Image(
                    image = cover,
                    modifier = Modifier().margin(top = 20.dp).border(2.dp, 15.dp)
                )

            }

        }
    }
    
}
