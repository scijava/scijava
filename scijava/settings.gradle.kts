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

pluginManagement {
//    includeBuild("../../gradle-catalog")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://maven.scijava.org/content/groups/public")
    }
}

plugins {
//    id("org.scijava.catalogs")// version "33.2.0"
}