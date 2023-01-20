plugins {
    `java-library`
}

dependencies {

    implementation(scijava.scijava.common)
    implementation(libs.gson)

    testImplementation(junit5.junit.jupiter.api)
    testImplementation(junit5.junit.jupiter.engine)
}

tasks.test { useJUnitPlatform() }