package top.colter.mirai.plugin.bilibili.draw

import org.jetbrains.skia.Color
import top.colter.bilibili.data.dynamic.content.DynamicMajor
import top.colter.bilibili.data.dynamic.major.*
import top.colter.bilibili.data.dynamic.type.MajorType.*
import top.colter.mirai.plugin.bilibili.draw.component.Media
import top.colter.mirai.plugin.bilibili.draw.component.MiniMedia
import top.colter.mirai.plugin.bilibili.draw.component.SmallMedia
import top.colter.skiko.*
import top.colter.skiko.data.LayoutAlignment
import top.colter.skiko.data.Ratio
import top.colter.skiko.data.Shadow
import top.colter.skiko.layout.*


fun Layout.majorDraw(major: DynamicMajor) {

    when (major.type) {
        ARCHIVE -> videoMajor(major.video!!)
        DRAW -> drawMajor(major.draw!!)
        ARTICLE -> articleMajor(major.article!!)
        OPUS -> opusMajor(major.opus!!)
        MUSIC -> musicMajor(major.music!!)
        LIVE -> liveMajor(major.live!!)
        LIVE_RCMD -> liveRcmdMajor(major.liveRcmd!!)
        PGC -> pgcMajor(major.pgc!!)
        COMMON -> commonMajor(major.common!!)
        UGC_SEASON -> seasonMajor(major.ugcSeason!!)
        BLOCKED -> blockedMajor(major.blocked!!)
        MEDIALIST -> mediaListMajor(major.mediaList!!)
        NONE -> infoMajor(major.none!!.tips!!)
        UNKNOWN -> infoMajor("无法绘制类型为 [${major.type.info}] 的动态类型, 请把动态链接反馈给开发者")
    }

}


val majorModifier = Modifier()
    .fillMaxWidth()
    .margin(20.dp)
//    .background(Color.WHITE.withAlpha(0.6f))
//    .border(3.dp, 15.dp)
//    .shadows(Shadow.ELEVATION_2)


// 视频
private fun Layout.videoMajor(video: MajorVideo) {
    // 如果为转发消息，使用小视频组件
    if (containsEnv("forward") || video.badge.text == "直播回放") {
        SmallMedia(
            cover = video.cover.image?.makeImage()!!,
            title = video.title,
            desc = video.description,
            tag = if (video.badge.text == "直播回放") "直播回放" else null,
            duration = video.duration,
            modifier = majorModifier.height(200.dp)
        )
    }else {
        Media(
            cover = video.cover.image?.makeImage()!!,
            title = video.title,
            desc = video.description,
            tag = "视频",
            duration = video.duration,
            info = "${video.stats.play}观看 ${video.stats.danmaku}弹幕",
            modifier = majorModifier.height(Dp.NULL)
        )
    }
}


// 图片
private fun Layout.drawMajor(draw: MajorDraw) {
    val imgList = draw.images.map { it.src.image?.makeImage()!! }
    val imgModifier = Modifier().background(Color.WHITE.withAlpha(0.6f)).border(2.dp, 10.dp).shadows(Shadow.ELEVATION_1)
    if (imgList.size == 1) {
        Image(image = imgList.first(), modifier = imgModifier)
    } else {
        val lineCount = if (imgList.size == 2 || imgList.size == 4) 2 else 3
        Grid(maxLineCount = lineCount, space = 15.dp, modifier = Modifier().fillMaxWidth()) {
            for (element in imgList) Image(element, modifier = imgModifier)
        }
    }
}


// 专栏
private fun Layout.articleMajor(article: MajorArticle) {
    Media(
        cover = article.covers.first().image?.makeImage()!!,
        title = article.title,
        desc = article.description,
        tag = "专栏",
        modifier = majorModifier.height(Dp.NULL)
    )
}


private fun Layout.opusMajor(opus: MajorOpus) {
    TODO("Not yet implemented")
}

// 音乐
private fun Layout.musicMajor(music: MajorMusic) {
    MiniMedia(
        cover = music.cover.image?.makeImage(),
        title = music.title,
        desc = music.label,
        tag = "音乐",
        modifier = majorModifier.height(100.dp)
    )
}

private fun Layout.liveMajor(live: MajorLive) {
    SmallMedia(
        cover = live.cover.image?.makeImage()!!,
        title = live.title,
        desc = "${live.descFirst}   ${live.descSecond}",
        tag = "直播",
        modifier = majorModifier.height(200.dp)
    )
}

private fun Layout.liveRcmdMajor(liveRcmd: MajorLiveRcmd) {
    SmallMedia(
        cover = liveRcmd.liveInfo.livePlayInfo.cover.image?.makeImage()!!,
        title = liveRcmd.liveInfo.livePlayInfo.title,
        desc = "${liveRcmd.liveInfo.livePlayInfo.parentAreaName}   ${liveRcmd.liveInfo.livePlayInfo.watchedShow.textLarge}",
        tag = "直播",
        modifier = majorModifier.height(200.dp)
    )
}

private fun Layout.pgcMajor(pgc: MajorPgc) {
    SmallMedia(
        cover = pgc.cover.image?.makeImage()!!,
        title = pgc.title,
        desc = "${pgc.stats.play}播放   ${pgc.stats.danmaku}弹幕",
        tag = pgc.badge.text,
        modifier = majorModifier.height(200.dp)
    )
}

private fun Layout.commonMajor(common: MajorCommon) {
    MiniMedia(
        cover = common.cover.image?.makeImage(),
        title = common.title,
        desc = common.desc,
        tag = common.badge.text,
        modifier = majorModifier.height(100.dp)
    )
}

private fun Layout.seasonMajor(ugcSeason: MajorVideo) {
    SmallMedia(
        cover = ugcSeason.cover.image?.makeImage()!!,
        title = ugcSeason.title,
        desc = ugcSeason.description,
        tag = ugcSeason.badge.text,
        duration = ugcSeason.duration,
        modifier = majorModifier.height(200.dp)
    )
}

private fun Layout.blockedMajor(blocked: MajorBlocked) {
    Box(majorModifier) {
        Image(
            image = blocked.bgImg.imgDay.image?.makeImage()!!,
            ratio = Ratio.SQUARE,
            modifier = Modifier().fillMaxWidth().fillMaxHeight()
        )
        Column(
            alignment = LayoutAlignment.CENTER
        ) {
            Image(
                image = blocked.icon.imgDay.image?.makeImage()!!,
                ratio = Ratio.SQUARE,
                alignment = LayoutAlignment.TOP_CENTER,
                modifier = Modifier().width(150.dp).height(150.dp).margin(bottom = 20.dp)
            )

            Text(
                text = blocked.hintMessage,
                fontSize = 30.dp,
                maxLinesCount = 5,
                alignment = LayoutAlignment.CENTER
            )
        }
    }
}

private fun Layout.mediaListMajor(mediaList: MajorMediaList) {
    SmallMedia(
        cover = mediaList.cover.image?.makeImage()!!,
        title = mediaList.title,
        desc = mediaList.subTitle,
        tag = mediaList.badge.text,
        modifier = majorModifier.height(200.dp)
    )
}

private fun Layout.infoMajor(text: String) {
    Box(majorModifier) {
        Text(
            text = text,
            fontSize = 30.dp,
            maxLinesCount = 5,
            alignment = LayoutAlignment.CENTER
        )
    }
}