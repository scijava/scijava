plugins {
    `java-library`
}

dependencies {

    implementation(projects.scijavaDiscovery)
    implementation(projects.scijavaOpsSpi)

    testImplementation(junit5.junit.jupiter.api)
    testImplementation(junit5.junit.jupiter.engine)
}