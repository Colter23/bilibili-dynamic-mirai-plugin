package top.colter.mirai.plugin.bilibili.draw.component

import org.jetbrains.skia.*
import top.colter.bilibili.data.user.OfficialVerifyType
import top.colter.mirai.plugin.bilibili.draw.loadSVG
import top.colter.mirai.plugin.bilibili.draw.makeImage
import top.colter.skiko.*
import top.colter.skiko.data.LayoutAlignment
import top.colter.skiko.layout.*
import java.io.File

/**
 * 作者组件
 */
fun Layout.Author(
    face: Image,
    pendant: Image? = null,
    verify: OfficialVerifyType = OfficialVerifyType.NONE,
    name: String,
    time: String,
    ornament: Image,
    numStr: String? = null,
    color: Int? = null,
    alignment: LayoutAlignment = LayoutAlignment.CENTER,
    modifier: Modifier
) = Row (
    alignment = alignment,
    modifier = modifier
) {
    require(modifier.height.isNotNull()) { "必须指定高度" }

    val badgeImage = when (verify) {
        OfficialVerifyType.NONE -> null
        OfficialVerifyType.PERSONA -> loadSVG(File("src/main/resources/icon/PERSONAL_OFFICIAL_VERIFY.svg").readBytes()).makeImage(100, 100)
        OfficialVerifyType.ORGANIZATION -> loadSVG(File("src/main/resources/icon/ORGANIZATION_OFFICIAL_VERIFY.svg").readBytes()).makeImage(100, 100)
    }

    val ratio = 0.56f

    Avatar(
        face = face,
        pendant = pendant,
        badge = badgeImage,
        modifier = Modifier().height(modifier.height).margin(15.dp)
    )
    Column(
        modifier = Modifier().fillWidth().fillMaxHeight() // .background(Color.GREEN)
    ) {
        Box(
            modifier = Modifier().fillMaxWidth().fillRatioHeight(ratio) // .background(Color.RED)
        ) {
            Text(
                text = name,
                color = Color.makeRGB(251, 114, 153),
                fontSize = 36.dp,
                fontStyle = MEDIUM,
                fontFamily = FontUtils.defaultFont?.familyName ?: "",
//                    fontFamily = "HarmonyOS Sans",
                alignment = LayoutAlignment.CENTER_LEFT,
            )
        }
        Box(
            modifier = Modifier().fillMaxWidth().fillRatioHeight(1f - ratio) // .background(Color.YELLOW)
        ) {
            Text(
                text = time,
                color = Color.makeRGB(156, 156, 156),
                fontSize = 28.dp,
                fontStyle = MEDIUM,
                fontFamily = FontUtils.defaultFont?.familyName ?: "",
//                    fontFamily = "HarmonyOS Sans",
                alignment = LayoutAlignment.CENTER_LEFT,
            )
        }

    }
    Decorate(
        image = ornament,
        numStr = numStr,
        color = color,
        modifier = Modifier().height(modifier.height).margin(15.dp)
    )
}

val MEDIUM: FontStyle
    get() = FontStyle(FontWeight.MEDIUM, FontWidth.NORMAL, FontSlant.UPRIGHT)