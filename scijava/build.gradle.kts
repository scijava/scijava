plugins {
    `java-library`
}

allprojects {
    group = "org.scijava"
    version = "0-SNAPSHOT"
}

dependencies {
    for (subproject in subprojects)
        implementation(subproject)
}

tasks.test {
    dependsOn(
        projects.scijavaCommon3.dependencyProject.tasks.test,
        projects.scijavaDiscoveryTest.dependencyProject.tasks.test,
//        projects.scijavaDiscoveryTherapi.dependencyProject.tasks.test,
        projects.scijavaLog2.dependencyProject.tasks.test,
//        projects.scijavaOpsEngine.dependencyProject.tasks.test,
        projects.scijavaParse2.dependencyProject.tasks.test,
//        projects.scijavaPersist.dependencyProject.tasks.test,
        projects.scijavaProgress.dependencyProject.tasks.test,
        projects.scijavaThreads.dependencyProject.tasks.test,
        projects.scijavaTypes.dependencyProject.tasks.test,
             )
}