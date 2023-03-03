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
import org.junit.After
import org.junit.Before
import org.junit.Test
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.data.ModuleDynamic
import top.colter.mirai.plugin.bilibili.draw.*
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

    @Test
    fun drawAndSave(): Unit = runBlocking {
        val file = File("src/test/resources/output/SponsorBlocked.png")
        sponsorBlocked.drawGeneral().encodeToData()?.let { file.writeBytes(it.bytes) }
    }
}