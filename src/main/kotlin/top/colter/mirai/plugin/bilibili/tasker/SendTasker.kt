package top.colter.mirai.plugin.bilibili.tasker

import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.BiliSubscribeData.dynamic
import top.colter.mirai.plugin.bilibili.data.DynamicMessage
import top.colter.mirai.plugin.bilibili.tasker.BiliDataTasker.mutex
import top.colter.mirai.plugin.bilibili.utils.cachePath

object SendTasker: BiliTasker() {

    override val interval: Int = 0

    override suspend fun main() {
        val dynamicMessage = BiliBiliDynamic.messageChannel.receive()


        //dynamicMessage.sendMessage(dynamicMessage.buildMessage())
    }

    private suspend fun getDynamicContactList(uid: Long, isVideo: Boolean): MutableSet<String>? = mutex.withLock {
        return try {
            val all = dynamic[0] ?: return null
            val list: MutableSet<String> = mutableSetOf()
            list.addAll(all.contacts.keys)
            val subData = dynamic[uid] ?: return list
            if (isVideo) list.addAll(subData.contacts.filter { it.value[0] == '1' || it.value[0] == '2' }.keys)
            else list.addAll(subData.contacts.filter { it.value[0] == '1' }.keys)
            list.removeAll(subData.banList.keys)
            list
        } catch (e: Throwable) {
            null
        }
    }

    fun DynamicMessage.sendMessage(message: Message){



    }

    suspend fun DynamicMessage.buildMessage(contact: Contact): Message {


        val img = cachePath.resolve(drawPath).toFile().uploadAsImage(contact)

        return buildMessageChain {
            +img
            +PlainText(this@buildMessage.did)
        }
    }

}