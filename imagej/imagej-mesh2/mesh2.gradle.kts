plugins {
    `java-library`
    id("org.gradlex.extra-java-module-info") version "1.2"
}

dependencies {

    implementation("org.scijava:scijava-collections")
    implementation("net.imglib2:imglib2:6.1.0")
    implementation("org.apache.commons:commons-math3:3.6.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testImplementation("net.imglib2:imglib2-roi:0.14.1")
    testImplementation("org.scijava:scijava-testutil")
}

extraJavaModuleInfo {
    automaticModule("org.apache.commons:commons-math3", "commons.math3")
}