package top.colter.mirai.plugin.bilibili.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.colter.mirai.plugin.bilibili.utils.decode


/**
 * 动态列表
 * @param hasMore 是否有更多动态
 * @param offset 动态偏移
 * @param updateBaseline
 * @param updateNum 更新数量
 * @param items 动态列表
 */
@Serializable
data class DynamicList(
    @SerialName("has_more")
    val hasMore: Boolean = false,
    @SerialName("offset")
    val offset: String,
    @SerialName("update_baseline")
    val updateBaseline: String,
    @SerialName("update_num")
    val updateNum: String,

    @SerialName("items")
    val items: List<DynamicItem>,
)

@Serializable
data class DynamicDetail(
    @SerialName("item")
    val item: DynamicItem,
    @SerialName("contact")
    val contact: String? = null
)

enum class DynamicType(val text: String) {
    DYNAMIC_TYPE_WORD("动态"),
    DYNAMIC_TYPE_DRAW("动态"),
    DYNAMIC_TYPE_ARTICLE("专栏"),
    DYNAMIC_TYPE_FORWARD("转发动态"),
    DYNAMIC_TYPE_AV("投稿视频"),
    DYNAMIC_TYPE_MUSIC("音乐"),
    DYNAMIC_TYPE_LIVE("直播"),
    DYNAMIC_TYPE_LIVE_RCMD("直播"),
    DYNAMIC_TYPE_PGC("番剧"),
    DYNAMIC_TYPE_COMMON_SQUARE("动态"),
    DYNAMIC_TYPE_COMMON_VERTICAL("动态"),
    DYNAMIC_TYPE_NONE("动态被删除"),
    DYNAMIC_TYPE_UNKNOWN("未知的动态"),
}


/**
 * 动态项
 * @param type 动态类型
 * @param idStr ID字符串
 * @param visible 是否显示当前动态(相关动态/合作视频)
 * @param basic 评论相关?
 * @param modules 动态模块
 * @param orig 转发的源动态
 */
@Serializable
data class DynamicItem(
    /**
     * DYNAMIC_TYPE_WORD           文字动态
     * DYNAMIC_TYPE_DRAW           图片动态
     * DYNAMIC_TYPE_ARTICLE        文章
     * DYNAMIC_TYPE_FORWARD        转发动态
     * DYNAMIC_TYPE_AV             视频
     * DYNAMIC_TYPE_MUSIC          音乐
     * DYNAMIC_TYPE_LIVE           直播
     * DYNAMIC_TYPE_LIVE_RCMD      直播
     * DYNAMIC_TYPE_PGC            番剧
     * DYNAMIC_TYPE_COMMON_SQUARE  活动
     * DYNAMIC_TYPE_NONE           动态被删除
     */
    @SerialName("type")
    val typeStr: String,

    @SerialName("id_str")
    val idStr: String?,
    @SerialName("visible")
    val visible: Boolean = true,
    @SerialName("basic")
    val basic: DynamicBasic? = null,
    @SerialName("modules")
    val modules: Modules,
    @SerialName("orig")
    val orig: DynamicItem? = null,
) {

    val type: DynamicType get() =
        when (typeStr){
            "DYNAMIC_TYPE_WORD" -> DynamicType.DYNAMIC_TYPE_WORD
            "DYNAMIC_TYPE_DRAW" -> DynamicType.DYNAMIC_TYPE_DRAW
            "DYNAMIC_TYPE_ARTICLE" -> DynamicType.DYNAMIC_TYPE_ARTICLE
            "DYNAMIC_TYPE_FORWARD" -> DynamicType.DYNAMIC_TYPE_FORWARD
            "DYNAMIC_TYPE_AV" -> DynamicType.DYNAMIC_TYPE_AV
            "DYNAMIC_TYPE_MUSIC" -> DynamicType.DYNAMIC_TYPE_MUSIC
            "DYNAMIC_TYPE_LIVE" -> DynamicType.DYNAMIC_TYPE_LIVE
            "DYNAMIC_TYPE_LIVE_RCMD" -> DynamicType.DYNAMIC_TYPE_LIVE_RCMD
            "DYNAMIC_TYPE_PGC" -> DynamicType.DYNAMIC_TYPE_PGC
            "DYNAMIC_TYPE_COMMON_SQUARE" -> DynamicType.DYNAMIC_TYPE_COMMON_SQUARE
            "DYNAMIC_TYPE_COMMON_VERTICAL" -> DynamicType.DYNAMIC_TYPE_COMMON_VERTICAL
            "DYNAMIC_TYPE_NONE" -> DynamicType.DYNAMIC_TYPE_NONE
            else -> DynamicType.DYNAMIC_TYPE_UNKNOWN
        }


    val did: String  get() = idStr?:"0"

    /**
     * 基本
     */
    @Serializable
    data class DynamicBasic(
        @SerialName("comment_id_str")
        val commentIdStr: String,
        @SerialName("comment_type")
        val commentType: Int,
        @SerialName("rid_str")
        val ridStr: String,
        @SerialName("like_icon")
        val likeIcon: LikeIcon,
    ) {
        @Serializable
        data class LikeIcon(
            @SerialName("id")
            val id: Int = 0,
            @SerialName("action_url")
            val actionUrl: String = "",
            @SerialName("start_url")
            val startUrl: String = "",
            @SerialName("end_url")
            val endUrl: String = "",
        )
    }

    /**
     * 动态模块
     * @param moduleAuthor 动态作者
     * @param moduleDynamic 动态内容
     * @param moduleInteraction 动态评论
     * @param moduleDispute 动态警告
     * @param moduleFold 折叠动态
     * @param moduleMore 更多选项
     * @param moduleStat 动态统计
     */
    @Serializable
    data class Modules(
        @SerialName("module_author")
        val moduleAuthor: ModuleAuthor,
        @SerialName("module_dynamic")
        val moduleDynamic: ModuleDynamic,
        @SerialName("module_interaction")
        val moduleInteraction: ModuleInteraction? = null,
        @SerialName("module_dispute")
        val moduleDispute: ModuleDispute? = null,
        @SerialName("module_fold")
        val moduleFold: ModuleFold? = null,
        @SerialName("module_more")
        val moduleMore: ModuleMore? = null,
        @SerialName("module_stat")
        val moduleStat: ModuleStat? = null,
    )
}

/**
 * 动态作者
 * @param type 作者类型
 * @param mid 用户ID
 * @param name 用户名
 * @param face 头像
 * @param faceNFT 是否为NFT头像
 * @param pendant 头像挂件
 * @param following 是否关注(未关注为null)
 * @param label 标签
 * @param jumpUrl 跳转URL
 * @param officialVerify 官方认证
 * @param pubAction 名称下方的副标题 (投稿了视频/与他人联合创作)
 * @param pubTime 名称下方的时间 (10分钟前)
 * @param pubTs 上传时间戳
 * @param vip VIP (大会员)
 * @param decorate 粉丝套装卡片
 */
@Serializable
data class ModuleAuthor(
    /**
     * AUTHOR_TYPE_NORMAL
     * AUTHOR_TYPE_PGC    番剧
     */
    @SerialName("type")
    val type: String,

    @SerialName("mid")
    val mid: Long,
    @SerialName("name")
    val name: String,
    @SerialName("face")
    val face: String,
    @SerialName("face_nft")
    val faceNFT: Boolean? = null,
    @SerialName("following")
    val following: Boolean? = null,
    @SerialName("label")
    val label: String = "",
    @SerialName("jump_url")
    val jumpUrl: String,
    @SerialName("pub_action")
    val pubAction: String = "",
    @SerialName("pub_time")
    val pubTime: String = "",
    @SerialName("pub_ts")
    val pubTs: Long,
    @SerialName("official_verify")
    val officialVerify: OfficialVerify? = null,
    @SerialName("vip")
    val vip: Vip? = null,
    @SerialName("pendant")
    val pendant: Pendant? = null,
    @SerialName("decorate")
    val decorate: Decorate? = null,
) {
    /**
     * 头像挂件
     * @param pid 挂件ID
     * @param name 挂件名
     * @param expire
     * @param image 挂件图片
     * @param imageEnhance 挂件图片增强
     * @param imageEnhanceFrame 挂件图片增强帧
     */
    @Serializable
    data class Pendant(
        @SerialName("pid")
        val pid: Int,
        @SerialName("name")
        val name: String,
        @SerialName("expire")
        val expire: Int,
        @SerialName("image")
        val image: String,
        @SerialName("image_enhance")
        val imageEnhance: String,
        @SerialName("image_enhance_frame")
        val imageEnhanceFrame: String,
    )

    /**
     * 官方认证
     * @param type 认证类型 无认证(-1) 个人认证(0) 机构认证(1) PERSONA_OFFICIAL_VERIFY ORGANIZATION_OFFICIAL_VERIFY
     * @param desc 描述
     */
    @Serializable
    data class OfficialVerify(
        @SerialName("type")
        val type: Int,
        @SerialName("desc")
        val desc: String,
    )

    /**
     * VIP
     * @param type vip类型 大会员(1) 年度大会员/十年大会员(2)
     * @param avatarSubscript 角标
     * @param avatarSubscriptUrl 角标链接
     * @param dueDate
     * @param label 标签
     * @param nicknameColor 昵称颜色
     * @param status 状态
     * @param themeType 主题类型
     */
    @Serializable
    data class Vip(
        @SerialName("type")
        val type: Int,

        @SerialName("avatar_subscript")
        val avatarSubscript: Int,
        @SerialName("avatar_subscript_url")
        val avatarSubscriptUrl: String,
        @SerialName("due_date")
        val dueDate: Long,
        @SerialName("label")
        val label: Label,
        @SerialName("nickname_color")
        val nicknameColor: String,
        @SerialName("status")
        val status: Int,
        @SerialName("theme_type")
        val themeType: Int,
    ) {
        /**
         * 标签
         * @param bgColor 背景颜色
         * @param bgStyle 背景样式
         * @param borderColor 边框颜色
         * @param labelTheme 标签主题 (annual_vip)
         * @param path
         * @param text 文字 (大会员)
         * @param textColor 文字颜色
         */
        @Serializable
        data class Label(
            @SerialName("bg_color")
            val bgColor: String,
            @SerialName("bg_style")
            val bgStyle: Int,
            @SerialName("border_color")
            val borderColor: String,
            @SerialName("label_theme")
            val labelTheme: String,
            @SerialName("path")
            val path: String,
            @SerialName("text")
            val text: String,
            @SerialName("text_color")
            val textColor: String,
        )
    }

    /**
     * 粉丝套装卡片
     * @param id 套装ID
     * @param type 套装类型 图标(1, 2) 专属卡片编号(3)
     * @param name 套装名称
     * @param cardUrl 套装图片链接
     * @param jumpUrl 跳转链接
     * @param fan 粉丝卡片
     */
    @Serializable
    data class Decorate(
        @SerialName("id")
        val id: Int,
        @SerialName("type")
        val type: Int,
        @SerialName("name")
        val name: String,
        @SerialName("card_url")
        val cardUrl: String,
        @SerialName("jump_url")
        val jumpUrl: String,
        @SerialName("fan")
        val fan: Fan? = null,
    ) {
        /**
         * 粉丝卡片
         * @param color 颜色
         * @param isFan 是否为粉丝
         * @param numStr 粉丝专属卡片数字字符串
         * @param number 粉丝专属卡片数字
         */
        @Serializable
        data class Fan(
            @SerialName("color")
            val color: String,
            @SerialName("is_fan")
            val isFan: Boolean,
            @SerialName("num_str")
            val numStr: String,
            @SerialName("number")
            val number: Int,
        )
    }
}

/**
 * 动态内容
 * @param additional 附加卡片
 * @param desc 动态文字列表
 * @param major 动态内容卡片
 * @param topic 话题
 */
@Serializable
data class ModuleDynamic(
    @SerialName("additional")
    val additional: Additional? = null,
    @SerialName("desc")
    val desc: Desc? = null,
    @SerialName("major")
    val major: Major? = null,
    @SerialName("topic")
    val topic: Topic? = null,
) {
    /**
     * 附加卡片
     * @param type 卡片类型
     * @param common  活动
     * @param reserve 预约
     * @param vote    投票
     * @param ugc     相关视频
     * @param goods   商品
     */
    @Serializable
    data class Additional(
        /**
         * ADDITIONAL_TYPE_COMMON   活动
         * ADDITIONAL_TYPE_RESERVE  预约
         * ADDITIONAL_TYPE_VOTE     投票
         * ADDITIONAL_TYPE_UGC      相关视频
         * ADDITIONAL_TYPE_GOODS    商品
         */
        @SerialName("type")
        val type: String,

        @SerialName("common")
        val common: Common? = null,
        @SerialName("reserve")
        val reserve: Reserve? = null,
        @SerialName("vote")
        val vote: Vote? = null,
        @SerialName("ugc")
        val ugc: Ugc? = null,
        @SerialName("goods")
        val goods: Goods? = null,
    ) {
        /**
         * 活动
         * https://t.bilibili.com/666029864355102737
         * https://t.bilibili.com/665881610678173713
         * @param idStr ID字符串
         * @param title 标题
         * @param cover 封面
         * @param subType 类型 游戏(game) 番剧(ogv) 装扮(decoration) 官方活动(official_activity)
         * @param desc1 第一行描述
         * @param desc2 第二行描述
         * @param headText 头文字描述(相关游戏)
         * @param jumpUrl 跳转链接
         * @param style 样式
         * @param button 按钮
         */
        @Serializable
        data class Common(
            @SerialName("id_str")
            val idStr: String,
            @SerialName("title")
            val title: String,
            @SerialName("cover")
            val cover: String,
            @SerialName("sub_type")
            val subType: String,
            @SerialName("desc1")
            val desc1: String,
            @SerialName("desc2")
            val desc2: String,
            @SerialName("head_text")
            val headText: String,
            @SerialName("jump_url")
            val jumpUrl: String,
            @SerialName("style")
            val style: Int,
            @SerialName("button")
            val button: Button,
        )

        /**
         * 预约
         * https://t.bilibili.com/648581851972108305 直播预约有奖
         * https://t.bilibili.com/649281734082297859 视频首映直播预告
         * @param rid 预约ID
         * @param upMid UP主ID
         * @param title 标题
         * @param reserveTotal 预约人数
         * @param desc1 第一行描述
         * @param desc2 第二行描述
         * @param desc3 第三行描述
         * @param premiere 预约封面?
         * @param state 状态
         * @param stype 预约类型 视频预约(1) 直播预约(2) 视频首映直播预告(4)
         * @param jumpUrl 跳转链接
         * @param button 按钮
         */
        @Serializable
        data class Reserve(
            @SerialName("rid")
            val rid: Long,
            @SerialName("up_mid")
            val upMid: Long,
            @SerialName("title")
            val title: String,
            @SerialName("reserve_total")
            val reserveTotal: Int,
            @SerialName("desc1")
            val desc1: Desc,
            @SerialName("desc2")
            val desc2: Desc,
            @SerialName("desc3")
            val desc3: Desc? = null,
            @SerialName("premiere")
            val premiere: Premiere? = null,
            @SerialName("state")
            val state: Int,
            @SerialName("stype")
            val stype: Int,
            @SerialName("jump_url")
            val jumpUrl: String,
            @SerialName("button")
            val button: Button,
        ) {
            /**
             * 预约描述
             * @param text 文字
             * @param jumpUrl 跳转链接
             * @param style 样式 灰色#99a2aa(0) 蓝色#00a1d6(1)
             * @param visible 是否可见
             */
            @Serializable
            data class Desc(
                @SerialName("text")
                val text: String,
                @SerialName("style")
                val style: Int,
                @SerialName("jump_url")
                val jumpUrl: String? = null,
                @SerialName("visible")
                val visible: Boolean? = null,
            )

            /**
             * 预约封面?
             * @param cover 封面
             * @param online 在线
             */
            @Serializable
            data class Premiere(
                @SerialName("cover")
                val cover: String? = null,
                @SerialName("online")
                val online: String? = null,
            )
        }

        /**
         * 投票
         * https://t.bilibili.com/649724369116856353
         * https://t.bilibili.com/362189798894110797
         * https://t.bilibili.com/352529506908653008
         * @param uid 用户ID
         * @param voteId 投票ID https://api.vc.bilibili.com/vote_svr/v1/vote_svr/vote_info?vote_id=2434097
         * @param desc 描述
         * @param type 类型
         * @param status 状态 正在进行(1) 结束(4)
         * @param joinNum 参加人数
         * @param endTime 结束时间
         * @param choiceCnt 可选数量
         * @param defaultShare 默认分享到动态
         */
        @Serializable
        data class Vote(
            @SerialName("uid")
            val uid: Long,
            @SerialName("vote_id")
            val voteId: Long,
            @SerialName("desc")
            val desc: String,
            @SerialName("type")
            val type: Int?,
            @SerialName("status")
            val status: Int?,
            @SerialName("join_num")
            val joinNum: Int,
            @SerialName("end_time")
            val endTime: Long,
            @SerialName("choice_cnt")
            val choiceCnt: Int,
            @SerialName("default_share")
            val defaultShare: Int,
        )

        /**
         * 相关视频
         * https://t.bilibili.com/629668894526444564
         * https://t.bilibili.com/649671579655995396
         * @param idStr ID字符串
         * @param title 标题
         * @param cover 封面
         * @param descSecond 第二行描述
         * @param duration 视频长度
         * @param headText 头文字
         * @param jumpUrl 跳转链接
         * @param multiLine
         */
        @Serializable
        data class Ugc(
            @SerialName("id_str")
            val idStr: String,
            @SerialName("title")
            val title: String,
            @SerialName("cover")
            val cover: String,
            @SerialName("desc_second")
            val descSecond: String,
            @SerialName("duration")
            val duration: String,
            @SerialName("head_text")
            val headText: String,
            @SerialName("jump_url")
            val jumpUrl: String,
            @SerialName("multi_line")
            val multiLine: String,
        )

        /**
         * 商品
         * https://t.bilibili.com/649213796294852628 单个商品
         * https://t.bilibili.com/648964520676425729 副标题
         * https://t.bilibili.com/649594558007476241 淘宝商品
         * https://t.bilibili.com/648921605368447013 多个商品
         * @param headIcon 头图标
         * @param headText 头文字
         * @param jumpUrl 跳转链接
         * @param items 商品列表
         */
        @Serializable
        data class Goods(
            @SerialName("head_icon")
            val headIcon: String,
            @SerialName("head_text")
            val headText: String,
            @SerialName("jump_url")
            val jumpUrl: String,
            @SerialName("items")
            val items: List<GoodItem>,
        ) {
            /**
             * 商品项
             * @param id 商品ID b站商品为Long 外站商品为String
             * @param name 名称
             * @param brief 副标题
             * @param cover 封面
             * @param jumpDesc 跳转按钮描述
             * @param jumpUrl 跳转链接
             */
            @Serializable
            data class GoodItem(
                @SerialName("id")
                val id: String,
                @SerialName("name")
                val name: String,
                @SerialName("brief")
                val brief: String,
                @SerialName("cover")
                val cover: String,
                @SerialName("price")
                val price: String,
                @SerialName("jump_desc")
                val jumpDesc: String,
                @SerialName("jump_url")
                val jumpUrl: String,
            )
        }


        /**
         * 按钮
         * @param type 类型 使用jumpStyle内容(1)
         * @param status 状态 正常(1) 已预约/撤销(2)
         * @param jumpUrl 跳转链接
         * @param jumpStyle 样式
         * @param check 按钮点击
         * @param uncheck 按钮未点击
         */
        @Serializable
        data class Button(
            @SerialName("type")
            val type: Int,
            @SerialName("status")
            val status: Int? = null,
            @SerialName("jump_url")
            val jumpUrl: String? = null,
            @SerialName("jump_style")
            val jumpStyle: JumpStyle? = null,
            @SerialName("check")
            val check: Check? = null,
            @SerialName("uncheck")
            val uncheck: Check? = null,
        ) {
            /**
             * 点击
             * @param text 文字
             * @param iconUrl 图标链接
             * @param disable 是否禁用
             * @param toast 按钮提示
             */
            @Serializable
            data class Check(
                @SerialName("text")
                val text: String,
                @SerialName("icon_url")
                val iconUrl: String?,
                @SerialName("disable")
                val disable: Int? = null,
                @SerialName("toast")
                val toast: String? = null,
            )
        }

        /**
         * 跳转按钮样式
         * @param text 文字
         * @param iconUrl 图标链接
         */
        @Serializable
        data class JumpStyle(
            @SerialName("text")
            val text: String,
            @SerialName("icon_url")
            val iconUrl: String,
        )

    }

    /**
     * 动态文字列表
     * @param richTextNodes 文字节点列表
     * @param text 全部文字
     */
    @Serializable
    data class Desc(
        @SerialName("rich_text_nodes")
        val richTextNodes: List<RichTextNode>,
        @SerialName("text")
        val text: String,
    ) {
        /**
         * 文字节点
         * @param type 节点类型
         * @param origText 源文字
         * @param text 显示文字
         * @param rid @用户ID/商品ID...
         * @param jumpUrl 网页链接/主题 跳转链接
         * @param emoji Emoji
         */
        @Serializable
        data class RichTextNode(
            /**
             * RICH_TEXT_NODE_TYPE_TEXT     文字
             * RICH_TEXT_NODE_TYPE_EMOJI    Emoji
             * RICH_TEXT_NODE_TYPE_AT       @用户
             * RICH_TEXT_NODE_TYPE_TOPIC    #主题#
             * RICH_TEXT_NODE_TYPE_WEB      网页链接
             * RICH_TEXT_NODE_TYPE_VOTE     投票
             * RICH_TEXT_NODE_TYPE_LOTTERY  互动抽奖
             * RICH_TEXT_NODE_TYPE_BV       BV号
             * RICH_TEXT_NODE_TYPE_GOODS    商品
             */
            @SerialName("type")
            val type: String,

            @SerialName("orig_text")
            val origText: String,
            @SerialName("text")
            val text: String,

            @SerialName("rid")
            val rid: String? = null,
            @SerialName("jump_url")
            val jumpUrl: String? = null,
            @SerialName("emoji")
            val emoji: Emoji? = null,
        ) {
            /**
             * Emoji
             * @param type 类型
             * @param iconUrl emoji链接
             * @param size 大小?
             * @param text emoji文字
             */
            @Serializable
            data class Emoji(
                @SerialName("type")
                val type: Int,

                @SerialName("icon_url")
                val iconUrl: String,
                @SerialName("size")
                val size: Int,
                @SerialName("text")
                val text: String,
            )
        }
    }

    /**
     * 动态内容卡片
     * @param type 卡片类型
     * @param archive 视频
     * @param draw 图片
     * @param article 专栏
     * @param music 音乐
     * @param live 直播
     * @param liveRcmd 直播
     * @param pgc 番剧
     * @param common 活动
     */
    @Serializable
    data class Major(
        /**
         * MAJOR_TYPE_ARCHIVE    视频
         * MAJOR_TYPE_DRAW       图片
         * MAJOR_TYPE_ARTICLE    专栏
         * MAJOR_TYPE_MUSIC      音乐
         * MAJOR_TYPE_LIVE       直播
         * MAJOR_TYPE_LIVE_RCMD  直播
         * MAJOR_TYPE_PGC        番剧
         * MAJOR_TYPE_COMMON     活动
         * MAJOR_TYPE_NONE       空
         */
        @SerialName("type")
        val type: String,

        @SerialName("archive")
        val archive: Archive? = null,
        @SerialName("draw")
        val draw: Draw? = null,
        @SerialName("article")
        val article: Article? = null,
        @SerialName("music")
        val music: Music? = null,
        @SerialName("live")
        val live: Live? = null,
        @SerialName("live_rcmd")
        val liveRcmd: LiveRcmd? = null,
        @SerialName("pgc")
        val pgc: Pgc? = null,
        @SerialName("common")
        val common: Common? = null,
        @SerialName("none")
        val none: None? = null,
    ) {
        /**
         * 视频
         * @param type 类型
         * @param aid AVID
         * @param bvid BVID
         * @param title 标题
         * @param cover 封面
         * @param desc 描述
         * //@param disablePreview
         * @param durationText 视频长度
         * @param jumpUrl 跳转链接
         * @param stat 视频统计
         * @param badge 徽章
         */
        @Serializable
        data class Archive(
            @SerialName("type")
            val type: Int,
            @SerialName("aid")
            val aid: String,
            @SerialName("bvid")
            val bvid: String,
            @SerialName("title")
            val title: String,
            @SerialName("cover")
            val cover: String,
            @SerialName("desc")
            val desc: String,
            //@SerialName("disable_preview")
            //val disablePreview: Boolean,
            @SerialName("duration_text")
            val durationText: String,
            @SerialName("jump_url")
            val jumpUrl: String,
            @SerialName("stat")
            val stat: Stat,
            @SerialName("badge")
            val badge: Badge,
        )

        /**
         * 图片
         * @param id 图片ID
         * @param items 图片列表
         */
        @Serializable
        data class Draw(
            @SerialName("id")
            val id: Long,
            @SerialName("items")
            val items: List<DrawItem>,
        ) {
            /**
             * 图片项
             * @param width 宽度
             * @param height 高度
             * @param size 文件大小
             * @param src 链接
             * @param tags TAG
             */
            @Serializable
            data class DrawItem(
                @SerialName("width")
                val width: Int,
                @SerialName("height")
                val height: Int,
                @SerialName("size")
                val size: Float,
                @SerialName("src")
                val src: String,
                @SerialName("tags")
                val tags: List<Tag>? = null,
            ) {
                /**
                 * TAG
                 * https://t.bilibili.com/649677042850201602
                 * @param mid 用户ID
                 * @param tid TAGID
                 * @param itemId
                 * @param source
                 * @param type
                 * @param text 文字
                 * @param jumpUrl 跳转链接
                 * @param schemaUrl
                 * @param poi
                 * @param orientation
                 * @param x
                 * @param y
                 */
                @Serializable
                data class Tag(
                    @SerialName("tid")
                    val tid: Long,
                    @SerialName("mid")
                    val mid: Long = 0,
                    @SerialName("item_id")
                    val itemId: Int = 0,
                    @SerialName("source")
                    val source: Int = 0,
                    @SerialName("type")
                    val type: Int,
                    @SerialName("text")
                    val text: String,
                    @SerialName("jump_url")
                    val jumpUrl: String = "",
                    @SerialName("schema_url")
                    val schemaUrl: String = "",
                    @SerialName("poi")
                    val poi: String = "",
                    @SerialName("orientation")
                    val orientation: Int,
                    @SerialName("x")
                    val x: Int,
                    @SerialName("y")
                    val y: Int,
                )
            }
        }

        /**
         * 专栏
         * https://t.bilibili.com/649964827143307286
         * @param id 专栏ID
         * @param title 标题
         * @param label 标签(114阅读)
         * @param desc 描述
         * @param jumpUrl 跳转链接
         * @param covers 封面列表
         */
        @Serializable
        data class Article(
            @SerialName("id")
            val id: Long,
            @SerialName("title")
            val title: String,
            @SerialName("desc")
            val desc: String,
            @SerialName("label")
            val label: String,
            @SerialName("jump_url")
            val jumpUrl: String,
            @SerialName("covers")
            val covers: List<String>,
        )

        /**
         * 音乐
         * https://t.bilibili.com/649393566418731042
         * @param id 音频ID
         * @param title 标题
         * @param cover 封面
         * @param label 标签
         * @param jumpUrl 跳转链接
         */
        @Serializable
        data class Music(
            @SerialName("id")
            val id: Long,
            @SerialName("title")
            val title: String,
            @SerialName("cover")
            val cover: String,
            @SerialName("label")
            val label: String,
            @SerialName("jump_url")
            val jumpUrl: String,
        )

        /**
         * 直播
         * https://t.bilibili.com/387009689050325972
         * @param id LiveID
         * @param title 标题
         * @param cover 封面
         * @param descFirst 第一个描述
         * @param descSecond 第二个描述
         * @param jumpUrl 跳转链接
         * @param liveState 直播状态 直播结束(0) 直播中(1)
         * @param reserveType
         * @param badge 徽章
         */
        @Serializable
        data class Live(
            @SerialName("id")
            val id: Long,
            @SerialName("title")
            val title: String,
            @SerialName("cover")
            val cover: String,
            @SerialName("desc_first")
            val descFirst: String,
            @SerialName("desc_second")
            val descSecond: String,
            @SerialName("jump_url")
            val jumpUrl: String,
            @SerialName("live_state")
            val liveState: Int,
            @SerialName("reserve_type")
            val reserveType: Int,
            @SerialName("badge")
            val badge: Badge,
        )

        /**
         * 直播
         * @param content 内容
         * @param reserveType
         */
        @Serializable
        data class LiveRcmd(
            /**
             * {"type": 1,"live_play_info": {"room_type": 0,"title": "尘白禁区第三天，又更新了1个G","parent_area_id": 3,"pendants": {"list": {"index_badge": {"list": {"1": {"pendant_id": 425,"type": "index_badge","name": "百人成就","position": 1,"text": "","bg_color": "#FB9E60","bg_pic": "https://i0.hdslb.com/bfs/live/539ce26c45cd4019f55b64cfbcedc3c01820e539.png"}}},"mobile_index_badge": {"list": {"1": {"bg_color": "#FB9E60","bg_pic": "https://i0.hdslb.com/bfs/live/539ce26c45cd4019f55b64cfbcedc3c01820e539.png","pendant_id": 426,"type": "mobile_index_badge","name": "百人成就","position": 1,"text": ""}}}}},"room_id": 26057,"play_type": 0,"uid": 164627,"cover": "http://i0.hdslb.com/bfs/live/new_room_cover/fda9c8f51ce214a7fadaddf6ef0277457fd89ee7.jpg","area_name": "综合棋牌","live_id": "230552946801141193","watched_show": {"icon_location": "","icon_web": "https://i0.hdslb.com/bfs/live/8d9d0f33ef8bf6f308742752d13dd0df731df19c.png","switch": true,"num": 4623,"text_small": "4623","text_large": "4623人看过","icon": "https://i0.hdslb.com/bfs/live/a725a9e61242ef44d764ac911691a7ce07f36c1d.png"},"live_status": 1,"area_id": 354,"parent_area_name": "手游","live_screen_type": 0,"live_start_time": 1650084174,"link": "https://live.bilibili.com/26057","room_paid_type": 0,"online": 74803},"live_record_info": null}
             * {"type": 1,"live_play_info": {"uid": 673816,"cover": "http://i0.hdslb.com/bfs/live/new_room_cover/41f9f632c80769a8d225150b4ffd9746dc70d44b.jpg","parent_area_name": "单机游戏","link": "https://live.bilibili.com/5082","room_type": 0,"live_screen_type": 0,"title": "鬼怪传说~ 晚点再打打星座上升！","live_id": "230288333866013658","parent_area_id": 6,"live_start_time": 1650109291,"room_id": 5082,"live_status": 1,"play_type": 0,"online": 23875,"area_id": 283,"area_name": "独立游戏","pendants": {"list": null},"watched_show": {"text_small": "2421","text_large": "2421人看过","icon": "https://i0.hdslb.com/bfs/live/a725a9e61242ef44d764ac911691a7ce07f36c1d.png","icon_location": "","icon_web": "https://i0.hdslb.com/bfs/live/8d9d0f33ef8bf6f308742752d13dd0df731df19c.png","switch": true,"num": 2421},"room_paid_type": 0},"live_record_info": null}
             */
            @SerialName("content")
            val content: String,
            @SerialName("reserve_type")
            val reserveType: Int,
        ){
            val liveInfo: LiveRcmdContent get() = content.decode()

            @Serializable
            data class LiveRcmdContent(
                @SerialName("type")
                val type: Int,
                @SerialName("live_play_info")
                val livePlayInfo: LivePlayInfo,
                //@SerialName("live_record_info")
                //val liveRecordInfo: LiveRecordInfo?,
            ){
                @Serializable
                data class LivePlayInfo(
                    @SerialName("uid")
                    val uid: Long,
                    @SerialName("room_id")
                    val roomId: Long,
                    @SerialName("live_id")
                    val liveId: String,
                    @SerialName("live_status")
                    val liveStatus: Int,
                    @SerialName("title")
                    val title: String,
                    @SerialName("cover")
                    val cover: String,
                    @SerialName("parent_area_name")
                    val parentAreaName: String,
                    @SerialName("parent_area_id")
                    val parentAreaId: Int,
                    @SerialName("area_name")
                    val areaName: String,
                    @SerialName("area_id")
                    val areaId: Int,
                    @SerialName("link")
                    val link: String,
                    @SerialName("room_type")
                    val room_type: Int,
                    @SerialName("live_screen_type")
                    val liveScreenType: Int,
                    @SerialName("live_start_time")
                    val liveStartTime: Long,
                    @SerialName("play_type")
                    val playType: Int,
                    @SerialName("online")
                    val online: Int,
                    @SerialName("room_paid_type")
                    val roomPaidType: Int,
                    @SerialName("watched_show")
                    val watchedShow: WatchedShow,
                    //@SerialName("pendants")
                    //val pendants: Pendants,

                ){
                    @Serializable
                    data class WatchedShow(
                        @SerialName("num")
                        val num: Int,
                        @SerialName("text_small")
                        val textSmall: String,
                        @SerialName("text_large")
                        val textLarge: String,
                        @SerialName("icon")
                        val icon: String,
                        @SerialName("icon_location")
                        val iconLocation: String,
                        @SerialName("icon_web")
                        val iconWeb: String,
                        @SerialName("switch")
                        val switch: Boolean,
                    )
                }
            }
        }


        /**
         * 番剧/电影
         * https://t.bilibili.com/649951005934354482 电影
         * https://t.bilibili.com/649955687456047124 番剧
         * https://t.bilibili.com/649948476201762833 国创
         * @param type 类型 番剧(1) 电影(2) 国创(4)
         * @param epid 番剧ID
         * @param seasonId
         * @param subType
         * @param title 标题
         * @param cover 封面
         * @param jumpUrl 跳转链接
         * @param stat 统计
         * @param badge 徽章
         */
        @Serializable
        data class Pgc(
            @SerialName("type")
            val type: Int,
            @SerialName("epid")
            val epid: Int,
            @SerialName("season_id")
            val seasonId: Int,
            @SerialName("sub_type")
            val subType: Int,
            @SerialName("title")
            val title: String,
            @SerialName("cover")
            val cover: String,
            @SerialName("jump_url")
            val jumpUrl: String,
            @SerialName("stat")
            val stat: Stat,
            @SerialName("badge")
            val badge: Badge,
        )

        /**
         * 活动
         * https://t.bilibili.com/451600413724453059 评分
         * https://t.bilibili.com/649769062547587096 装扮
         * https://t.bilibili.com/649677803073044502 活动
         * https://t.bilibili.com/255349133036764369 整部动漫评分
         * @param id
         * @param sketchId
         * @param bizType 类型 评分(0) 活动(1) 无徽章(3) 动漫评分(101) 装扮(231)
         * @param title 标题
         * @param cover 封面
         * @param desc 描述
         * @param label 标签
         * @param jumpUrl 跳转链接
         * @param style 样式
         * @param badge 徽章
         */
        @Serializable
        data class Common(
            @SerialName("id")
            val id: String,
            @SerialName("sketch_id")
            val sketchId: String,
            @SerialName("biz_type")
            val bizType: Int,
            @SerialName("title")
            val title: String,
            @SerialName("cover")
            val cover: String,
            @SerialName("desc")
            val desc: String,
            @SerialName("label")
            val label: String = "",
            @SerialName("jump_url")
            val jumpUrl: String,
            @SerialName("style")
            val style: Int,
            @SerialName("badge")
            val badge: Badge,
        )

        /**
         * 空
         * @param tips 提示
         */
        @Serializable
        data class None(
            @SerialName("tips")
            val tips: String? = null,
        )


        /**
         * 统计
         * @param danmaku 弹幕数
         * @param play 播放数
         */
        @Serializable
        data class Stat(
            @SerialName("danmaku")
            val danmaku: String,
            @SerialName("play")
            val play: String,
        )

        /**
         * 徽章
         * @param bgColor 背景颜色
         * @param color 颜色
         * @param text 徽章文字
         */
        @Serializable
        data class Badge(
            @SerialName("bg_color")
            val bgColor: String,
            @SerialName("color")
            val color: String,
            @SerialName("text")
            val text: String,
        )
    }

    /**
     * 主题
     * @param id 主题ID
     * @param name 主题名
     * @param jumpUrl 跳转链接
     */
    @Serializable
    data class Topic(
        @SerialName("id")
        val id: Int,
        @SerialName("name")
        val name: String,
        @SerialName("jump_url")
        val jumpUrl: String,
    )

}

/**
 * 动态评论
 * @param items 评论
 */
@Serializable
data class ModuleInteraction(
    @SerialName("items")
    val items: List<ModuleDynamic>,
)

/**
 * 动态警告
 * https://t.bilibili.com/649561422335836211
 * @param title 警告内容
 * @param desc
 * @param jumpUrl
 */
@Serializable
data class ModuleDispute(
    @SerialName("title")
    val title: String,
    @SerialName("desc")
    val desc: String,
    @SerialName("jump_url")
    val jumpUrl: String,
)

/**
 * 折叠动态
 * @param type
 * @param ids 动态IDS
 * @param statement 折叠描述(展开3条相关动态)
 * @param users
 */
@Serializable
data class ModuleFold(
    @SerialName("type")
    val type: Int,
    @SerialName("ids")
    val ids: List<String>,
    @SerialName("statement")
    val statement: String,
    @SerialName("users")
    val users: List<BaseUser>,
)

@Serializable
data class BaseUser(
    @SerialName("mid")
    val mid: Long,
    @SerialName("name")
    val name: String,
    @SerialName("face")
    val face: String,
)

/**
 * 更多选项
 * @param threePointItems
 */
@Serializable
data class ModuleMore(
    @SerialName("three_point_items")
    val threePointItems: List<ThreePointItem>,
) {
    /**
     * @param type 类型
     * @param label 标签
     * @param modal
     */
    @Serializable
    data class ThreePointItem(
        /**
         * THREE_POINT_FOLLOWING 取消关注
         * THREE_POINT_REPORT 举报
         * THREE_POINT_DELETE 删除
         */
        @SerialName("type")
        val type: String,
        @SerialName("label")
        val label: String,
        @SerialName("modal")
        val modal: Modal? = null,
    ) {
        /**
         * @param title 标题
         * @param content 内容
         * @param cancel 取消文字
         * @param confirm 确认文字
         */
        @Serializable
        data class Modal(
            @SerialName("title")
            val title: String,
            @SerialName("content")
            val content: String,
            @SerialName("cancel")
            val cancel: String,
            @SerialName("confirm")
            val confirm: String,
        )
    }
}

/**
 * 动态统计
 * @param comment 评论
 * @param forward 转发
 * @param like 点赞
 */
@Serializable
data class ModuleStat(
    @SerialName("comment")
    val comment: Stat,
    @SerialName("forward")
    val forward: Stat,
    @SerialName("like")
    val like: Stat,
) {
    /**
     * 统计
     * @param text 数量文字
     * @param count 数量
     * @param forbidden
     * @param status
     */
    @Serializable
    data class Stat(
        @SerialName("text")
        val text: String? = null,
        @SerialName("count")
        val count: Int,
        @SerialName("forbidden")
        val forbidden: Boolean,
        @SerialName("status")
        val status: Boolean? = null,
    )
}
