plugins {
    `java-library`
}

val generateCode by tasks.registering(GenerateCode::class)
sourceSets {
    main { java.srcDir(generateCode) }
}