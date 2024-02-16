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
                # (in addition to the per-method manually assigned aliases)
version: "0" # Optional. If present, all ops will include this version metadata.

# Optional single or list of authors to apply to all Ops
authors:
  - "author 1"
  - "author 2"
  - ...

# The remaining entries define the actual Ops to include. Ops are specified by
# fully-qualified class name. You can include as many base classes as you wish.
# Each class name should map to a map of method names to metadata for Ops of
# that method. You may include as many methods as desired from the base class.
# All methods with that name in the base class will be processed as Ops.
fully.qualified.className:
  methodName:
    # The alias should be "SciJava" style - e.g. if your method performs a
    # Gaussian filter, alias it "filter.gauss"
    alias: "method.alias"
    # Same as the global authors key. Method-specific authors supersede
    # (replace) the global entry for these Ops.
    authors: "author name"
    # The type specification is essential for non-Function methods. If your
    # methods include a pre-allocated buffer or an item that is modified as part
    # of execution, their type should be 'ComputerN' or 'InplaceN' respectively,
    # where 'N' is the one-based index of the parameter being modified.
    type: "Type"
    # Optional priority to apply to all Ops for this method, used during
    # Op matching in the case multiple Ops satisfy the parameter and name
    # for a given request.
    priority: "0.0"
    # Optional description to apply to all Ops for this method. This will appear
    # in help text queries for the Op.
    description: "method description"
  ...
...
```


