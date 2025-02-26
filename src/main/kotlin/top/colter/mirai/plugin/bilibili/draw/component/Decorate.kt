package top.colter.mirai.plugin.bilibili.draw.component

import org.jetbrains.skia.Color
import org.jetbrains.skia.Image
import top.colter.skiko.FontUtils
import top.colter.skiko.Modifier
import top.colter.skiko.data.LayoutAlignment
import top.colter.skiko.dp
import top.colter.skiko.layout.Box
import top.colter.skiko.layout.Image
import top.colter.skiko.layout.Layout
import top.colter.skiko.layout.Text
import top.colter.skiko.width

val fanFont = FontUtils.loadTypeface("src/main/resources/font/FansCard.ttf")

/**
 * 装饰组件
 */
fun Layout.Decorate(
    image: Image,
    numStr: String? = null,
    color: Int? = null,
    alignment: LayoutAlignment = LayoutAlignment.CENTER,
    modifier: Modifier
) = Box (
    alignment = alignment,
    modifier = modifier
) {
    require(modifier.height.isNotNull()) { "必须指定高度" }

    val cardHeight = if (numStr == null) modifier.height * 0.6f else modifier.height
    val cardWidth = image.width.dp * cardHeight.px / image.height.dp

    modifier.width(if (numStr != null) cardWidth * 0.66f else cardWidth)

    Image(
        image = image,
        alignment = LayoutAlignment.CENTER_RIGHT,
        modifier = Modifier().width(cardWidth)
    )
    if (numStr != null) {
        Text(
            text = numStr,
            color = color?: Color.BLACK,
            fontSize = cardHeight / 3.5f,
            fontFamily = fanFont.familyName,
            alignment = LayoutAlignment.CENTER_LEFT
        )
    }
}
