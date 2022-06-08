package top.colter.mirai.plugin.bilibili.tasker

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.code.MiraiCode
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.RawForwardMessage
import net.mamoe.mirai.message.data.buildForwardMessage
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.BiliDynamicConfig
import top.colter.mirai.plugin.bilibili.BiliDynamicData
import top.colter.mirai.plugin.bilibili.BiliDynamicData.dynamic
import top.colter.mirai.plugin.bilibili.data.DynamicMessage
import top.colter.mirai.plugin.bilibili.data.LiveMessage
import top.colter.mirai.plugin.bilibili.tasker.BiliDataTasker.mutex
import top.colter.mirai.plugin.bilibili.utils.CacheType
import top.colter.mirai.plugin.bilibili.utils.cachePath
import top.colter.mirai.plugin.bilibili.utils.findContact
import top.colter.mirai.plugin.bilibili.utils.uploadImage
import kotlin.io.path.notExists
import kotlin.io.path.readBytes
import kotlin.text.Regex.Companion.escapeReplacement

object SendTasker : BiliTasker() {

    override val interval: Int = 0

    private val templateConfig by BiliDynamicConfig::templateConfig

    override suspend fun main() {
        val biliMessage = BiliBiliDynamic.messageChannel.receive()

        val contactIdList = if (biliMessage.contact == null) {
            when(biliMessage){
                is DynamicMessage -> getDynamicContactList(biliMessage.uid)
                is LiveMessage -> getLiveContactList(biliMessage.uid)
            }
        } else {
            listOf(biliMessage.contact!!)
        }


        if (contactIdList != null) {
            val contactList = mutableListOf<Contact>()
            contactIdList.forEach {
                val c = findContact(it)
                if (c != null) {
                    contactList.add(c)
                }
            }

            val templateMap: MutableMap<String, MutableSet<Contact>> = mutableMapOf()

            val pushTemplates = when(biliMessage){
                is DynamicMessage -> templateConfig.dynamicPush
                is LiveMessage -> templateConfig.livePush
            }

            val push = when(biliMessage){
                is DynamicMessage -> BiliDynamicData.dynamicPushTemplate
                is LiveMessage -> BiliDynamicData.livePushTemplate
            }

            val defaultTemplate = when(biliMessage){
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
                templateMsgMap[it.key] = when(biliMessage) {
                    is DynamicMessage -> biliMessage.buildMessage(contactList.first(), pushTemplates[it.key]!!)
                    is LiveMessage -> biliMessage.buildMessage(contactList.first(), pushTemplates[it.key]!!)
                }
            }

            for (temp in templateMap) {
                temp.value.forEach {
                    templateMsgMap[temp.key]?.let { it1 -> it.sendMessage(it1) }
                }
            }

        }
    }

    private suspend fun getDynamicContactList(uid: Long): MutableSet<String>? = mutex.withLock {
        return try {
            val all = dynamic[0] ?: return null
            val list: MutableSet<String> = mutableSetOf()
            list.addAll(all.contacts.keys)
            val subData = dynamic[uid] ?: return list
            list.addAll(subData.contacts.keys)
            list.removeAll(subData.banList.keys)
            list
        } catch (e: Throwable) {
            null
        }
    }

    private suspend fun getLiveContactList(uid: Long): MutableSet<String>? = mutex.withLock {
        return try {
            val all = dynamic[0] ?: return null
            val list: MutableSet<String> = mutableSetOf()
            list.addAll(all.contacts.keys)
            val subData = dynamic[uid] ?: return list
            list.addAll(subData.contacts.filter { it.value[1] == '1' }.keys)
            list.removeAll(subData.banList.keys)
            list
        } catch (e: Throwable) {
            null
        }
    }

    private suspend fun Contact.sendMessage(messages: List<Message>) {
        messages.forEach {
            sendMessage(it)
            delay(100)
        }
    }

    private val forwardRegex = """\{>>}(.*?)\{<<}""".toRegex()

    private val tagRegex = """\{([a-z]+)}""".toRegex()

    suspend fun LiveMessage.buildMessage(contact: Contact, template: String): List<Message> {
        val msgList = mutableListOf<Message>()

        // TODO

        return msgList
    }

    suspend fun DynamicMessage.buildMessage(contact: Contact, template: String): List<Message> {

        val msgList = mutableListOf<Message>()

        val msgTemplate = escapeReplacement(template)

        val forwardCardTemplate = templateConfig.forwardCard

        val res = forwardRegex.findAll(msgTemplate)

        var index = 0

        res.forEach { mr ->
            if (mr.range.first > index) {
                msgList.addAll(buildMsgList(msgTemplate.substring(index, mr.range.first), this, contact))
            }
            msgList.add(buildForwardMessage(contact,
                object : ForwardMessage.DisplayStrategy {
                    override fun generateBrief(forward: RawForwardMessage): String {
                        return buildSimpleTemplate(forwardCardTemplate.brief, this@buildMessage)
                    }

                    override fun generatePreview(forward: RawForwardMessage): List<String> {
                        return buildSimpleTemplate(forwardCardTemplate.preview, this@buildMessage).split(
                            "\\n",
                            "\n"
                        )
                    }

                    override fun generateSummary(forward: RawForwardMessage): String {
                        return buildSimpleTemplate(forwardCardTemplate.summary, this@buildMessage)
                    }

                    override fun generateTitle(forward: RawForwardMessage): String {
                        return buildSimpleTemplate(forwardCardTemplate.title, this@buildMessage)
                    }
                }
            ) {
                buildMsgList(mr.destructured.component1(), this@buildMessage, contact).forEach {
                    contact.bot named this@buildMessage.uname at this@buildMessage.timestamp says it
                }
            })
            index = mr.range.last + 1
        }

        if (index < msgTemplate.length) {
            msgList.addAll(buildMsgList(msgTemplate.substring(index, msgTemplate.length), this, contact))
        }

        return msgList
    }

    private suspend fun buildMsgList(template: String, dm: DynamicMessage, contact: Contact): List<Message> {
        val msgs = template.split("\\r", "\r")
        val msgList = mutableListOf<Message>()
        msgs.forEach { ms ->
            msgList.add(MiraiCode.deserializeMiraiCode(buildMsg(ms, dm, contact)))
        }
        return msgList.toList()
    }

    private fun buildSimpleTemplate(ms: String, dm: DynamicMessage): String {
        return ms.replace("{name}", dm.uname)
            .replace("{uid}", dm.uid.toString())
            .replace("{did}", dm.did)
            .replace("{time}", dm.time)
            .replace("{type}", dm.type)
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
                "type" -> dm.type
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