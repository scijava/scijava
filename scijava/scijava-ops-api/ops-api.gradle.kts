plugins {
    `java-library`
}

dependencies {
    implementation(projects.scijavaFunction)
    implementation(projects.scijavaPriority)
    implementation(projects.scijavaStruct)
    implementation(projects.scijavaTypes)

    // missing
    implementation(projects.scijavaCommon3)
    implementation(projects.scijavaLog2)
    implementation(projects.scijavaDiscovery)
    implementation("com.google.guava:guava:31.1-jre")
}

val generateScijava by tasks.registering(GenerateScijava::class)
sourceSets.main { java.srcDir(generateScijava) }