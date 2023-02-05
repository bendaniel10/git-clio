plugins {
    id("java")
    kotlin("jvm")
}

group = "com.bendaniel10"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(Dependencies.koinCore)
}