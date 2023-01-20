plugins {
    `java-library`
}

dependencies {

    implementation(projects.scijava.scijavaCommon3)

    testImplementation(junit5.junit.jupiter.api)
    testImplementation(junit5.junit.jupiter.engine)
}