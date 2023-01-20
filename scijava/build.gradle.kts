plugins {
    `java-library`
}

allprojects {
    group = "org.scijava"
    version = "0-SNAPSHOT"
}

dependencies {
    for (subproject in subprojects)
        implementation(subproject)
}