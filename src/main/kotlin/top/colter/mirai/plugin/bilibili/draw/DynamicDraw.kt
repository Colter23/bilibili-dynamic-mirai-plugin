package top.colter.mirai.plugin.bilibili.draw

import kotlinx.coroutines.*
import org.jetbrains.skia.Color
import org.jetbrains.skia.Image
import org.jetbrains.skia.paragraph.TextStyle
import top.colter.bilibili.data.LazyImage
import top.colter.bilibili.data.dynamic.BiliDynamic
import top.colter.bilibili.data.dynamic.author
import top.colter.bilibili.data.dynamic.content
import top.colter.bilibili.data.dynamic.major
import top.colter.bilibili.data.dynamic.type.RichTextType
import top.colter.bilibili.tools.forEachLazyImageFields
import top.colter.mirai.plugin.bilibili.draw.component.Author
import top.colter.mirai.plugin.bilibili.draw.component.SmallAuthor
import top.colter.mirai.plugin.bilibili.tools.CacheType
import top.colter.mirai.plugin.bilibili.tools.cacheImage
import top.colter.mirai.plugin.bilibili.tools.getOrDownload
import top.colter.skiko.*
import top.colter.skiko.data.RichParagraphBuilder
import top.colter.skiko.layout.Column
import top.colter.skiko.layout.Layout
import top.colter.skiko.layout.RichText
import kotlin.coroutines.coroutineContext


suspend fun DynamicDraw(dynamic: BiliDynamic): Image? {
    CoroutineScope(coroutineContext).launch {
        val list = mutableListOf<Deferred<Pair<LazyImage, ByteArray?>>>()
        forEachLazyImageFields(dynamic) {
            if (url.isNotBlank()) {
                list.add(async {
                    Pair(this@forEachLazyImageFields, getOrDownload(url, CacheType.IMAGES) )
                })
            }
        }
        list.awaitAll().forEach {
            it.first.image = it.second
        }
    }.join()

    val draw = BiliDraw {
        DynamicView(dynamic)
    }
    cacheImage(draw, "${dynamic.mid}/${dynamic.id}.png", CacheType.DRAW_DYNAMIC)
    return draw
}

//val BiliDynamic.imageMap: Map<String, String> by lazy {
//    return mapOf("" to "")
//}
//    get() {
//        return mapOf("" to "")
//    }


fun Layout.DynamicView(dynamic: BiliDynamic) {

    val face = dynamic.author.face.image?.makeImage()!!
    val verify = dynamic.author.official?.type!!
    val pendant = dynamic.author.pendant?.image?.image?.makeImage()
    val ornament = dynamic.author.decorate?.image?.image?.makeImage()!!

    val name = dynamic.name
    val time = dynamic.formatTime

    val numStr = dynamic.author.decorate?.fan?.numStr
    val color = dynamic.author.decorate?.fan?.color?.let { Color.makeRGB(it) }


    Column(modifier = Modifier()
        .fillMaxWidth()
        .padding(20.dp)
        .background(Color.WHITE.withAlpha(0.6f))
        .border(3.dp, 15.dp)
    ) {

        if (containsEnv("forward")) {
            SmallAuthor(
                face = face,
                verify = verify,
                name = name,
                time = time,
                modifier = Modifier().fillMaxWidth().height(50.dp).margin(horizontal = 5.dp, vertical = 10.dp) // .background(Color.RED)
            )
        } else {
            Author(
                face = face,
                pendant = pendant,
                verify = verify,
                name = name,
                time = time,
                ornament = ornament,
                numStr = numStr,
                color = color,
                modifier = Modifier().fillMaxWidth().height(100.dp).margin(horizontal = (-15).dp, vertical = 10.dp) // .background(Color.RED)
//                modifier = Modifier().fillMaxWidth().height(100.dp).margin(top = 10.dp, right = (-15).dp, bottom = 30.dp, left = (-15).dp) // .background(Color.RED)
            )
        }

        if (dynamic.content != null) {
            val style = TextStyle().setColor(Color.BLACK).setFontSize(30.px).setFontFamily(FontUtils.defaultFont!!.familyName)
            val linkStyle = TextStyle().setColor(Color.makeRGB(23, 139, 207)).setFontSize(30.px).setFontFamily(FontUtils.defaultFont!!.familyName)
            val paragraph = RichParagraphBuilder(style)

            dynamic.content?.richTextNodes?.forEach {
                when (it.type) {
                    RichTextType.AT,
                    RichTextType.WEB,
                    RichTextType.VOTE,
                    RichTextType.BV,
                    RichTextType.GOODS,
                    RichTextType.TOPIC -> paragraph.addText(it.text, linkStyle)
                    RichTextType.TEXT,
                    RichTextType.UNKNOWN -> paragraph.addText(it.text)
                    RichTextType.EMOJI -> paragraph.addEmoji(it.text, it.emoji?.iconUrl?.image?.makeImage()!!)
                }
            }

            RichText(
                paragraph = paragraph.build(),
                modifier = Modifier().margin(vertical = 20.dp)
            )
        }

        dynamic.major?.let {
            majorDraw(it)
        }

        dynamic.origin?.let {
            putEnv("forward", true)
            DynamicView(it)
            removeEnv("forward")
        }

    }
//    }

}