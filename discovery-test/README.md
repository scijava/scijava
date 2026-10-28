# SciJava Discovery Test: Integration tests for SciJava Discovery

This module exercises `Discoverer` against Java's [`ServiceLoader`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/ServiceLoader.html) across real module boundaries. It is not intended to be used as a dependency.

Its `module-info.java` `provides` sample implementations of `org.scijava.ops.spi.Op` and `org.scijava.ops.spi.OpCollection`, and its tests check that a `Discoverer` built with `Discoverer.usingProviders(c -> ServiceLoader.load(c).stream())` finds them.

Note that `ServiceLoader` can only discover implementations of interfaces that the calling module declares it `uses`. This module therefore declares:

```java
uses org.scijava.ops.spi.Op;
uses org.scijava.ops.spi.OpCollection;
```
