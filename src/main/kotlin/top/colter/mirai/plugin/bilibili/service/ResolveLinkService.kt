package top.colter.mirai.plugin.bilibili.service

import org.jetbrains.skia.Color
import top.colter.mirai.plugin.bilibili.BiliConfig
import top.colter.mirai.plugin.bilibili.api.*
import top.colter.mirai.plugin.bilibili.data.*
import top.colter.mirai.plugin.bilibili.draw.*
import top.colter.mirai.plugin.bilibili.utils.*
import java.time.Instant


private val regex = BiliConfig.linkResolveConfig.reg

fun matchingRegular(content: String): LinkType? {
    return if (regex.any { it.find(content) != null }) {
        logger.info("开始解析链接 -> $content")
        matchingInternalRegular(content)
    } else null
}

fun matchingInternalRegular(content: String): LinkType? {
    var matchResult: MatchResult? = null
    var type: LinkType? = null

    for (linkType in LinkType.values()) {
        for (regex in linkType.regex) {
            matchResult = regex.find(content)
            if (matchResult != null) {
                type = linkType
                break
            }
        }
        if (matchResult != null) break
    }
    return if (matchResult != null && type != null) {
        type.id = matchResult.destructured.component1()
        type
    }else {
        logger.warning("未匹配到链接! -> $content")
        null
    }
}

enum class TriggerMode {
    At,
    Always,
    Never
}

interface ResolveLink {
    //suspend fun resolve(): BiliDetail?
    suspend fun drawGeneral(): String?
    suspend fun getLink(): String
}

enum class LinkType(val regex: List<Regex>, var id: String? = null): ResolveLink {
    VideoLink(listOf(
        """(?:www.bilibili.com/video/)?((?:BV[0-9A-z]{10})|(?:av\d{1,10}))""".toRegex()
    )) {
        override suspend fun drawGeneral(): String? {
            return biliClient.getVideoDetail(id!!)?.run {
                drawGeneral(id!!, "视频", pubdate.formatTime, toDrawAuthorData(), toDrawData().drawGeneral(true))
            }
        }

        override suspend fun getLink(): String = VIDEO_LINK(id!!)

    },
    Article(listOf(
        """(?:www.bilibili.com/read/)?(cv\d{1,10})""".toRegex()
    )) {
        override suspend fun drawGeneral(): String? {
            return biliClient.getArticleDetail(id!!)?.run {
                drawGeneral(id!!, "专栏", time.formatTime, author, toDrawData().drawGeneral())
            }
        }

        override suspend fun getLink(): String = ARTICLE_LINK(id!!.removePrefix("cv"))
    },
    Dynamic(listOf(
        """[tm].bilibili.com/(?:dynamic/)?(\d+)""".toRegex()
    )) {
        override suspend fun drawGeneral(): String? {
            val color = Color.makeRGB(BiliConfig.imageConfig.defaultColor)
            return biliClient.getDynamicDetail(id!!)?.run {
                val dynamic = drawDynamic(color)
                val img = makeCardBg(dynamic.height, listOf(color)) {
                    it.drawImage(dynamic, 0f, 0f)
                }
                cacheImage(img, "$idStr.png", CacheType.DRAW_SEARCH)
            }
        }

        override suspend fun getLink(): String = DYNAMIC_LINK(id!!)
    },
    Live(listOf(
        """live.bilibili.com/(?:h5/)?(\d+)""".toRegex()
    )) {
        override suspend fun drawGeneral(): String? {
            val room = biliClient.getLiveDetail(id!!) ?: return null
            val author = biliClient.userInfo(room.uid)?.toDrawAuthorData() ?: return null
            val data = room.toDrawData().drawGeneral()
            return drawGeneral(id!!, "直播", Instant.now().epochSecond.formatTime, author, data)
        }

        override suspend fun getLink(): String = LIVE_LINK(id!!)
    },
    User(listOf(
        """space.bilibili.com/(\d+)""".toRegex()
    )) {
        override suspend fun drawGeneral(): String? {
            val author = biliClient.userInfo(id!!.toLong())?.toDrawAuthorData() ?: return null
            return drawGeneral(id!!, "用户", Instant.now().epochSecond.formatTime, author, null)
        }

        override suspend fun getLink(): String = SPACE_LINK(id!!)
    },
    Pgc(listOf(
        """(?:(?:www|m).bilibili.com/bangumi/(?:play|media)/)?((?:ss|ep|md)\d+)""".toRegex()
    )) {
        override suspend fun drawGeneral(): String? {
            val info = biliClient.getPcgInfo(id!!) ?: return null
            val author = info.toPgcAuthor() ?: return null
            val data = info.toPgc()?.drawSmall()
            return drawGeneral(id!!, "番剧", Instant.now().epochSecond.formatTime, author, data)
        }

        override suspend fun getLink(): String = PGC_LINK(id!!)
    },
    ShortLink(listOf(
        """b23.tv/([0-9A-z]+)""".toRegex()
    )) {
        override suspend fun drawGeneral(): String? {
            val link = biliClient.redirect("https://b23.tv/$id")
            return if (link != null) {
                matchingInternalRegular(link)?.drawGeneral()
            }else null
        }

        override suspend fun getLink(): String = "$BASE_SHORT/$id"
    }
}

suspend fun drawGeneral(id: String, tag: String, time: String, author: ModuleAuthor, imgData: org.jetbrains.skia.Image?): String {
    val footer = buildFooter(author.name, author.mid, id, time, tag)

    val color = Color.makeRGB(BiliConfig.imageConfig.defaultColor)

    val imgList = mutableListOf(
        author.drawGeneral(time, VIDEO_LINK(id), color),
    )
    imgData?.let { imgList.add(it) }

    val cimg = imgList.assembleCard(id, footer, tag = "搜索")

    val img = makeCardBg(cimg.height, listOf(color)) {
        it.drawImage(cimg, 0f, 0f)
    }
    return cacheImage(img, "$id.png", CacheType.DRAW_SEARCH)
}

//摆烂行为

fun VideoDetail.toDrawAuthorData(): ModuleAuthor =
    ModuleAuthor(
        "AUTHOR_TYPE_NORMAL",
        owner.mid,
        owner.name,
        owner.face
    )

fun BiliUser.toDrawAuthorData(): ModuleAuthor =
    ModuleAuthor(
        "AUTHOR_TYPE_NORMAL",
        mid,
        name!!,
        face!!,
        officialVerify = official,
        vip = vip,
        pendant = pendant
    )


fun VideoDetail.toDrawData(): ModuleDynamic.Major.Archive =
    ModuleDynamic.Major.Archive(
        0,
        aid,
        bvid,
        title,
        pic,
        desc,
        duration.formatDuration(false),
        "",
        ModuleDynamic.Major.Stat(
            stat.danmaku.toString(),
            stat.view.toString()
        ),
        ModuleDynamic.Major.Badge(
            "#fb7299",
            "#ffffff",
            "视频"
        )
    )

fun ArticleDetail.toDrawData(): ModuleDynamic.Major.Article =
    ModuleDynamic.Major.Article(
        aid,
        title,
        summary,
        "$words 字",
        "",
        covers
    )

fun LiveRoomDetail.toDrawData(): ModuleDynamic.Major.Live =
    ModuleDynamic.Major.Live(
        roomId,
        title,
        cover,
        "$parentAreaName",
        "$areaName",
        "",
        liveStatus,
        0,
        ModuleDynamic.Major.Badge(
            "", "",
            when (liveStatus) {
                0 -> "未开播"
                1 -> "直播中"
                2 -> "轮播中"
                else -> "直播"
            }
        )
    )

fun BiliDetail.toPgcAuthor(): ModuleAuthor? =
    when (this) {
        is PgcSeason -> {
            ModuleAuthor(
                "AUTHOR_TYPE_PGC",
                0,
                title,
                cover,
            )
        }
        is PgcMedia -> {
            ModuleAuthor(
                "AUTHOR_TYPE_PGC",
                0,
                media.title,
                media.cover,
            )
        }

        else -> null
    }
fun BiliDetail.toPgc(): ModuleDynamic.Major.Pgc? =
    when (this) {
        is PgcSeason -> {
            ModuleDynamic.Major.Pgc(
                type,
                0,
                seasonId.toInt(),
                0,
                title,
                cover,
                "",
                ModuleDynamic.Major.Stat(
                    stat?.danmakus.toString(),
                    stat?.views.toString()
                ),
                ModuleDynamic.Major.Badge("", "", "番剧")
            )
        }
        is PgcMedia -> {
            ModuleDynamic.Major.Pgc(
                media.type,
                0,
                media.seasonId.toInt(),
                0,
                media.title,
                media.horizontalPicture,
                "",
                ModuleDynamic.Major.Stat("", ""),
                ModuleDynamic.Major.Badge("", "", media.typeName)
            )
        }

        else -> null
    }
