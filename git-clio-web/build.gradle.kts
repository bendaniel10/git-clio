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
    implementation(project(":git-clio-parse"))
    implementation(Dependencies.koinCore)
    implementation(Dependencies.ktorServerCore)
    implementation(Dependencies.ktorServerNetty)
    implementation(Dependencies.koinKtorSl4jLogger)
    implementation(Dependencies.kotlinXDateTime)
    implementation(Dependencies.ktorServerFreemarker)
}
