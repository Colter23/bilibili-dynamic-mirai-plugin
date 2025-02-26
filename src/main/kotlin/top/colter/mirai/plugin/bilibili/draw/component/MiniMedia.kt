package top.colter.mirai.plugin.bilibili.draw.component

import org.jetbrains.skia.Color
import org.jetbrains.skia.Image
import top.colter.skiko.*
import top.colter.skiko.data.LayoutAlignment
import top.colter.skiko.data.Ratio
import top.colter.skiko.data.Shadow
import top.colter.skiko.layout.*


fun Layout.MiniMedia(
    cover: Image? = null,
    title: String,
    desc: String,
    tag: String? = null,
    modifier: Modifier = Modifier().height(100.dp)
) = Row ( modifier = modifier ) {
    require(modifier.height.isNotNull()) { "必须指定高度" }

    if (cover != null) {
        Box(
            modifier = Modifier().fillMaxHeight()
        ) {
            // 封面
            Image(
                image = cover,
                ratio = Ratio.SQUARE,
                modifier = Modifier().border(2.dp, 10.dp)
            )

        }
    }

    // 标题简介
    Column(
        modifier = Modifier().fillWidth().fillMaxHeight().margin(15.dp)
    ) {
        Box(Modifier().fillMaxWidth().fillRatioHeight(0.4f)) {
            Text(
                text = title,
                fontSize = 23.dp,
                maxLinesCount = 2,
                alignment = LayoutAlignment.CENTER_LEFT,
                modifier = Modifier().fillMaxWidth()
            )
        }
        Box(Modifier().fillMaxWidth().fillRatioHeight(0.6f)) {
            Text(
                text = desc,
                color = Color.BLACK.withAlpha(0.7f),
                maxLinesCount = 4,
                alignment = LayoutAlignment.CENTER_LEFT,
                modifier = Modifier().fillMaxWidth()
            )
        }
    }

    // TAG
    if (!tag.isNullOrBlank()) {
        Box(
            alignment = LayoutAlignment.TOP_RIGHT,
            modifier = Modifier()
                .padding(horizontal = 20.dp, vertical = 5.dp)
                .background(color = Color.makeRGB(251, 114, 153))
                .border(2.dp, listOf(0.dp, 10.dp, 0.dp, 10.dp))
                .shadows(Shadow.ELEVATION_2)
        ) {
            Text(
                text = tag,
                fontSize = 20.dp,
                color = Color.WHITE,
                modifier = Modifier().maxWidth(200.dp).margin(top = (-3).dp)
            )
        }
    }
}