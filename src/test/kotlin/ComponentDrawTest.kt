
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Color
import org.junit.Before
import org.junit.Test
import top.colter.bilibili.data.user.OfficialVerifyType
import top.colter.mirai.plugin.bilibili.draw.component.*
import top.colter.mirai.plugin.bilibili.draw.loadSVG
import top.colter.mirai.plugin.bilibili.draw.makeImage
import top.colter.skiko.*
import top.colter.skiko.data.LayoutAlignment
import top.colter.skiko.data.Ratio
import top.colter.skiko.data.Shadow
import top.colter.skiko.layout.*


internal class ComponentDrawTest {

    @Before
    fun init() {
        Dp.factor = 1f

        FontUtils.loadTypeface(loadTestResource("font", "HarmonyOS_Sans_SC_Medium.ttf").absolutePath)
        FontUtils.loadEmojiTypeface(loadTestResource("font", "NotoColorEmoji.ttf").absolutePath)
    }


    @Test
    fun `test avatar`(): Unit = runBlocking {

        val face = loadTestImage("image", "avatar1.jpg")
        val pendant = loadTestImage("image", "pendant.png")
        val badge = loadSVG(resource.resolve("icon/PERSONAL_OFFICIAL_VERIFY.svg").readBytes()).makeImage(100, 100)

        View(
            file = testOutput.resolve("avatar.png"),
            modifier = Modifier().height(150.dp).background(Color.WHITE.withAlpha(0.6f))
        ) {
            Row(Modifier().fillMaxHeight().padding(20.dp), LayoutAlignment.CENTER) {
                Avatar(
                    face = face,
                    badge = badge,
                    modifier = Modifier().width(100.dp).height(100.dp).margin(15.dp)
                )
                Avatar(
                    face = face,
                    pendant = pendant,
                    badge = badge,
                    modifier = Modifier().width(100.dp).height(100.dp).margin(15.dp)
                )
            }
        }
    }

    @Test
    fun `test decorate`(): Unit = runBlocking {

        val ornament1 = loadTestImage("image", "ornament1.png")
        val ornament2 = loadTestImage("image", "ornament2.png")

        View(
            file = testOutput.resolve("decorate.png"),
            modifier = Modifier().width(400.dp).background(Color.WHITE.withAlpha(0.6f))
        ) {
            Column (Modifier().fillMaxWidth().padding(20.dp), LayoutAlignment.CENTER) {
                Decorate(
                    image = ornament1,
                    modifier = Modifier().height(125.dp).margin(15.dp) // .background(Color.RED)
                )
                Decorate(
                    image = ornament2,
                    numStr = "000001",
                    color = Color.makeRGB(240, 146, 218),
                    modifier = Modifier().height(125.dp).margin(15.dp) // .background(Color.RED)
                )
            }
        }
    }

    @Test
    fun `test author`(): Unit = runBlocking {

        val face = loadTestImage("image", "avatar.jpg")
        val pendant = loadTestImage("image", "pendant.png")
        val ornament1 = loadTestImage("image", "ornament1.png")
        val ornament2 = loadTestImage("image", "ornament2.png")

        View(
            file = testOutput.resolve("author.png"),
            modifier = Modifier().width(900.dp).padding(20.dp).background(Color.WHITE.withAlpha(0.6f))
        ) {
            Box(Modifier().fillMaxWidth().background(Color.WHITE.withAlpha(0.6f)).border(3.dp, 15.dp)) {
                Author(
                    face = face,
                    pendant = pendant,
                    verify = OfficialVerifyType.PERSONA,
                    name = "猫芒ベル_Official",
                    time = "2023年03月14月 22:00:45",
                    ornament = ornament2,
                    numStr = "000001",
                    color = Color.makeRGB(240, 146, 218),
                    modifier = Modifier().fillMaxWidth().height(100.dp).margin(horizontal = 5.dp, vertical = 20.dp) // .background(Color.RED)
                )
            }

        }
    }

    @Test
    fun `test small author`(): Unit = runBlocking {

        val face = loadTestImage("image", "avatar.jpg")

        View(
            file = testOutput.resolve("small_author.png"),
            modifier = Modifier().width(900.dp).padding(20.dp).background(Color.WHITE.withAlpha(0.6f))
        ) {
            Box(Modifier().fillMaxWidth().background(Color.WHITE.withAlpha(0.6f)).border(3.dp, 15.dp)) {
                SmallAuthor(
                    face = face,
                    verify = OfficialVerifyType.PERSONA,
                    name = "猫芒ベル_Official",
                    time = "2023年03月14月 22:00:45",
                    modifier = Modifier().fillMaxWidth().height(50.dp).margin(horizontal = 5.dp, vertical = 10.dp) // .background(Color.RED)
                )
            }

        }
    }

    @Test
    fun `test video`(): Unit = runBlocking {
        val cover = loadTestImage("image", "bg1.jpg")

        View(
            file = testOutput.resolve("video.png"),
            modifier = Modifier().width(900.dp).padding(20.dp).background(Color.makeRGB(204, 222, 255))
        ) {
            Media(
                cover = cover,
                title = "班班幼儿园的地底下藏着秘密，准备开始上课了！",
                desc = "❤️喜欢我的视频不妨【关注我】【点赞评论】【一键三连】吧！\n" +
                        "❌请勿在其他UP主视频内刷写我的名字，包括但不限于音乐、社交及其他平台。\n" +
                        "❌不要轻易相信其他地方自称为“薄海纸鱼”的人的言论或求助，谨防受骗。\n" +
                        "❌我的视频仅为个人分享，观点仅代表个人，请勿用我的观点去引战指责或吵架。",
                duration = "1P  43:33",
                info = "1.8万观看 104弹幕",
                tag = "视频",
                modifier = Modifier()
                    .fillMaxWidth()
                    .margin(20.dp)
                    .background(Color.WHITE.withAlpha(0.6f))
                    .border(3.dp, 15.dp)
                    .shadows(Shadow.ELEVATION_2)
            )
        }
    }

    @Test
    fun `test small video`(): Unit = runBlocking {
        val cover = loadTestImage("image", "bg1.jpg")

        View(
            file = testOutput.resolve("small_video.png"),
            modifier = Modifier().width(900.dp).padding(20.dp).background(Color.WHITE.withAlpha(0.6f))
        ) {
            SmallMedia(
                cover = cover,
                title = "【直播回放】B限♡视频鉴赏会 2023年11月30日20点场 ",
                desc = "",
                duration = "43:33",
                tag = "直播回放",
                modifier = Modifier()
                    .fillMaxWidth()
                    .height(200.dp)
                    .margin(20.dp)
                    .background(Color.WHITE.withAlpha(0.6f))
                    .border(3.dp, 15.dp)
            )
        }
    }

    @Test
    fun `test article`(): Unit = runBlocking {
        val banner = loadTestImage("image", "banner.jpg")

        View(
            file = testOutput.resolve("article.png"),
            modifier = Modifier().width(900.dp).padding(20.dp).background(Color.makeRGB(204, 222, 255))
        ) {
            Media(
                cover = banner,
                title = "「和光同尘」版本更新维护公告",
                desc = "各位亲爱的侍主大人： 真理圣殿办事处将于近期进行版本更新维护，在维护完成后侍主大人将体验到「和光同尘」版本全新内容，" +
                        "维护期间米伦世界将暂时无法访问，请各位侍主大人合理安排游戏时间。更新时间 11月30日 09:30 ~ 15:30更新内容 1. " +
                        "「和光同尘」常规活动限时开放！活动时间：11月30日维护结束后~12月14日10:00致亲爱的牧：我现在在异世界一个",
                tag = "专栏",
                coverRatio = Ratio.BANNER,
                modifier = Modifier()
                    .fillMaxWidth()
                    .margin(20.dp)
                    .background(Color.WHITE.withAlpha(0.6f))
                    .border(3.dp, 15.dp)
                    .shadows(Shadow.ELEVATION_2)
            )
        }
    }

    @Test
    fun `test music`(): Unit = runBlocking {
        val cover = loadTestImage("image", "avatar.jpg")

        View(
            file = testOutput.resolve("music.png"),
            modifier = Modifier().width(900.dp).padding(20.dp).background(Color.makeRGB(204, 222, 255))
        ) {
            MiniMedia(
                cover = cover,
                title = "【ゆう十】Telecaster B-Boy【钢琴版】",
                desc = "音乐 · 人声演唱",
                tag = "音乐",
                modifier = Modifier()
                    .fillMaxWidth()
                    .height(100.dp)
                    .margin(20.dp)
                    .background(Color.WHITE.withAlpha(0.6f))
                    .border(3.dp, 15.dp)
                    .shadows(Shadow.ELEVATION_2)
            )
        }
    }

    @Test
    fun `test blocked`(): Unit = runBlocking {
        val bg = loadTestImage("image", "blocked_bg.png")
        val icon = loadTestImage("image", "blocked_icon.png")

        View(
            file = testOutput.resolve("blocked.png"),
            modifier = Modifier().width(900.dp).padding(20.dp).background(Color.makeRGB(204, 222, 255))
        ) {
            Box(
                modifier = Modifier()
                    .fillMaxWidth()
                    .margin(20.dp)
                    .background(Color.WHITE.withAlpha(0.6f))
                    .border(3.dp, 15.dp)
                    .shadows(Shadow.ELEVATION_2)
            ) {
                Image(
                    image = bg,
                    ratio = Ratio.SQUARE,
                    modifier = Modifier().fillMaxWidth().fillMaxHeight()
                )
                Column(
                    alignment = LayoutAlignment.CENTER
                ) {
                    Image(
                        image = icon,
                        ratio = Ratio.SQUARE,
                        alignment = LayoutAlignment.TOP_CENTER,
                        modifier = Modifier().width(150.dp).height(150.dp).margin(bottom = 20.dp)
                    )

                    Text(
                        text = "小小料王专属动态\n加入当前UP主的任一包月充电即可解锁观看",
                        fontSize = 30.dp,
                        maxLinesCount = 5,
                        alignment = LayoutAlignment.CENTER
                    )


                }

            }
        }

    }


}
