# SciJava Discovery Therapi: A Discoverer implementation backed by Therapi

This module provides the `TherapiDiscover`, a `Discoverer` implementation that uses [`therapi-runtime-javadoc`](https://github.com/dnault/therapi-runtime-javadoc) to discover tagged elements through javadoc tags.

`TherapiDiscoverer` **only** implements `Discoverer.elementsTaggedWith(String tag)`.

To make tags discoverable via `TherapiDiscoverer`, one must first enable therapi's annotation processor.

```java
<properties>
	<therapi.version>0.12.0</therapi.version>
	<therapi-runtime-javadoc-scribe.version>${therapi.version}</therapi-runtime-javadoc-scribe.version>
	<therapi.packages></therapi.packages>
</properties>
```

This sets the therapi version, and denotes the packages (using the `<therapi.packages>` tag) that should be processed. This can be left blank to indicate all packages, or can be a comma-delimited list to process **only** those packages.

```java
<build>
	<plugins>
		<plugin>
			<artifactId>maven-compiler-plugin</artifactId>
			<configuration>
				<annotationProcessorPaths>
					<path>
						<groupId>com.github.therapi</groupId>
						<artifactId>therapi-runtime-javadoc-scribe</artifactId>
						<version>${therapi-runtime-javadoc-scribe.version}</version>
					</path>
				</annotationProcessorPaths>
				<fork>true</fork>
				<compilerArgs>
					<arg>-Ajavadoc.packages="${therapi.packages}"</arg>
				</compilerArgs>
			</configuration>
		</plugin>
	</plugins>
</build>
```

These elements already live in `scijava-incubator`, and will be moved upstream to `pom-scijava` at a later date. This means that all incubator projects (and later all SciJava projects) will have therapi capabilities for free. This behavior is **opt-in**; to enable therapi's annotation processor (and thus any functionality from `TherapiDiscoverer`) one must add `<therapi.packages></therapi.packages>` to the `properties` section of their POM.

## Tag Structure

To add a tag to any [`AnnotatedElement`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/reflect/AnnotatedElement.html), one can simply insert the [`@implNote`](https://nipafx.dev/javadoc-tags-apiNote-implSpec-implNote/) tag into the javadoc of that `AnnotatedElement`. Tags should be structured as

```java
@implNote <tagType> <tagBody>
```

Where `tagType` is the `String` under which this `AnnotatedElement` should be discovered, and `tagBody` is any set of options relevant for the `tagType`. **TODO: `tagBody` structure**

An example might look like

```java

/**
 * @implNote foo 
 */
public void taggedMethod(...) {
  ...
}
```

Assuming therapi processes the package containing `taggedMethod`, `taggedMethod` can then be retrieved using `TherapiDiscoverer.elementsTaggedWith("foo")`.

