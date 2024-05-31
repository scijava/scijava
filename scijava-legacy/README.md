# SciJava Legacy: A backwards compatibility bridge with older SciJava code

This library provides a bridge between current SciJava libraries
and previous generations (Java 8 / pre-JPMS) of SciJava/ImageJ2 code.

Features include:

* Integration of SciJava Ops into SciJava Common's application context
  and module system, via an OpEnvironmentService and preprocessor plugin.

* A SciJava Ops TypeExtractor for ImageJ2's Dataset image type.

For Maven projects, this library can be used by adding to your `pom.xml`:

```java
<dependency>
	<groupId>org.scijava</groupId>
	<artifactId>scijava-legacy</artifactId>
	<version>1.0.0</version>
</dependency>
```
