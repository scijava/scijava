# SciJava Ops External Parser

This module provides a utility that can write Op YAML based on external
configuration (itself a YAML). This allows inclusion of static methods from
external libraries into the Ops framework without modifying the code of those
libraries.

## Configuring the utility

The {@code OpParser} is a Java-based program that runs a `main` method
expecting a path to the YAML configuration for your library. It will also need
to run in an environment with the target libraries on the classpath (e.g. as
Maven dependencies).

One simple way to do this is to modify this component's `pom.xml` to add these
libraries as dependencies with `runtime` scope. See current example with
`org.bytedec:opencv` libraries.

The `OpParser` class can either be run from the containing `jar` [on the
command line](https://www.baeldung.com/java-run-jar-with-arguments), or from an
IDE such as
[IntelliJ](https://www.jetbrains.com/help/idea/running-applications.html).

## YAML format

The input YAML should be formatted as follows:

```yaml
namespace: "ns" # Optional. If present, your ops will all have "ns.MethodName" aliases.
version: "x" # Optional. If present, all ops will include this version metadata.

# Optional list of authors to apply to all Ops
authors:
  - "author 1"
  - "author 2"
  - ...

# Optional. If "containers" is present, the value is a list of fully qualified
# class names. Op methods that contain 2 or more of parameters with these types
# will be marked as Computers, with the second occurrance being the "output"
# param.
containers:
  - "container.1.name"
  - "container.2.name"
  - ...

# You can include as many base classes as you wish. Each fully-qualified class
# name should map to a map of method names to aliases. All methods in the base
# class with that name will be indexed under the given alias. The alias should
# be "SciJava" style - e.g. if your method performs a Gaussian filter, alias it
# "filter.gauss"
fully.qualified.className:
  method1Name: "method1.alias"
  method2Name: "method2.alias"
  ...
...
```


