# SciJava Ops Compat: A backwards compatibility bridge with older SciJava code

This library provides a bridge between SciJava Ops
and previous generations (Java 8 / pre-JPMS) of SciJava code.

It integrates SciJava Ops into SciJava Common's application context
and module system, via an OpEnvironmentService and preprocessor plugin.

For Maven projects, this library can be used by adding to your `pom.xml`:

```xml
<dependency>
	<groupId>org.scijava</groupId>
	<artifactId>scijava-ops-compat</artifactId>
	<version>1.0.0</version>
</dependency>
```
