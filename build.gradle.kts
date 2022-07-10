plugins {
    val kotlinVersion = "1.6.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.12.0"
}

group = "top.colter"
version = "3.0.0-BETA1.1"

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies{
    implementation("com.google.zxing:javase:3.4.1")

    implementation("io.ktor:ktor-client-okhttp:2.0.3") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.slf4j")
    }
    implementation("io.ktor:ktor-client-encoding:2.0.3") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.slf4j")
    }
    implementation("io.ktor:ktor-client-content-negotiation:2.0.3") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.slf4j")
    }
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.0.3") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.slf4j")
    }
    implementation("com.squareup.okhttp3:okhttp:4.10.0") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.slf4j")
    }

    compileOnly("xyz.cssxsh.mirai:mirai-skia-plugin:1.1.3")

    testImplementation(kotlin("test", "1.6.21"))
    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-windows-x64:0.7.20")
    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-linux-x64:0.7.20")
    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-linux-arm64:0.7.20")
    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-macos-x64:0.7.20")
    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-macos-arm64:0.7.20")
}
