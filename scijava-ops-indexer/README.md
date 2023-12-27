# SciJava Ops Indexer: Ops declaration within Javadoc

This module provides an annotation processor that can write Op YAML, based on Op descriptions written purely in Javadoc!

## Configuring the annotation processor

To enable Op YAML generation in your build process, add the following to your `pom.xml`:

```xml
<properties>
    <scijava.parse.ops>true</scijava.parse.ops>
</properties>
```

**NOTE**: Until this repository is moved out of the SciJava Incubator and the following is added to pom-scijava, the following must also be added to your POM:

```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.scijava</groupId>
                        <artifactId>scijava-ops-indexer</artifactId>
                        <version>${project.version}</version>
                    </path>
                </annotationProcessorPaths>
                <fork>true</fork>
                <showWarnings>true</showWarnings>
                <compilerArgs>
                    <arg>-Aparse.ops="${scijava.parse.ops}"</arg>
                    <arg>-Aop.version="${project.version}"</arg>
                </compilerArgs>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## Declaring Ops Within Javadoc

To add a tag to any [`AnnotatedElement`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/reflect/AnnotatedElement.html) as an Op, one can simply insert the following tag into its Javadoc:

```java
@implNote op names='<comma-separated list of names>'
```

For example, if you insert the `@implNote op` tag into the following static method

```java

/**
 * Some example Op
 *
 * @param d1 the first {@link Double}
 * @param d2 the second {@link Double}
 * @return the sum
 * @author Gabriel Selzer
 * @implNote op names='math.add' 
 */
public static Double add(final Double d1, final Double d2) {
  return d1 + d2;
}
```

This annotation processor might create the following file `op.yaml` within the JAR build by Maven:

```yaml
- op:
    names: [math.add]
    description: |2
       Some example Op
    source: javaMethod:/com.example.foo.Bar.add%28java.lang.Double%2Cjava.lang.Double%29
    priority: 0.0
    version: 1.2.3
    parameters:
      - parameter type: INPUT
        name: d1
        description: |
          the first {@link Double}
        type: java.lang.Double
      - parameter type: INPUT
        name: d2
        description: |
          the second {@link Double}
        type: java.lang.Double
      - parameter type: OUTPUT
        name: output
        description: |
          the sum
        type: java.lang.Double
    authors:
      - |
        Gabriel Selzer
    tags: {}
```

This YAML file can then be ingested by the SciJava Ops Engine (add link), and your Ops will automatically become available in any environment including your JAR!

