plugins {
    `java-library`
    id("org.gradlex.extra-java-module-info") version "1.2"
}

dependencies {

    annotationProcessor(libs.therapi.processor)
    implementation(libs.therapi)
    implementation(projects.scijavaDiscovery)
    implementation(projects.scijavaParse2)

    testImplementation(junit5.junit.jupiter.api)
    testImplementation(junit5.junit.jupiter.engine)
}

extraJavaModuleInfo {
    automaticModule("com.github.therapi:therapi-runtime-javadoc-scribe", "therapi.runtime.javadoc.scribe")
    automaticModule("com.github.therapi:therapi-runtime-javadoc", "therapi.runtime.javadoc")
}

tasks.test { useJUnitPlatform() }