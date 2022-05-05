package top.colter.mirai.plugin.bilibili.utils

import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyleSet
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.makeFromFile
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.TypefaceFontProvider

object FontUtils {

    private val fontMgr = FontMgr.default
    private val fontProvider = TypefaceFontProvider()
    val fonts = FontCollection().setDefaultFontManager(fontMgr).setDynamicFontManager(fontProvider)

    fun registerTypeface(typeface: Typeface?, alias: String? = null) =
        fontProvider.registerTypeface(typeface, alias)

    fun matchFamily(familyName: String): FontStyleSet {
        val fa = fontProvider.matchFamily(familyName)
        if (fa.count() != 0){
            return fa
        }else {
            return fontMgr.matchFamily(familyName)
        }
    }

    fun loadTypeface(path: String, index: Int = 0): Typeface {
        val face = Typeface.makeFromFile(path, index)
        registerTypeface(face)
        return face
    }

}