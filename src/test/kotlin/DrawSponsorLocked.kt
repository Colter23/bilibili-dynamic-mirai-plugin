package top.colter

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.junit.After
import org.junit.Before
import org.junit.Test
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.data.ModuleDynamic
import top.colter.mirai.plugin.bilibili.draw.*
import top.colter.mirai.plugin.bilibili.utils.CacheType
import top.colter.mirai.plugin.bilibili.utils.FontUtils
import top.colter.mirai.plugin.bilibili.utils.getOrDownloadImage
import java.io.File


// 用于绘制充电解锁图片资源
class DrawSponsorLocked {
    @OptIn(ConsoleExperimentalApi::class)
    @Before
    fun initPlugin() = runBlocking {
        MiraiConsoleTerminalLoader.startAsDaemon()
        BiliBiliDynamic.load()
        BiliBiliDynamic.enable()
    }

    @OptIn(ConsoleExperimentalApi::class)
    @After
    fun cleanup() = runBlocking {
        MiraiConsole.shutdown()
    }

    private val decoder = Json {
        ignoreUnknownKeys = true
    }

    private val sponsorBlockedString = """
        {
              "bg_img": {
                "img_dark": "https://i0.hdslb.com/bfs/activity-plat/static/20221216/c103299ba3500e5000d47f2f0f04712d/wBIsPss7VZ.png",
                "img_day": "https://i0.hdslb.com/bfs/activity-plat/static/20221216/c103299ba3500e5000d47f2f0f04712d/eqeFwt8kUe.png"
              },
              "blocked_type": 1,
              "button": {
                "icon": "https://i0.hdslb.com/bfs/activity-plat/static/20230112/3b3c5705bda98d50983f6f47df360fef/qcRJ6sJU91.png",
                "jump_url": "https://www.bilibili.com/h5/upower/index?navhide=1\u0026mid=107251863\u0026prePage=onlyFansDynMdlBlocked",
                "text": "充电"
              },
              "hint_message": "该动态为包月充电专属\n可以给UP主充电后观看",
              "icon": {
                "img_dark": "https://i0.hdslb.com/bfs/activity-plat/static/20221216/c103299ba3500e5000d47f2f0f04712d/RP513ypCyt.png",
                "img_day": "https://i0.hdslb.com/bfs/activity-plat/static/20221216/c103299ba3500e5000d47f2f0f04712d/8gweMAFDvP.png"
              }
            }
    """.trimIndent()
    private val sponsorBlocked: ModuleDynamic.Major.Blocked = decoder.decodeFromString(sponsorBlockedString)

    private suspend fun ModuleDynamic.Major.Blocked.drawAndSave(path: String) {
        val paragraphStyle = ParagraphStyle().apply {
            maxLinesCount = 2
            ellipsis = "..."
            alignment = Alignment.CENTER
            textStyle = titleTextStyle.apply {
                color = Color.WHITE
            }
        }
        val hintMessage = ParagraphBuilder(paragraphStyle, FontUtils.fonts).addText(hintMessage).build()
        val buttonMessage = ParagraphBuilder(paragraphStyle, FontUtils.fonts).addText(button.text).build()
        val bgImage = getOrDownloadImage(bgImg.imgDark, CacheType.IMAGES)!!
        val lockIcon = getOrDownloadImage(icon.imgDark, CacheType.IMAGES)!!
        val buttonIcon = getOrDownloadImage(button.icon)!!

        val bgWidth = cardContentRect.width - quality.cardPadding * 2
        val bgHeight = bgWidth / bgImage.width * bgImage.height

        val lockWidth = bgWidth / 7
        val lockHeight = lockWidth / lockIcon.width * lockIcon.height

        return Surface.makeRasterN32Premul(
            bgWidth.toInt(), bgHeight.toInt()
        ).apply {
            canvas.apply {
                var x = 0f
                var y = 0f
                drawImageClip(bgImage, RRect.makeXYWH(x, y, bgWidth, bgHeight, quality.cardArc))
                x += (bgWidth - lockWidth) / 2
                y += bgHeight / 4
                drawImageClip(lockIcon, RRect.makeXYWH(x, y, lockWidth, lockHeight, quality.cardArc))

                x = 0f
                y += lockHeight + quality.drawSpace
                hintMessage.layout(bgWidth).paint(this, x, y)

                x =  bgWidth / 8 * 3
                y += hintMessage.height + 2 * quality.drawSpace
                val buttonWidth = bgWidth / 4
                val buttonHeight = buttonWidth / 3
                drawRRect(
                    RRect.Companion.makeXYWH(x, y, buttonWidth, buttonHeight, 50f),
                    Paint().apply {
                        color = Color.makeRGB("#ff679a")
                        mode = PaintMode.FILL
                        isAntiAlias = false
                    }
                )

                val iconHeight = buttonHeight / 3 * 2
                x += buttonWidth / 4
                drawImageClip(buttonIcon, RRect.Companion.makeXYWH(x, y + buttonHeight / 6, iconHeight, iconHeight, quality.cardArc))

                x -= iconHeight / 8
                y += buttonHeight / 8
                buttonMessage.layout(buttonWidth - iconHeight).paint(this, x, y)
            }
        }.saveImage(path)
    }

    @Test
    fun drawAndSave() = runBlocking {
        val file = File("src/test/resources/output/SponsorBlocked.png")
        sponsorBlocked.drawAndSave(file.path)
    }
}