plugins {
    `java-library`
}


dependencies {

    implementation(projects.imagej.imagejMesh2)
    implementation(imglib2.imglib2)
    implementation(imglib2.imglib2.algorithm)
    implementation(imglib2.imglib2.algorithm.fft)
    implementation(imglib2.imglib2.roi)
    implementation(scijava.parsington)
    implementation("org.scijava:scijava-ops-api")
    implementation("org.scijava:scijava-ops-spi")
    implementation("org.scijava:scijava-ops-engine")
    implementation("org.scijava:scijava-function")
    implementation("org.scijava:scijava-types")
    implementation(misc.commons.math3)
    implementation(misc.joml)
    implementation(misc.ojalgo)
    implementation(misc.jama)

    testImplementation(junit5.junit.jupiter.api)
    testImplementation(junit5.junit.jupiter.engine)
    testImplementation("org.scijava:scijava-testutil")
    testImplementation(projects.imagej.imagejTestutil)
    testImplementation(scifio.scifio)
    testImplementation("org.scijava:scijava-threads")
    testImplementation(scijava.scripting.jython)
}