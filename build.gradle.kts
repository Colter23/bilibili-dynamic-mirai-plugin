plugins {
    val kotlinVersion = "1.7.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.12.0"
}

group = "top.colter"
version = "3.0.0-BETA2"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies{
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
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.0.3") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.slf4j")
    }

    implementation("com.google.zxing:javase:3.5.0")
    compileOnly("xyz.cssxsh.mirai:mirai-skia-plugin:1.1.4")

    testImplementation(kotlin("test", "1.7.0"))
    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-windows-x64:0.7.23")
    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-linux-x64:0.7.23")
    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-linux-arm64:0.7.23")
    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-macos-x64:0.7.23")
    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-macos-arm64:0.7.23")
}
