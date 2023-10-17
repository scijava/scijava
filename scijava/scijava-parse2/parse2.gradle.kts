plugins {
    `java-library`
}

dependencies {

    implementation("org.scijava:parsington:3.1.0")
    implementation(projects.scijavaCollections)

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")
}

tasks.test { useJUnitPlatform() }