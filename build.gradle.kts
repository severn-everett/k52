import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    kotlin("jvm") version "1.9.23"
    id("io.ktor.plugin") version "2.3.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
}

group = "com.severett"
version = "0.0.1"

application {
    mainClass.set("com.severett.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_1_9
    }
}

repositories {
    mavenCentral()
}

dependencies {
    val ktorVersion: String by project
    val logbackVersion: String by project
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.github.oshai:kotlin-logging:6.0.9")
    implementation("io.insert-koin:koin-ktor:$ktorVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$ktorVersion")
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-server-resources")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}
