plugins {
    `java-library`
    id("org.gradlex.extra-java-module-info") version "1.2"
}

dependencies {
    implementation(projects.imagejOps2)
    implementation(projects.imagejMesh2)
    implementation("org.scijava:parsington:3.1.0")
    implementation("org.scijava:scijava-collections")
    implementation("org.scijava:scijava-common3")
    implementation("org.scijava:scijava-function")
    implementation("org.scijava:scijava-discovery")
    implementation("org.scijava:scijava-log2")
    implementation("org.scijava:scijava-meta")
    implementation("org.scijava:scijava-ops-api")
    implementation("org.scijava:scijava-ops-spi")
    implementation("org.scijava:scijava-ops-engine")
    implementation("org.scijava:scijava-priority")
    implementation("org.scijava:scijava-progress")
    implementation("org.scijava:scijava-types")
    implementation("org.scijava:scijava-struct")
    implementation("io.scif:scifio:0.45.0")
    implementation("net.imglib2:imglib2-algorithm:0.13.2")
    implementation("net.imglib2:imglib2-algorithm-fft:0.2.1")
    implementation("org.joml:joml:1.10.5")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("com.google.guava:guava:31.1-jre")
}

extraJavaModuleInfo {
    automaticModule("org.apache.commons:commons-math3", "commons.math3")
    automaticModule("com.googlecode.efficient-java-matrix-library:ejml", "ejml")
    automaticModule("io.scif:scifio-jai-imageio", "scifio.jai.imageio")
    automaticModule("gov.nist.math:jama", "jama")
    automaticModule("net.sf.trove4j:trove4j", "trove4j")
    automaticModule("org.ojalgo:ojalgo", "ojalgo")
    automaticModule("edu.mines:mines-jtk", "mines.jtk")
    automaticModule("edu.ucar:udunits", "udunits")
    automaticModule("org.bushe:eventbus", "eventbus")
    module("com.google.guava:failureaccess", "failureaccess", "1.0.1") { exportAllPackages() }
    module("com.google.guava:listenablefuture", "listenablefuture", "9999.0-empty-to-avoid-conflict-with-guava") { exportAllPackages() }
    automaticModule("com.google.code.findbugs:jsr305", "jsr305")
    automaticModule("com.google.j2objc:j2objc-annotations", "j2objc.annotations")
}