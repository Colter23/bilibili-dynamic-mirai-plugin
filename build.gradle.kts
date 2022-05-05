plugins {
    val kotlinVersion = "1.6.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.11.0-RC"
}

group = "top.colter"
version = "3.0.0-M1"

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies{
    implementation("com.google.zxing:javase:3.4.1")
    implementation("org.jetbrains.skiko:skiko-awt-runtime-windows-x64:0.7.18")
    testImplementation(kotlin("test", "1.6.21"))
}
