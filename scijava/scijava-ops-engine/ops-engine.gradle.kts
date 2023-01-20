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
    automaticModule("org.javassist:javassist:3.28.0-GA", "javassist")
//    module("com.github.therapi:therapi-runtime-javadoc", "therapi.runtime.javadoc", "0.13.0") {
//        exportAllPackages()
//    }
}