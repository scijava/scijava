rootProject.name = "imagej"

includeBuild("../scijava")

file(".").list()?.forEach {
    if (it.startsWith("imagej-"))
        include(it)
}

rootProject.children.forEach {
    it.buildFileName = "${it.name.substringAfter("imagej-")}.gradle.kts"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

//gradle.rootProject {
//    group = "net.imagej"
//}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://maven.scijava.org/content/groups/public")
        maven("https://maven.scijava.org/content/repositories/releases/")
        maven("https://maven.scijava.org/content/repositories/public/")
    }
}

plugins {
    id("org.scijava.catalogs") version "33.2.0"
}