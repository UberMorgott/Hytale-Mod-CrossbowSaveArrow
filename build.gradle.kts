plugins {
    id("java-library")
}

group = "morgott"
version = "0.0.9"

repositories {
    mavenCentral()
    maven("https://maven.hytale.com/release")
}

dependencies {
    compileOnly("com.hypixel.hytale:Server:0.6.8")
    // MixinTale Developer Tools 3.0.0 (CurseForge 1449240), unzipped into libs/: annotations + processor, never shipped
    compileOnly(files("libs/MixinTale-API-3.0.0.jar"))
    annotationProcessor(files("libs/MixinTale-Processor-3.0.0.jar"))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release = 21
        options.compilerArgs.add("-parameters")
        // Patch handlers are relocated into the game classes, which already use the game's bundled BSON and fastutil libraries
        options.compilerArgs.add("-Amixintale.allowedPackages=org.bson,it.unimi.dsi.fastutil")
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
    jar {
        archiveBaseName.set("CrossbowSaveArrow")
    }
}

java {
    // Server 0.6.8 classes are Java 25 bytecode (major 69); compile with JDK 25, emit Java 21 bytecode
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
