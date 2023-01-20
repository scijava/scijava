rootProject.name = "scijava"

file(".").list()?.forEach {
    if (it.startsWith("scijava-"))
        include(it)
}

rootProject.children.forEach {
    val name = it.name.substringAfter("scijava-")
//    it.name =  name
    it.buildFileName = "$name.gradle.kts"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

//gradle.rootProject {
//    group = "org.scijava"
//}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
//        maven("https://maven.scijava.org/content/groups/public")
    }
    versionCatalogs {
        create("libs") {
            library("therapi", "com.github.therapi:therapi-runtime-javadoc:0.12.0")
            library("therapi-processor", "com.github.therapi:therapi-runtime-javadoc-scribe:0.12.0")
            library("guava", "com.google.guava:guava:28.2-jre")
            library("gson", "com.google.code.gson:gson:2.8.9")
//            library("commons-lang3", "org.apache.commons", "commons-lang3").version {
//                strictly("[3.8, 4.0[")
//                prefer("3.9")
//            }
        }
    }
}

plugins {
    id("org.scijava.catalogs") version "33.2.0"
}