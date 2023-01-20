plugins {
    `java-library`
}

dependencies {

    implementation(scijava.parsington)
    implementation(projects.scijavaCollections)

    testImplementation(junit5.junit.jupiter.api)
    testImplementation(junit5.junit.jupiter.engine)
}