plugins {
    `java-library`
}

dependencies {

    implementation(projects.scijavaCommon3)
    implementation(projects.scijavaDiscovery)
    implementation(projects.scijavaLog2)
    implementation(projects.scijavaPriority)
    implementation(libs.guava)

    testImplementation(junit5.junit.jupiter.api)
    testImplementation(junit5.junit.jupiter.engine)
    testImplementation(projects.scijavaTestutil)
}