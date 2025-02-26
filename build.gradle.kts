plugins {
    val kotlinVersion = "2.0.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.15.0"
}

group = "top.colter"
version = "4.0.0-alpha.1"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {

    implementation("io.ktor:ktor-client-okhttp:3.0.3")
    implementation("io.ktor:ktor-client-encoding:3.0.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.3")
    implementation("com.cronutils:cron-utils:9.2.0")

    implementation("com.google.zxing:javase:3.5.1")
    implementation("org.jetbrains.kotlinx:atomicfu:0.19.0")
    api("org.jetbrains.skiko:skiko-awt:0.7.54")

    implementation("top.colter.skiko:skiko-layout:0.0.1")
    implementation("top.colter.bilibili:bilibili-client:0.0.1")

    implementation("xyz.cssxsh.mirai:mirai-skia-plugin:1.3.0") {
        exclude("org.jetbrains.skiko:skiko-awt")
    }
//    implementation(project(mapOf("path" to ":")))

    testImplementation("net.mamoe:mirai-core-mock:2.15.0")

    testImplementation(kotlin("test", "2.0.0"))
    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-windows-x64:0.7.44")
//    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-linux-x64:0.7.27")
//    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-linux-arm64:0.7.27")
//    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-macos-x64:0.7.27")
//    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-macos-arm64:0.7.27")
}

mirai {
    jvmTarget = JavaVersion.VERSION_11
}