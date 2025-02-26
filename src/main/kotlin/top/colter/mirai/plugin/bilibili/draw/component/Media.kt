package top.colter.mirai.plugin.bilibili.draw.component

import org.jetbrains.skia.Color
import org.jetbrains.skia.Image
import top.colter.skiko.*
import top.colter.skiko.data.LayoutAlignment
import top.colter.skiko.data.Ratio
import top.colter.skiko.data.Shadow
import top.colter.skiko.layout.*

/**
 * 媒体组件
 *
 * [cover] 封面
 *
 * [title] 标题
 *
 * [desc] 简介
 *
 * [tag] 标签
 *
 * [duration] 时长
 *
 * [info] 信息
 *
 * [coverRatio] 封面比例
 *
 */
fun Layout.Media(
    cover: Image,
    title: String,
    desc: String,
    tag: String? = null,
    duration: String? = null,
    info: String? = null,
    coverRatio: Float = Ratio.COVER_1,
    modifier: Modifier = Modifier()
) = Column(
    modifier = modifier
) {
    require(modifier.width.isNotNull()) { "必须指定宽度" }

    Box(
        modifier = Modifier().fillMaxWidth()
    ) {
        // 封面
        Image(
            image = cover,
            ratio = coverRatio,
            modifier = Modifier().border(2.dp, 10.dp).shadows(Shadow.ELEVATION_1)
        )
        position

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

        // 信息
        if (!duration.isNullOrBlank() || !info.isNullOrBlank()) {
            // 遮罩
            Row(
                alignment = LayoutAlignment.BOTTOM_LEFT,
                modifier = Modifier()
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color.BLACK.withAlpha(0.5f))
                    .border(0.dp, listOf(0.dp, 0.dp, 10.dp, 10.dp))
            ) {
                // 时长
                if (!duration.isNullOrBlank()) {
                    Box(
                        alignment = LayoutAlignment.CENTER_LEFT,
                        modifier = Modifier()
                            .margin(left = 40.dp)
                            .padding(horizontal = 15.dp, vertical = 4.dp)
                            .background(color = Color.BLACK.withAlpha(0.5f))
                            .border(0.dp, 10.dp)
                    ) {
                        Text(
                            text = duration,
                            fontSize = 20.dp,
                            color = Color.WHITE,
                            modifier = Modifier().maxWidth(400.dp).margin(top = (-3).dp)
                        )
                    }
                }

                // 视频信息
                if (!info.isNullOrBlank()) {
                    Text(
                        text = info,
                        fontSize = 23.dp,
                        color = Color.WHITE,
                        alignment = LayoutAlignment.CENTER_LEFT,
                        modifier = Modifier().maxWidth(600.dp).margin(left = 15.dp)
                    )
                }

            }
        }
    }

    Text(
        text = title,
        fontSize = 23.dp,
        maxLinesCount = 2,
        modifier = Modifier().margin(top = 15.dp, right = 15.dp, bottom = 10.dp, left = 15.dp)
    )
    Text(
        text = desc,
        color = Color.BLACK.withAlpha(0.7f),
        maxLinesCount = 3,
        modifier = Modifier().margin(right = 15.dp, bottom = 15.dp, left = 15.dp)
    )
}
