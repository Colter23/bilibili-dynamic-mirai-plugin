package top.colter.mirai.plugin.bilibili

import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.id
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.info
import top.colter.bilibili.client.BiliClient
import top.colter.mirai.plugin.bilibili.account.AccountManager
import top.colter.mirai.plugin.bilibili.account.BiliAccount
import top.colter.mirai.plugin.bilibili.account.BiliApplication
import top.colter.mirai.plugin.bilibili.database.PushTemplate


object BiliBiliDynamic : KotlinPlugin(
    JvmPluginDescription(
        id = "top.colter.bilibili-dynamic-mirai-plugin",
        name = "BiliBili Dynamic",
        version = "4.0.0-alpha.1",
    ) {
        author("Colter")
//        dependsOn("xyz.cssxsh.mirai.plugin.mirai-skia-plugin", ">= 1.1.0")
    }
) {

    object DynamicBiliApplication: BiliApplication {
        override val id: String = BiliBiliDynamic.id
        override val name: String = "动态检测"
        override val description: String = "检测B站动态"
        override val accounts: List<BiliAccount> by AccountManager()
    }


//    private val additional by lazy {
//        try {
//            Class.forName("xyz.cssxsh.mirai.plugin.mirai-skia-plugin", false, jvmPluginClasspath.pluginClassLoader)
//            false
//        } catch (_: ClassNotFoundException) {
//            true
//        }
//    }


//    val dynamicChannel = Channel<DynamicDetail>(20)

    val client = BiliClient()

    override fun PluginComponentStorage.onLoad() {
//        client.storage.container.addAll(DynamicBiliApplication.account.first().cookie)

        // 插件授权
//        runAfterStartup {
//            val colter = Bot.instances[0].getFriend(3375582524L)
//            if (colter != null) {
//                colter.permitteeId.permit(parentPermission.id)
//                colter.permitteeId.getPermittedPermissions().forEach {
//                    println(it.toString())
//                }
//            }else {
//                println("未找到")
//            }
//        }


    }

    @OptIn(ConsoleFrontEndImplementation::class, ConsoleExperimentalApi::class)
    override fun onEnable() {
        logger.info { "Plugin loaded" }

//        if (additional) {
//            with(jvmPluginClasspath) {
//                downloadAndAddToPath(pluginIndependentLibrariesClassLoader, listOf("io.github.kasukusakura:silk-codec:0.0.5"))
//            }
//            try {
//                NativeLoader.initialize(dataFolder)
//            } catch (error: UnsatisfiedLinkError) {
//                logger.error("Silk Codec 初始化失败, folder: $dataFolder", error)
//            }
//        }

        PushTemplate.reload()
//        BiliSubscribe.reload()
//        BiliUser.reload()
//        ContactGroup.reload()
//        BlackWhiteList.reload()
//
//        FriendMessageListener.registerTo(globalEventChannel())
//        GroupMessageListener.registerTo(globalEventChannel())
//        MessageListener.registerTo(globalEventChannel())






        // region Context

        // endregion

        // 文件变动事件
//        val ws = dataFolder.toPath().fileSystem.newWatchService()
//        StandardWatchEventKinds.OVERFLOW
//        dataFolder.toPath().register(ws, StandardWatchEventKinds.ENTRY_MODIFY)
//
//        ws.take().pollEvents().forEach {
//            it.context()
//        }

//        launch {
//            // 进度条
//            val p = MiraiConsole.newProcessProgress()
//            repeat(100){
//                p.update(it.toLong()/100)
//                p.updateText("**$it**")
//                p.rerender()
//
//                delay(100)
//            }
//            p.updateText("更新完成!")
//            p.close()
//        }



    }

    override fun onDisable() {
        PushTemplate.save()
//        dynamicChannel.close()

    }
}