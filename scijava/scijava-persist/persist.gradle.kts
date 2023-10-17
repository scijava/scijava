plugins {
    `java-library`
}

dependencies {

    implementation("org.scijava:scijava-common:2.94.2")
    implementation("com.google.code.gson:gson:2.8.9")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")
}

tasks.test { useJUnitPlatform() }