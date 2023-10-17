plugins {
    `java-library`
    id("org.gradlex.extra-java-module-info") version "1.2"
}

val generateImagej by tasks.registering(GenerateImagej::class)
sourceSets.main { java.srcDir(generateImagej) }

dependencies {

    implementation(projects.imagej.imagejMesh2)
    implementation("net.imglib2:imglib2:6.1.0")
    implementation("net.imglib2:imglib2-algorithm:0.13.2")
    implementation("net.imglib2:imglib2-algorithm-fft:0.2.1")
    implementation("net.imglib2:imglib2-roi:0.14.1")
//    implementation("org.scijava.parsington:3.1.0")
    implementation("org.scijava:scijava-collections")
    implementation("org.scijava:scijava-common3")
    implementation("org.scijava:scijava-concurrent")
    implementation("org.scijava:scijava-discovery")
    implementation("org.scijava:scijava-function")
    implementation("org.scijava:scijava-log2")
    implementation("org.scijava:scijava-meta")
    implementation("org.scijava:scijava-priority")
    implementation("org.scijava:scijava-ops-api")
    implementation("org.scijava:scijava-ops-spi")
    implementation("org.scijava:scijava-ops-engine")
    implementation("org.scijava:scijava-struct")
    implementation("org.scijava:scijava-types")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("org.joml:joml:1.10.5")
    implementation("org.ojalgo:ojalgo:45.1.1")
    implementation("gov.nist.math:jama:1.0.3")
    implementation("org.ojalgo:ojalgo:45.1.1")
    implementation("edu.mines:mines-jtk:20151125")
    implementation("net.sf.trove4j:trove4j:3.0.3")
    implementation("com.googlecode.efficient-java-matrix-library:ejml:0.25")
    implementation("com.google.guava:guava:31.1-jre")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testImplementation("org.scijava:scijava-testutil")
    testImplementation(projects.imagej.imagejTestutil)
    testImplementation("io.scif:scifio:0.45.0")
    testImplementation("org.scijava:scijava-threads")
//    testImplementation(scijava.scripting.jython)
}

extraJavaModuleInfo {
    automaticModule("org.apache.commons:commons-math3", "commons.math3")
    automaticModule("org.ojalgo:ojalgo", "ojalgo")
    automaticModule("gov.nist.math:jama", "jama")
    automaticModule("edu.mines:mines-jtk", "mines.jtk")
    automaticModule("net.sf.trove4j:trove4j", "trove4j")
    automaticModule("com.googlecode.efficient-java-matrix-library:ejml", "ejml")
    module("com.google.guava:failureaccess", "failureaccess", "1.0.1") { exportAllPackages() }
    module("com.google.guava:listenablefuture", "listenablefuture", "9999.0-empty-to-avoid-conflict-with-guava") { exportAllPackages() }
    automaticModule("com.google.code.findbugs:jsr305", "jsr305")
    automaticModule("com.google.j2objc:j2objc-annotations", "j2objc.annotations")
}