
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") version "2.0.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("io.github.surpsg.code-lines") version "0.0.1"
}

group = "me.cirosanchez"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://jitpack.io")
    maven("https://repo.flyte.gg/releases")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://maven.refinedev.xyz/public-repo")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    implementation("com.github.cirosanchez:cLib:fcb852536c")
    implementation("org.mongodb:bson:4.3.4")
    implementation("fr.mrmicky:fastboard:2.1.3")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("net.luckperms:api:5.4")

    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))



    val targetJavaVersion = 21
    kotlin {
        jvmToolchain(targetJavaVersion)
    }

    tasks.build {
        dependsOn("shadowJar")
    }

    tasks.processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    tasks {

        clean {
            doFirst {
                val file = file("run/plugins/Factions/messages.yml")

                if (file.exists()){
                    file.delete()
                    println("messages.yml deleted")
                }

                val anotherFile = file("run/plugins/Factions/config.yml")

                if (anotherFile.exists()){
                    anotherFile.delete()
                    println("config.yml deleted")
                }

                val scoreboardFile = file("run/plugins/Factions/scoreboard.yml")

                if (scoreboardFile.exists()){
                    scoreboardFile.delete()
                    println("scoreboard.yml deleted")
                }
            }
        }
        runServer {
            minecraftVersion("1.21.1")
        }
    }
}
tasks.withType<JavaCompile> { // Preserve parameter names in the bytecode
    options.compilerArgs.add("-parameters")
}

tasks.withType<KotlinJvmCompile> { // optional: if you're using Kotlin
    compilerOptions {
        javaParameters = true
    }
}
