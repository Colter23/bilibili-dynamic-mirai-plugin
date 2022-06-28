package top.colter.mirai.plugin.bilibili.utils

import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.TypefaceFontProvider

object FontUtils {

    private val fontMgr = FontMgr.default
    private val fontProvider = TypefaceFontProvider()
    val fonts = FontCollection().setDynamicFontManager(fontProvider).setDefaultFontManager(fontMgr)

    var defaultFont: Typeface? = null

    private fun registerTypeface(typeface: Typeface?, alias: String? = null) {
        fontProvider.registerTypeface(typeface)
        if (alias!=null) fontProvider.registerTypeface(typeface, alias)
    }


    fun matchFamily(familyName: String): FontStyleSet {
        val fa = fontProvider.matchFamily(familyName)
        if (fa.count() != 0) {
            return fa
        } else {
            return fontMgr.matchFamily(familyName)
        }
    }

    fun loadTypeface(path: String, alias: String? = null, index: Int = 0): Typeface {
        val face = Typeface.makeFromFile(path, index)
        if (defaultFont == null) defaultFont = face
        registerTypeface(face, alias)
        logger.info("加载字体 ${face.familyName} 成功")
        return face
    }

    fun loadTypeface(data: Data, index: Int = 0): Typeface {
        val face = Typeface.makeFromData(data, index)
        registerTypeface(face)
        return face
    }

}