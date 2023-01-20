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
    implementation(projects.scijavaDiscoveryTherapi)
    implementation(projects.scijavaLog2)
    implementation(projects.scijavaPriority)
    implementation(projects.scijavaProgress)
    implementation(projects.scijavaStruct)
    implementation(projects.scijavaTypes)
    implementation(projects.scijavaFunction)
    implementation(misc.javassist)
    // missing
    implementation(misc.snakeyaml)
    annotationProcessor(libs.therapi.processor)
    implementation(libs.therapi)
    implementation(libs.guava)
    implementation(projects.scijavaParse2)

    testImplementation(junit5.junit.jupiter.api)
    testImplementation(junit5.junit.jupiter.engine)
}

val generateCodeMain by tasks.registering(GenerateCode::class)
val generateCodeTest by tasks.registering(GenerateCode::class) { main = false }
val generateCode by tasks.registering { dependsOn(generateCodeMain, generateCodeTest) }
sourceSets {
    main { java.srcDir(generateCodeMain) }
    test { java.srcDir(generateCodeTest) }
}

extraJavaModuleInfo {
    module("org.javassist:javassist", "javassist", "3.28.0-GA") { exportAllPackages() }
    extraJavaModuleInfo {
        automaticModule("com.github.therapi:therapi-runtime-javadoc-scribe", "therapi.runtime.javadoc.scribe")
        automaticModule("com.github.therapi:therapi-runtime-javadoc", "therapi.runtime.javadoc")
        automaticModule("com.google.code.findbugs:jsr305", "jsr305")
        automaticModule("com.google.j2objc:j2objc-annotations", "j2objc.annotations")
    }
    module("com.google.guava:failureaccess", "failureaccess", "1.0.1") { exportAllPackages() }
    module("com.google.guava:listenablefuture", "listenablefuture", "9999.0-empty-to-avoid-conflict-with-guava") { exportAllPackages() }
}

tasks.test { useJUnitPlatform() }