package top.colter.mirai.plugin.bilibili.draw

data class Quality(
    val imageWidth: Int = 0,
    val cardMargin: Int = 0,
    val cardPadding: Int = 0,
    val cardArc: Float = 0f,

    val faceSize: Float = 0f,
    val noPendantFaceInflate: Float = 0f,
    val pendantSize: Float = 0f,
    val verifyIconSize: Float = 0f,

    val badgeHeight: Int = 0,
    val badgePadding: Int = 0,
    val badgeArc: Float = 0f,

    val lineHeight: Float = 0f,
    val lineSpace: Float = 0f,

    val mainFontSize: Float = 0f,
    val subFontSize: Float = 0f,
    val emojiSize: Float = 0f,
    val iconSize: Float = 0f
){

//    data class VideoCard(
//
//    )

    companion object{
        val low = Quality(
            imageWidth =  800,
            cardMargin =  20,
            cardPadding = 20,
            cardArc =  10f,
            faceSize = 64f,
            noPendantFaceInflate = 5f,
            pendantSize =  112f,
            verifyIconSize = 20f,

            badgeHeight = 36,
            badgePadding = 5,
            badgeArc = 5f,

            lineHeight = 30f,
            lineSpace = 5f,

            mainFontSize = 22f,
            subFontSize = 18f,
            emojiSize = 30f,
            iconSize = 26f,
        )
        val middle = Quality(
            imageWidth =  1000,
            cardMargin =  30,
            cardPadding = 30,
            cardArc =  15f,
            faceSize = 80f,
            noPendantFaceInflate = 10f,
            pendantSize =  120f,
            verifyIconSize = 25f,

            badgeHeight = 45,
            badgePadding = 8,
            badgeArc = 8f,

            lineHeight = 35f,
            lineSpace = 8f,

            mainFontSize = 26f,
            subFontSize = 22f,
            emojiSize = 36f,
            iconSize = 31f,
        )
        val high = Quality(1200, 50, 20, 35f,10f,64f,112f, 20f,20)

        fun level(level: Int): Quality {
            return when(level){
                1 -> low
                2 -> middle
                3 -> high
                else -> {
                    logger.warning("图片质量配置错误 有效配置(1, 2, 3) 当前配置: $level")
                    low
                }
            }
        }
    }



}