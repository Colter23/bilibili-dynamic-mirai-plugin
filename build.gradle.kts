plugins {
    val kotlinVersion = "1.7.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.13.0-M1"
    id("net.mamoe.maven-central-publish") version "0.7.1"
}

group = "top.colter"
version = "3.2.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

mavenCentralPublish {
    useCentralS01()
    singleDevGithubProject("Colter23", "bilibili-dynamic-mirai-plugin")
    licenseFromGitHubProject("AGPL-3.0", "master")
    publication {
        artifact(tasks.getByName("buildPlugin"))
    }
}

dependencies {
    implementation("io.ktor:ktor-client-okhttp:2.1.0") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.slf4j")
    }
    implementation("io.ktor:ktor-client-encoding:2.1.0") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.slf4j")
    }
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.1.0") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.slf4j")
    }

    implementation("com.google.zxing:javase:3.5.0")
    compileOnly("xyz.cssxsh.mirai:mirai-skia-plugin:1.1.8")

    testImplementation(kotlin("test", "1.7.0"))
    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-windows-x64:0.7.27")
    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-linux-x64:0.7.27")
    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-linux-arm64:0.7.27")
    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-macos-x64:0.7.27")
    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-macos-arm64:0.7.27")
}

mirai {
    jvmTarget = JavaVersion.VERSION_11
}