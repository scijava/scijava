plugins {
    `java-library`
}

val generateScijava by tasks.registering(GenerateScijava::class)
sourceSets {
    main { java.srcDir(generateScijava) }
}