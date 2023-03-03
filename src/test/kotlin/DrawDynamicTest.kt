package top.colter

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import org.jetbrains.skia.Color
import org.junit.After
import org.junit.Before
import org.junit.Test
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.data.DynamicItem
import top.colter.mirai.plugin.bilibili.draw.drawDynamic
import top.colter.mirai.plugin.bilibili.tasker.DynamicMessageTasker.buildMessage
import java.io.File

class DrawDynamicTest {
    private val decoder = Json{
        ignoreUnknownKeys = true
    }
    private val dids = listOf(
        "767166448722247682",
        "MASKED_sponsor_only_unlocked"
    )

    @OptIn(ConsoleExperimentalApi::class)
    @Before
    fun initPlugin() = runBlocking {
        MiraiConsoleTerminalLoader.startAsDaemon()
        BiliBiliDynamic.load()
        BiliBiliDynamic.enable()
    }

    private fun loadTestResource(path: String)
        = File("src/test/resources/").resolve(path)

    private fun loadDynamicItemString(did: String)
        = loadTestResource("json/dynamic_item").resolve("$did.json").readText()

    private suspend fun DynamicItem.drawAndSave(path: String){
        val img = drawDynamic(Color.CYAN, false)
        loadTestResource("output/$path.png").writeBytes(img.encodeToData()!!.bytes)
    }

    private fun decodeToDynamicItem(did: String): DynamicItem
        = decoder.decodeFromString(loadDynamicItemString(did))

    @Test
    fun drawDynamic(): Unit = runBlocking {
        dids.forEach { decodeToDynamicItem(it).drawAndSave(it) }
    }

    @Test
    fun buildDynamicMsg() = runBlocking{
        dids.forEach { did ->
            decodeToDynamicItem(did).buildMessage().apply {
            println("============== ${this.did}==============")
            println("content: $content")
            images?.forEach {
                println(it)
                }
            }
            println()
        }
    }

    @OptIn(ConsoleExperimentalApi::class)
    @After
    fun cleanup() = runBlocking {
        MiraiConsole.shutdown()
    }
}