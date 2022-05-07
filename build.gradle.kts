plugins {
    val kotlinVersion = "1.6.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.10.2"
}

group = "top.colter"
version = "2.2.2"

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies{
    implementation("com.vdurmont:emoji-java:5.1.1")
    implementation("com.google.zxing:javase:3.4.1")
    implementation("org.jetbrains.skiko:skiko-awt-runtime-windows-x64:0.7.18")
    testImplementation(kotlin("test", "1.6.21"))
}
