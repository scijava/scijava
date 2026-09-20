# SciJava Ops Image: The next generation of image processing algorithms

This library provides a suite of algorithms to aid in image processing using SciJava Ops. It is the successor to [ImageJ Ops](https://github.com/imagej/imagej-ops). For Maven projects, this library can be used by adding to your `pom.xml`:

```java
<dependency>
	<groupId>org.scijava</groupId>
	<artifactId>scijava-ops-image</artifactId>
	<version>1.0.0</version>
	<scope>test</scope>
</dependency>
```

As all algorithms in this library are Ops (check out SciJava Ops SPI for more on what this means), they are stateless and can be used statically. Static use, however, is discouraged with preference given to the use of SciJava Ops Engine (check out SciJava Ops Engine for the reasons why). All contained algorithms can be used out of the box with SciJava Ops Engine, making it easier to find the right algorithm for any application.
