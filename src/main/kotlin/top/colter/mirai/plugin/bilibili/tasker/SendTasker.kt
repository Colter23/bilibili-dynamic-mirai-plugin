package top.colter.mirai.plugin.bilibili.tasker

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import net.mamoe.mirai.console.permission.PermissionService.Companion.getPermittedPermissions
import net.mamoe.mirai.console.permission.PermitteeId.Companion.permitteeId
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.code.MiraiCode
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import top.colter.mirai.plugin.bilibili.*
import top.colter.mirai.plugin.bilibili.data.DynamicMessage
import top.colter.mirai.plugin.bilibili.data.DynamicType
import top.colter.mirai.plugin.bilibili.data.LiveMessage
import top.colter.mirai.plugin.bilibili.tasker.BiliDataTasker.mutex
import top.colter.mirai.plugin.bilibili.utils.CacheType
import top.colter.mirai.plugin.bilibili.utils.cachePath
import top.colter.mirai.plugin.bilibili.utils.findContact
import top.colter.mirai.plugin.bilibili.utils.uploadImage
import kotlin.io.path.notExists
import kotlin.io.path.readBytes

object SendTasker : BiliTasker() {

    override var interval: Int = 0

    private val templateConfig by BiliConfig::templateConfig
    private val atAllPlus = BiliConfig.pushConfig.atAllPlus

    private val dynamic by BiliData::dynamic
    private val filter by BiliData::filter
    private val atAll by BiliData::atAll

    private val messageInterval = BiliConfig.pushConfig.messageInterval
    private val pushInterval = BiliConfig.pushConfig.pushInterval

    private val forwardRegex = """\{>>}(.*?)\{<<}""".toRegex()

    private val tagRegex = """\{([a-z]+)}""".toRegex()

    override suspend fun main() {
        val biliMessage = BiliBiliDynamic.messageChannel.receive()

        withTimeout(300005) {
            val contactIdList = if (biliMessage.contact == null) {
                when (biliMessage) {
                    is DynamicMessage -> getDynamicContactList(biliMessage.uid, biliMessage.content, biliMessage.type)
                    is LiveMessage -> getLiveContactList(biliMessage.uid)
                }
            } else {
                listOf(biliMessage.contact!!)
            }

            if (!contactIdList.isNullOrEmpty()) {
                val contactList = mutableListOf<Contact>()
                contactIdList.forEach {
                    val c = findContact(it)
                    if (c != null) {
                        contactList.add(c)
                    }
                }

                val templateMap: MutableMap<String, MutableSet<Contact>> = mutableMapOf()

                val pushTemplates = when (biliMessage) {
                    is DynamicMessage -> templateConfig.dynamicPush
                    is LiveMessage -> templateConfig.livePush
                }

                val push = when (biliMessage) {
                    is DynamicMessage -> BiliData.dynamicPushTemplate
                    is LiveMessage -> BiliData.livePushTemplate
                }

                val defaultTemplate = when (biliMessage) {
                    is DynamicMessage -> templateConfig.defaultDynamicPush
                    is LiveMessage -> templateConfig.defaultLivePush
                }

                if (push.isEmpty()) {
                    if (templateMap[defaultTemplate] == null) templateMap[defaultTemplate] =
                        mutableSetOf()
                    contactList.forEach {
                        templateMap[defaultTemplate]!!.add(it)
                    }
                }

                push.forEach { (t, u) ->
                    contactList.forEach {
                        if (u.contains(it.id)) {
                            if (templateMap[t] == null) templateMap[t] = mutableSetOf()
                            templateMap[t]!!.add(it)
                        } else {
                            if (templateMap[defaultTemplate] == null) templateMap[defaultTemplate] =
                                mutableSetOf()
                            templateMap[defaultTemplate]!!.add(it)
                        }
                    }
                }

                val templateMsgMap: MutableMap<String, List<Message>> = mutableMapOf()
                templateMap.forEach {
                    templateMsgMap[it.key] = when (biliMessage) {
                        is DynamicMessage -> biliMessage.buildMessage(pushTemplates[it.key]!!, contactList.first())
                        is LiveMessage -> biliMessage.buildMessage(pushTemplates[it.key]!!, contactList.first())
                    }
                }

                for (temp in templateMap) {
                    temp.value.forEach {
                        templateMsgMap[temp.key]?.let { it1 ->
                            val aa = atAll[it.id]?.get(biliMessage.uid) ?: atAll[it.id]?.get(0L)
                            if (biliMessage.contact == null && it is Group && it.botPermission.level > 0) {
                                var isAtAll = false
                                if (aa != null && aa.isNotEmpty()) {
                                    if (aa.contains(AtAllType.ALL)) isAtAll = true
                                    else when (biliMessage) {
                                        is DynamicMessage ->
                                            if (aa.contains(AtAllType.DYNAMIC) || aa.contains(biliMessage.type.toAtAllType()))
                                                isAtAll = true
                                        is LiveMessage -> if (aa.contains(AtAllType.LIVE)) isAtAll = true
                                    }
                                }
                                val gwp = when (biliMessage) {
                                    is DynamicMessage -> if (biliMessage.type == DynamicType.DYNAMIC_TYPE_AV) BiliBiliDynamic.videoGwp else null
                                    is LiveMessage -> BiliBiliDynamic.liveGwp
                                }
                                val hasPerm = it.permitteeId.getPermittedPermissions().any { it.id == gwp }
                                if (isAtAll || hasPerm) {
                                    if (atAllPlus == "SINGLE_MESSAGE" || it1.last().content.contains("[转发消息]")) {
                                        it.sendMessage(it1.plusElement(buildMessageChain { +AtAll }))
                                    } else {
                                        val last = it1.last().plus("\n").plus(AtAll)
                                        it.sendMessage(it1.dropLast(1).plusElement(last))
                                    }
                                } else {
                                    it.sendMessage(it1)
                                }
                            } else {
                                it.sendMessage(it1)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun Contact.sendMessage(messages: List<Message>) {
        messages.forEach {
            sendMessage(it)
            delay(messageInterval)
        }
        delay(pushInterval)
    }

    fun DynamicType.toAtAllType() =
        when (this) {
            DynamicType.DYNAMIC_TYPE_AV -> AtAllType.VIDEO
            DynamicType.DYNAMIC_TYPE_MUSIC -> AtAllType.MUSIC
            DynamicType.DYNAMIC_TYPE_ARTICLE -> AtAllType.ARTICLE
            else -> AtAllType.DYNAMIC
        }

    fun DynamicType.toFilterType() =
        when (this) {
            DynamicType.DYNAMIC_TYPE_WORD,
            DynamicType.DYNAMIC_TYPE_DRAW,
            DynamicType.DYNAMIC_TYPE_COMMON_SQUARE,
            DynamicType.DYNAMIC_TYPE_COMMON_VERTICAL,
            DynamicType.DYNAMIC_TYPE_UNKNOWN,
            DynamicType.DYNAMIC_TYPE_NONE -> DynamicFilterType.DYNAMIC

            DynamicType.DYNAMIC_TYPE_FORWARD -> DynamicFilterType.FORWARD
            DynamicType.DYNAMIC_TYPE_AV,
            DynamicType.DYNAMIC_TYPE_PGC -> DynamicFilterType.VIDEO

            DynamicType.DYNAMIC_TYPE_MUSIC -> DynamicFilterType.MUSIC
            DynamicType.DYNAMIC_TYPE_ARTICLE -> DynamicFilterType.ARTICLE
            DynamicType.DYNAMIC_TYPE_LIVE,
            DynamicType.DYNAMIC_TYPE_LIVE_RCMD -> DynamicFilterType.LIVE
        }


    private suspend fun getDynamicContactList(uid: Long, content: String, type: DynamicType): MutableSet<String>? =
        mutex.withLock {
            return try {
                val all = dynamic[0] ?: return null
                val list: MutableSet<String> = mutableSetOf()
                list.addAll(all.contacts)
                val subData = dynamic[uid] ?: return list
                list.addAll(subData.contacts)
                list.removeAll(subData.banList.keys)
                list.filter { contact ->
                    if (filter.containsKey(contact) && (filter[contact]!!.containsKey(uid) || filter[contact]!!.containsKey(0L))) {
                        val dynamicFilter = filter[contact]!![uid] ?: filter[contact]!![0L]!!
                        val typeSelect = dynamicFilter.typeSelect
                        if (typeSelect.list.isNotEmpty()) {
                            val b = typeSelect.list.contains(type.toFilterType())
                            when (typeSelect.mode) {
                                FilterMode.WHITE_LIST -> if (!b) return@filter false
                                FilterMode.BLACK_LIST -> if (b) return@filter false
                            }
                        }
                        val regularSelect = dynamicFilter.regularSelect
                        if (regularSelect.list.isNotEmpty()) {
                            regularSelect.list.forEach {
                                val b = Regex(it).containsMatchIn(content)
                                when (regularSelect.mode) {
                                    FilterMode.WHITE_LIST -> if (!b) return@filter false
                                    FilterMode.BLACK_LIST -> if (b) return@filter false
                                }
                            }
                        }
                    }
                    true
                }.toMutableSet()
            } catch (e: Throwable) {
                logger.warning(e)
                null
            }
        }

    private suspend fun getLiveContactList(uid: Long): MutableSet<String>? = mutex.withLock {
        return try {
            val all = dynamic[0] ?: return null
            val list: MutableSet<String> = mutableSetOf()
            list.addAll(all.contacts)
            val subData = dynamic[uid] ?: return list
            list.addAll(subData.contacts)
            list.removeAll(subData.banList.keys)
            list.filter { contact ->
                if (filter.containsKey(contact) && (filter[contact]!!.containsKey(uid) || filter[contact]!!.containsKey(0L))) {
                    val dynamicFilter = filter[contact]!![uid] ?: filter[contact]!![0L]!!
                    val typeSelect = dynamicFilter.typeSelect
                    if (typeSelect.list.isNotEmpty()) {
                        val b = typeSelect.list.contains(DynamicFilterType.LIVE)
                        when (typeSelect.mode) {
                            FilterMode.WHITE_LIST -> if (!b) return@filter false
                            FilterMode.BLACK_LIST -> if (b) return@filter false
                        }
                    }
                }
                true
            }.toMutableSet()
        } catch (e: Throwable) {
            logger.warning(e)
            null
        }
    }

    suspend fun LiveMessage.buildMessage(template: String, contact: Contact): List<Message> {
        return buildMsgList(template) {
            buildLiveMsg(it, this, contact)
        }
    }

    private suspend fun buildLiveMsg(ms: String, lm: LiveMessage, contact: Contact): String {
        var p = 0
        var content = ms

        while (true) {
            val key = tagRegex.find(content, p) ?: break
            val rep = when (key.destructured.component1()) {
                "name" -> lm.uname
                "uid" -> lm.uid.toString()
                "rid" -> lm.rid.toString()
                "time" -> lm.time
                "type" -> "直播"
                "title" -> lm.title
                "area" -> lm.area
                "link" -> lm.link
                "cover" -> uploadImage(lm.cover, CacheType.IMAGES, contact).serializeToMiraiCode()
                "draw" -> {
                    if (lm.drawPath == null) {
                        "[绘制直播图片失败]"
                    } else {
                        val path = cachePath.resolve(lm.drawPath)
                        if (path.notExists()) {
                            "[未找到绘制的直播图片]"
                        } else {
                            contact.uploadImage(
                                cachePath.resolve(lm.drawPath).readBytes().toExternalResource().toAutoCloseable()
                            ).serializeToMiraiCode()
                        }
                    }
                }

                else -> {
                    "[不支持的类型: ${key.destructured.component1()}]"
                }
            }
            content = content.replaceRange(key.range, rep)
            p = key.range.first + rep.length
        }
        return content
    }

    suspend fun DynamicMessage.buildMessage(template: String, contact: Contact): List<Message> {

        val msgList = mutableListOf<Message>()

        val msgTemplate = template.replace("\n", "\\n").replace("\r", "\\r")

        val forwardCardTemplate = templateConfig.forwardCard

        val res = forwardRegex.findAll(msgTemplate)

        var index = 0

        res.forEach { mr ->
            if (mr.range.first > index) {
                msgList.addAll(buildMsgList(msgTemplate.substring(index, mr.range.first)) {
                    buildMsg(it, this, contact)
                })
            }
            msgList.add(buildForwardMessage(contact,
                object : ForwardMessage.DisplayStrategy {
                    override fun generateBrief(forward: RawForwardMessage): String {
                        return buildSimpleMsg(forwardCardTemplate.brief, this@buildMessage)
                    }

                    override fun generatePreview(forward: RawForwardMessage): List<String> {
                        return buildSimpleMsg(forwardCardTemplate.preview, this@buildMessage).split(
                            "\\n",
                            "\n"
                        )
                    }

                    override fun generateSummary(forward: RawForwardMessage): String {
                        return buildSimpleMsg(forwardCardTemplate.summary, this@buildMessage)
                    }

                    override fun generateTitle(forward: RawForwardMessage): String {
                        return buildSimpleMsg(forwardCardTemplate.title, this@buildMessage)
                    }
                }
            ) {
                buildMsgList(mr.destructured.component1()) {
                    buildMsg(it, this@buildMessage, contact)
                }.forEach {
                    contact.bot named this@buildMessage.uname at this@buildMessage.timestamp says it
                }
            })
            index = mr.range.last + 1
        }

        if (index < msgTemplate.length) {
            msgList.addAll(buildMsgList(msgTemplate.substring(index, msgTemplate.length)) {
                buildMsg(it, this, contact)
            })
        }

        return msgList
    }

    private inline fun buildMsgList(template: String, build: (ms: String) -> String): List<Message> {
        val msgs = template.split("\\r", "\r")
        val msgList = mutableListOf<Message>()
        msgs.forEach { ms ->
            msgList.add(MiraiCode.deserializeMiraiCode(build(ms)))
        }
        return msgList.toList()
    }

    private fun buildSimpleMsg(ms: String, dm: DynamicMessage): String {
        return ms.replace("{name}", dm.uname)
            .replace("{uid}", dm.uid.toString())
            .replace("{did}", dm.did)
            .replace("{time}", dm.time)
            .replace("{type}", dm.type.text)
            .replace("{content}", dm.content)
            .replace("{link}", dm.links?.get(0)?.value!!)
    }

    private suspend fun buildMsg(ms: String, dm: DynamicMessage, contact: Contact): String {
        var p = 0
        var content = ms

        while (true) {
            val key = tagRegex.find(content, p) ?: break
            val rep = when (key.destructured.component1()) {
                "name" -> dm.uname
                "uid" -> dm.uid.toString()
                "did" -> dm.did
                "time" -> dm.time
                "type" -> dm.type.text
                "content" -> dm.content
                "link" -> dm.links?.get(0)?.value!!
                "images" -> {
                    buildString {
                        dm.images?.forEach {
                            appendLine(uploadImage(it, CacheType.IMAGES, contact).serializeToMiraiCode())
                        }
                    }
                }

                "draw" -> {
                    if (dm.drawPath == null) {
                        "[绘制动态失败]"
                    } else {
                        val path = cachePath.resolve(dm.drawPath)
                        if (path.notExists()) {
                            "[未找到绘制的动态]"
                        } else {
                            contact.uploadImage(
                                cachePath.resolve(dm.drawPath).readBytes().toExternalResource().toAutoCloseable()
                            ).serializeToMiraiCode()
                        }
                    }
                }

                else -> {
                    "[不支持的类型: ${key.destructured.component1()}]"
                }
            }
            content = content.replaceRange(key.range, rep)
            p = key.range.first + rep.length
        }
        return content
    }
}