plugins {
    `java-library`
}

dependencies {
    implementation(projects.scijavaFunction)
    implementation(projects.scijavaStruct)
    implementation(projects.scijavaTypes)

    // missing
    implementation(projects.scijavaCommon3)
    implementation(projects.scijavaLog2)
    implementation(projects.scijavaDiscovery)
    implementation(libs.guava)
}

val generateCode by tasks.registering(GenerateCode::class)
sourceSets {
    main {
        java {
            srcDir(generateCode)
        }
    }
}