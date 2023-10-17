plugins {
    `java-library`
    id("org.gradlex.extra-java-module-info") version "1.2"
}

dependencies {

    implementation(projects.scijavaCollections)
    implementation(projects.scijavaCommon3)
    implementation(projects.scijavaMeta)
    implementation(projects.scijavaOpsApi)
    implementation(projects.scijavaOpsSpi)
    implementation(projects.scijavaDiscovery)
    //    implementation(projects.scijavaDiscoveryTherapi)
    implementation(projects.scijavaLog2)
    implementation(projects.scijavaPriority)
    implementation(projects.scijavaProgress)
    implementation(projects.scijavaStruct)
    implementation(projects.scijavaTypes)
    implementation(projects.scijavaFunction)
    implementation("org.javassist:javassist:3.29.2-GA")
    implementation("org.scijava:parsington:3.1.0")
    // missing
    implementation("org.yaml:snakeyaml:1.33")
    //    annotationProcessor(libs.therapi.processor)
    //    implementation(libs.therapi)
    implementation("com.google.guava:guava:31.1-jre")
    implementation(projects.scijavaParse2)

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")
}

val generateScijavaMain by tasks.registering(GenerateScijava::class)
val generateScijavaTest by tasks.registering(GenerateScijava::class) { isMain = false }
val generateCode by tasks.registering { dependsOn(generateScijavaMain, generateScijavaTest) }
sourceSets {
    main { java.srcDir(generateScijavaMain) }
    test { java.srcDir(generateScijavaTest) }
}

extraJavaModuleInfo {
    module("org.javassist:javassist", "org.javassist", "3.28.0-GA") { exportAllPackages() }
    automaticModule("com.github.therapi:therapi-runtime-javadoc-scribe", "therapi.runtime.javadoc.scribe")
    automaticModule("com.github.therapi:therapi-runtime-javadoc", "therapi.runtime.javadoc")
    automaticModule("com.google.code.findbugs:jsr305", "jsr305")
    automaticModule("com.google.j2objc:j2objc-annotations", "j2objc.annotations")
    module("com.google.guava:failureaccess", "failureaccess", "1.0.1") { exportAllPackages() }
    module("com.google.guava:listenablefuture", "listenablefuture", "9999.0-empty-to-avoid-conflict-with-guava") { exportAllPackages() }
}

tasks.test { useJUnitPlatform() }