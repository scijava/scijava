plugins {
    `java-library`
}

dependencies {

    implementation("org.scijava:scijava-collections")
    implementation(imglib2.imglib2)
    implementation(misc.commons.math3)

    testImplementation(junit5.junit.jupiter.api)
    testImplementation(junit5.junit.jupiter.engine)
    testImplementation(imglib2.imglib2.roi)
    testImplementation("org.scijava:scijava-testutil")
}