plugins {
    `java-library`
}

dependencies {

    implementation(projects.scijavaCommon3)

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")
}

tasks.test { useJUnitPlatform() }