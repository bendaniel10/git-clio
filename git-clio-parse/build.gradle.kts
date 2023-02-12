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
    implementation(project(":git-clio-fetch"))
    implementation(Dependencies.koinCore)
    implementation(Dependencies.ktorKotlinxSerialization)
    implementation(Dependencies.kotlinXDateTime)
}