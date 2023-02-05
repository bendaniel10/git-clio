plugins {
    id("java")
    kotlin("jvm")
    kotlin("plugin.serialization") version Versions.kotlinVersion
}

group = "com.bendaniel10"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(Dependencies.ktorClientLogging)
    implementation(Dependencies.ktorCore)
    implementation(Dependencies.ktorCioEngine)
    implementation(Dependencies.ktorContentNegotiation)
    implementation(Dependencies.ktorKotlinxSerialization)
    implementation(Dependencies.ktorAuth)
    implementation(Dependencies.koinKtor)
    implementation(Dependencies.koinKtorSl4jLogger)
    implementation(Dependencies.sl4jSimple)
}
