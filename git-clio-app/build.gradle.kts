import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version Versions.shadowJarVersion
}

group = "com.bendaniel10"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveFileName.set("gitclio.jar")
        manifest {
            attributes(mapOf("Main-Class" to "com.bendaniel10.Main"))
        }
    }
}

dependencies {
    implementation(Dependencies.koinCore)
}