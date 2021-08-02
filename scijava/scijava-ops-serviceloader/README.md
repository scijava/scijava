# SciJava Ops Service Loader: A [`ServiceLoader`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/ServiceLoader.html)
This module provides an implementation of `Discoverer` that is powered by the `ServiceLoader` from Java's Module System.

## What can `ServiceLoaderDiscoverer` discover?

`ServiceLoaderDiscoverer` can discover implementations of *any* interface declared to be `use`d in its `module-info.java`. Given a 