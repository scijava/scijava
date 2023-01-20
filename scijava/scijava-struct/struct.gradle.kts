plugins {
    `java-library`
}

dependencies {
    implementation(projects.scijavaCommon3)
    implementation(projects.scijavaTypes)

    // apparently needed
    implementation(projects.scijavaLog2)
    implementation(projects.scijavaDiscovery)
    implementation(libs.guava)
}