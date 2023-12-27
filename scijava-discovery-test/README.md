# SciJava Ops Service Loader: A [`ServiceLoader`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/ServiceLoader.html) extension of SciJava Discovery
This module provides an implementation of `Discoverer` that is powered by the `ServiceLoader` from Java's Module System.

## What can `ServiceLoaderDiscoverer` discover?

`ServiceLoaderDiscoverer` can discover implementations of *any* interface declared to be `use`d in its `module-info.java`. Although we limit the `use`d interfaces to those defined in SciJava Ops SPI (this is, after all, an Ops project), the process is the same for any other interface.

# I wrote an implementation, how can I make it discoverable via `ServiceLoaderDiscoverer`?

Suppose your module `com.example.foo` contains an implementation `com.example.foo.FooOp` of `org.scijava.ops.spi.Op`, and you wise to make `FooOp` discoverable from `ServiceLoaderDiscoverer`. Make sure to:

* Declare SciJava Ops Service Loader as a dependency; if using Maven, it looks like this:

```java
<dependency>
	<groupId>org.scijava</groupId>
	<artifactId>scijava-ops-serviceLoader</artifactId>
	<version>0-SNAPSHOT</version>
</dependency
```
* Make sure that the interface being implemented is `use`d in SciJava Ops Service Loader's `module-info.java`
* Declare in your project's `module-info.java`:

```java
provides org.scijava.ops.spi.Op with com.example.foo.FooOp;
```