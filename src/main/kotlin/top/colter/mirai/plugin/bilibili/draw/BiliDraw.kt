package top.colter.mirai.plugin.bilibili.draw

import org.jetbrains.skia.Color
import org.jetbrains.skia.Image
import top.colter.mirai.plugin.bilibili.database.DrawConfig
import top.colter.skiko.*
import top.colter.skiko.data.Gradient
import top.colter.skiko.layout.Layout
import top.colter.skiko.layout.View


suspend fun BiliDraw(draw: suspend Layout.() -> Unit): Image? {
    if (!DrawConfig.enable) return null
     val image = View(
        modifier = Modifier()
            .width(1000.dp)
            .padding(30.dp)
            .background(gradient = Gradient(10, listOf(Color.RED, Color.GREEN)))
    ) {
        draw()
    }



    return image
}