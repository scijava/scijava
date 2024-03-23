# SciJava Ops Benchmarks: A set of benchmarks for the Scijava Ops framework

This module contains benchmark code used to assess the performance of the SciJava Ops framework.

# Executing the Benchmarks

The following lines can be used to build and execute the benchmarks from the base `scijava-ops-benchmarks` directory on the command line:

```bash
# Build the benchmarks module
cd ..
mvn clean install -pl scijava-ops-benchmarks -am

# Copy dependencies into target folder
cd scijava-ops-benchmarks

# Execute the benchmarks
mvn dependency:copy-dependencies
java -cp "target/scijava-ops-benchmarks-0-SNAPSHOT.jar:target/dependency/*" org.openjdk.jmh.Main
```

# Adding a new benchmark

The best way to create a new benchmark is to create a new Java class within `src/main/java/org/scijava/benchmarks`. Within this new class, you can add new methods (annotated with `@Benchmark`) which will be automatically invoked when benchmarks are executed using the commands above.
