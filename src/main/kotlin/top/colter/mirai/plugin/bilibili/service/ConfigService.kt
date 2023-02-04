package top.colter.mirai.plugin.bilibili.service

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.whileSelectMessages
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.nextMessage
import top.colter.mirai.plugin.bilibili.*
import top.colter.mirai.plugin.bilibili.command.GroupOrContact
import top.colter.mirai.plugin.bilibili.service.FilterService.addFilter
import top.colter.mirai.plugin.bilibili.service.FilterService.delFilter
import top.colter.mirai.plugin.bilibili.service.FilterService.listFilter
import top.colter.mirai.plugin.bilibili.service.TemplateService.setTemplate
import top.colter.mirai.plugin.bilibili.utils.delegate
import top.colter.mirai.plugin.bilibili.utils.name

object ConfigService {
    suspend fun config(event: MessageEvent, uid: Long = 0L, contact: Contact) {
        val subject = event.subject
        if (uid != 0L && !((dynamic.containsKey(uid) && dynamic[uid]!!.contacts.contains(contact.delegate)))) {
            subject.sendMessage("没有订阅这个人哦 [$uid]")
            return
        }
        val user = if (uid != 0L) dynamic[uid] else null
        val configMap = mutableMapOf<String, String>()

        val configMsg = subject.sendMessage(buildString {
            appendLine("配置: ")
            append("用户: ")
            appendLine(if (uid == 0L) "全局" else user?.name)
            append("目标: ")
            appendLine(if (subject.id == contact.id) "当前环境" else contact.name)
            appendLine()
            appendLine("当前可配置项:")
            var i = 1
            if (contact is Group) {
                configMap[i.toString()] = "ATALL"
                val aa = atAll[contact.delegate]?.get(uid)?.isNotEmpty()
                appendLine("  $i: At全体 [${aa ?: false}]")
                appendLine("      $i.1: 当前At全体项")
                appendLine("      $i.2: 添加At全体")
                appendLine("      $i.2: 删除At全体")
                i++
            }
            if (uid != 0L) {
                configMap[i.toString()] = "COLOR"
                appendLine("  ${i++}: 主题色 [${user?.color ?: BiliConfig.imageConfig.defaultColor}]")
            }
            if (uid == 0L) {
                val cdl = BiliData.dynamicPushTemplate.filter { it.value.contains(contact.delegate) }.map { it.key }
                val currDynamic = if (cdl.isNotEmpty()) cdl.first() else BiliConfig.templateConfig.defaultDynamicPush
                val cll = BiliData.livePushTemplate.filter { it.value.contains(contact.delegate) }.map { it.key }
                val currLive = if (cll.isNotEmpty()) cll.first() else BiliConfig.templateConfig.defaultLivePush
                val clel = BiliData.liveCloseTemplate.filter { it.value.contains(contact.delegate) }.map { it.key }
                val currLiveClose = if (clel.isNotEmpty()) clel.first() else BiliConfig.templateConfig.defaultLiveClose
                configMap[i.toString()] = "PUSH"
                appendLine("  $i: 推送模板")
                appendLine("      $i.1: 动态推送模板 [$currDynamic]")
                appendLine("      $i.2: 直播推送模板 [$currLive]")
                appendLine("      $i.3: 直播结束模板 [$currLiveClose]")
                i++
            }

            val filter = BiliData.filter[contact.delegate]?.get(uid)
            val mode = if (filter == null) "无过滤器" else
                "类型: ${filter.typeSelect.mode.value} | 正则: ${filter.regularSelect.mode.value}"

            configMap[i.toString()] = "FILTER"
            appendLine("  $i: 过滤器")
            appendLine("      $i.1: 过滤器列表")
            appendLine("      $i.2: 添加类型过滤器")
            appendLine("      $i.3: 添加正则过滤器")
            appendLine("      $i.4: 切换过滤模式 [$mode]")
            appendLine("      $i.5: 删除过滤器")
            appendLine()
            append("[中括号]内为当前值\n请输入编号, 2分钟未回复自动退出\n或回复 退出 来主动退出")
        })

        var regMsg: MessageReceipt<Contact>? = null

        while (true) {
            var cc = 0
            var rres: String? = null
            var selectContent = ""
            var selectConfig = ""

            event.whileSelectMessages {
                "退出" {
                    event.subject.sendMessage("已退出")
                    false
                }
                configMap.forEach { (t, u) ->
                    startsWith(t) {
                        selectContent = message.content
                        selectConfig = u
                        rres = ""
                        false
                    }
                }
                default {
                    cc++
                    subject.sendMessage("没有这个选项哦${if (cc < 2) ", 请重新输入" else ", 超出重试次数, 退出"}")
                    cc < 2
                }
                timeout(120_000) { false }
            }
            regMsg?.recall()
            if (rres == null) return

            when (selectConfig) {
                "ATALL" -> {
                    val b = selectContent.split(".").last()
                    when (b) {
                        "1" -> subject.sendMessage(AtAllService.listAtAll(uid, contact.delegate))
                        "2" -> {
                            subject.sendMessage(buildString {
                                appendLine("请选择要At全体的内容: ")
                                appendLine("  全部")
                                appendLine("  ├─ 全部动态")
                                appendLine("  │   ├─ 视频")
                                appendLine("  │   ├─ 音乐")
                                appendLine("  │   └─ 专栏")
                                appendLine("  └─ 直播")
                            })

                            var c = 0
                            var res: String? = null
                            var selectType = ""
                            event.whileSelectMessages {
                                "退出" {
                                    event.subject.sendMessage("已退出")
                                    false // 停止循环
                                }
                                AtAllType.values().forEach { t ->
                                    t.value {
                                        selectType = t.value
                                        res = ""
                                        false
                                    }
                                }
                                default {
                                    c++
                                    subject.sendMessage("没有这个选项哦${if (c < 2) ", 请重新输入" else ", 超出重试次数, 退出"}")
                                    c < 2
                                }
                                timeout(120_000) { false }
                            }
                            if (res == null) return
                            subject.sendMessage(AtAllService.addAtAll(selectType, uid, GroupOrContact(contact)))
                        }
                        "3" -> {
                            val list = atAll[contact.delegate]?.get(uid)
                            if (list == null || list.isEmpty()) subject.sendMessage("没有At全体哦")
                            subject.sendMessage("At全体项:\n" + AtAllService.listAtAll(uid, contact.delegate) + "\n请回复要删除的项")
                            val type = event.nextMessage().content
                            subject.sendMessage(AtAllService.delAtAll(type, uid, contact.delegate))
                        }
                    }
                }

                "COLOR" -> {
                    subject.sendMessage("请输入16进制颜色，例如: #d3edfa")
                    var res: String? = null
                    var count = 0
                    event.whileSelectMessages {
                        "退出" {
                            event.subject.sendMessage("已退出")
                            false // 停止循环
                        }
                        default {
                            val color = message.content
                            if (color.first() != '#' || color.length != 7) {
                                subject.sendMessage("格式错误，请输入16进制颜色，例如: #d3edfa")
                            } else {
                                subject.sendMessage(DynamicService.setColor(uid, color))
                                count = 2
                                res = ""
                            }
                            ++count < 2
                        }
                        timeout(120_000) { false }
                    }
                    if (res == null) return
                }

                "PUSH" -> {
                    val b = selectContent.split(".").last()
                    val template = when (b) {
                        "1" -> BiliConfig.templateConfig.dynamicPush
                        "2" -> BiliConfig.templateConfig.livePush
                        "3" -> BiliConfig.templateConfig.liveClose
                        else -> {
                            subject.sendMessage("没有这个选项哦")
                            null
                        }
                    }
                    if (template != null) {
                        subject.sendMessage("请选择一个推送模板, 回复模板名\n生成模板需要一定时间...")
                        TemplateService.listTemplate(if (b == "1") "d" else if (b == "2") "l" else "le", subject)
                        var c = 0
                        var res: String? = null
                        var selectTemplate = ""
                        event.whileSelectMessages {
                            "退出" {
                                event.subject.sendMessage("已退出")
                                false // 停止循环
                            }
                            template.forEach { (t, _) ->
                                t {
                                    selectTemplate = t
                                    res = ""
                                    false
                                }
                            }
                            default {
                                c++
                                subject.sendMessage("没有这个选项哦${if (c < 2) ", 请重新输入" else ", 超出重试次数, 退出"}")
                                c < 2
                            }
                            timeout(120_000) { false }
                        }
                        if (res == null) return
                        subject.sendMessage(setTemplate(if (b == "1") "d" else if (b == "2") "l" else "le", selectTemplate, subject.delegate))
                    }
                }

                "FILTER" -> {
                    val b = selectContent.split(".").last()
                    val filter = BiliData.filter[contact.delegate]?.get(uid)
                    when (b) {
                        "1" -> subject.sendMessage(listFilter(uid, contact.delegate))
                        "2" -> {
                            val mode = filter?.typeSelect?.mode?.value ?: "黑名单"
                            val type = DynamicFilterType.values().joinToString("\n    ") { it.value }
                            subject.sendMessage("当前过滤器类型: $mode\n支持的类型: \n    $type\n请回复要过滤的类型")

                            var c = 0
                            var res: String? = null
                            var selectType: String? = null
                            event.whileSelectMessages {
                                "退出" {
                                    event.subject.sendMessage("已退出")
                                    false // 停止循环
                                }
                                DynamicFilterType.values().forEach { t ->
                                    t.value {
                                        selectType = t.value
                                        res = ""
                                        false
                                    }
                                }
                                default {
                                    c++
                                    subject.sendMessage("没有这个选项哦${if (c < 2) ", 请重新输入" else ", 超出重试次数, 退出"}")
                                    c < 2
                                }
                                timeout(120_000) { false }
                            }
                            if (res == null) return
                            subject.sendMessage(addFilter(FilterType.TYPE, null, selectType, uid, contact.delegate))
                        }
                        "3" -> {
                            try {
                                val mode = filter?.regularSelect?.mode?.value ?: "黑名单"
                                subject.sendMessage("当前过滤器类型: $mode\n请回复过滤文本或正则")
                                val reg = event.nextMessage(120_000).content
                                if (reg != "")
                                    subject.sendMessage(addFilter(FilterType.REGULAR, null, reg, uid, contact.delegate))
                            } catch (e: Exception) {
                                return
                            }
                        }
                        "4" -> {
                            val typeMode = filter?.typeSelect?.mode?.value ?: "黑名单"
                            val regMode = filter?.regularSelect?.mode?.value ?: "黑名单"
                            subject.sendMessage("类型过滤器: $typeMode\n正则过滤器: $regMode\n请选择要切换的过滤的类型\nt: 类型过滤器\nr: 正则过滤器")

                            var c = 0
                            var res: String? = null
                            var selectType: FilterType? = null
                            var selectMode: FilterMode? = null
                            event.whileSelectMessages {
                                "退出" {
                                    event.subject.sendMessage("已退出")
                                    false // 停止循环
                                }
                                "t" {
                                    selectType = FilterType.TYPE
                                    selectMode = filter?.typeSelect?.mode ?: FilterMode.BLACK_LIST
                                    res = ""
                                    false
                                }
                                "r" {
                                    selectType = FilterType.REGULAR
                                    selectMode = filter?.regularSelect?.mode ?: FilterMode.BLACK_LIST
                                    res = ""
                                    false
                                }
                                default {
                                    c++
                                    subject.sendMessage("没有这个选项哦${if (c < 2) ", 请重新输入" else ", 超出重试次数, 退出"}")
                                    c < 2
                                }
                                timeout(120_000) { false }
                            }
                            if (res == null) return
                            if (selectType != null) {
                                selectMode =
                                    if (selectMode == FilterMode.BLACK_LIST) FilterMode.WHITE_LIST else FilterMode.BLACK_LIST
                                subject.sendMessage(addFilter(selectType!!, selectMode, null, uid, contact.delegate))
                            }
                        }
                        "5" -> {
                            try {
                                subject.sendMessage(listFilter(uid, contact.delegate))
                                val reg = event.nextMessage(120_000).content
                                subject.sendMessage(delFilter(reg, uid, contact.delegate))
                            } catch (e: Exception) {
                                return
                            }
                        }
                    }
                }
            }
            regMsg = subject.sendMessage(
                buildMessageChain {
                    + QuoteReply(configMsg.source)
                    + PlainText("输入编号以继续\n不回复或回复 退出 来退出")
                }
            )
        }
    }
}