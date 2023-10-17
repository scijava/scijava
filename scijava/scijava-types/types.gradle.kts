plugins {
    `java-library`
}

dependencies {

    implementation(projects.scijavaCommon3)
    implementation(projects.scijavaDiscovery)
    implementation(projects.scijavaLog2)
    implementation(projects.scijavaPriority)
    implementation("com.google.guava:guava:31.1-jre")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testImplementation(projects.scijavaTestutil)
}

tasks.test { useJUnitPlatform() }