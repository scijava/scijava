# ImageJ Ops 2: The next generation of image processing algorithms

This library provides a suite of algorithms to aid in image processing and is the successor to [ImageJ Ops](https://github.com/imagej/imagej-ops). For Maven projects, this library can be used by adding to your `pom.xml`:

```java
<dependency>
	<groupId>net.imagej</groupId>
	<artifactId>imagej-ops2</artifactId>
	<version>0-SNAPSHOT</version>
	<scope>test</scope>
</dependency>
```

As all algorithms in this library are Ops (check out SciJava Ops SPI for more on what this means), they are stateless and can be used statically. Static use, however, is discouraged with preference given to the use of SciJava Ops Engine (check out SciJava Ops Engine for the reasons why). All ImageJ Ops2 algorithms are usable out of the box with SciJava Ops Engine, making it easier to find the right algorithm for any application.