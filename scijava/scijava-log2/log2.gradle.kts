plugins {
    `java-library`
}

dependencies {
    testImplementation(junit5.junit.jupiter.api)
    testImplementation(junit5.junit.jupiter.engine)
}

tasks.test { useJUnitPlatform() }