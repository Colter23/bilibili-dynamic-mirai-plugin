plugins {
    val kotlinVersion = "1.6.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.10.1"
}

group = "top.colter"
version = "2.2.1"

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies{
    implementation("com.vdurmont:emoji-java:5.1.1")
    implementation("com.google.zxing:javase:3.4.1")
    testImplementation(kotlin("test", "1.6.0"))
}
