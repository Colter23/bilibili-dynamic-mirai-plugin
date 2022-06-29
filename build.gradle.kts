plugins {
    val kotlinVersion = "1.6.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.11.1"
}

group = "top.colter"
version = "3.0.0-BETA1"

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies{
    implementation("com.google.zxing:javase:3.4.1")

    implementation("io.ktor:ktor-client-serialization:1.6.7") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.slf4j")
        exclude(group = "io.ktor", module = "ktor-client-core")
    }
    implementation("io.ktor:ktor-client-encoding:1.6.7") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.slf4j")
        exclude(group = "io.ktor", module = "ktor-client-core")
    }

    val skikoVersion = "0.7.12"
    //api("org.jetbrains.skiko:skiko-awt:0.7.20")
    //临时
    implementation("org.jetbrains.skiko:skiko-awt-runtime-linux-x64:$skikoVersion")
    implementation("org.jetbrains.skiko:skiko-awt-runtime-windows-x64:$skikoVersion")
    implementation("org.jetbrains.skiko:skiko-awt-runtime-linux-arm64:$skikoVersion")
    implementation("org.jetbrains.skiko:skiko-awt-runtime-macos-x64:$skikoVersion")
    implementation("org.jetbrains.skiko:skiko-awt-runtime-macos-arm64:$skikoVersion")

    testImplementation(kotlin("test", "1.6.21"))
}
