# Writing Your Own Op Package

Right now, there are two ways to write and expose your own Ops.

## Ops Through Javadoc

Ops can be declared directly through Javadoc - this yields a couple of benefits, namely:
* No additional runtime dependencies needed for simple Ops

This mechanism of Op declaration is used by the ImageJ Ops2 project

### Adding the SciJava Javadoc Parser to your POM

Ops written through Javadoc are discovered by the SciJava Javadoc Parser, which creates a file `op.yaml` containing all of the data needed to import each Op you declare.

Until the SciJava 3 annotation processor is added to pom-scijava, developers must add the following block of code to the `build` section of your POM:

TODO: Replace with the pom-scijava version needed to grab this annotation processor.
TODO: Replace the SciJava Javadoc Parser version with the correct initial version
```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.scijava</groupId>
                        <artifactId>scijava-javadoc-parser</artifactId>
                        <version>${project.version}</version>
                    </path>
                </annotationProcessorPaths>
                <fork>true</fork>
                <showWarnings>true</showWarnings>
                <compilerArgs>
                    <arg>-Ajavadoc.packages="-"</arg>
                </compilerArgs>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Declaring Ops with the `@implNote` syntax

To declare a block of code as an Op, simply add the `@implNote` tag to that block's Javadoc. The `@implNote` schema for declaring Ops is as follows:

```java
/**
 * @implNote op names='<names>' [priority='<priority>'] [type='<type>']
 */
```

The arguments to the `@implNote op` syntax are described below:
* `names='<names>'` provides the names that the Op will match. If you'd like this Op to be searchable under one name `foo.bar`, you can use the argument `names='foo.bar'`. If you'd like your Op to be searchable using multiple names, you can use a comma-delimited list. For example, if you want your Op to be searchable under the names `foo.bar` and `foo.baz`, then you can use the argument `names='foo.bar,foo.baz'`, you can use the argument `names='foo.bar'`. If you'd like your Op to be searchable using multiple names, you can use a comma-delimited list. For example, if you want your Op to be searchable under the names `foo.bar` and `foo.baz`, then you can use the argument `names='foo.bar,foo.baz'`.
* `priority='<priority>'` provides a decimal-valued priority used to break ties when multiple Ops match a given Op request. *We advise against adding priorities unless you experience matching conflicts*. Op priorities should follow the SciJava Priority standards [insert link].
* `type='<type>'` identifies the functional type of the Op **and is only required for Ops written as methods** - more information on that below [insert link].

### Declaring Ops as Methods

Any `static` method can be easily declared as an Op by simply appending the `@implNote` tag to the method's Javadoc:

```java
/**
 * My static method, which is also an Op
 * @implNote op names='my.op' type='java.util.function.BiFunction'
 * @param arg1 the first argument to the method
 * @param arg2 the first argument to the method
 * @return the result of the method
 */
public static Double myStaticMethodOp(Double arg1, Double arg2) {
    ...computation here...
}
```
Note that the `type` argument in the `@implNote` syntax is **required** for Ops written as methods (and only for Ops written as methods), as the Op must be registered to a functional type. The recommended functional types are housed in the SciJava Functions library [insert link].

### Declaring Ops as Classes

Any `Class` implementing a `FunctionalInterface` (such as `java.util.function.Function`, `java.util.function.BiFunction`, `org.scijava.computers.Computers.Arity1`, etc.) can be declared as an Op using the `@implNote` syntax within the Javadoc *of that class*, as shown in the example below:

```java
/**
 * My class, which is also an Op
 *
 * @implNote op names='my.op'
 */
public class MyClassOp
		implements java.util.function.BiFunction<Double, Double, Double>
{

	/**
     * The functional method of my Op
     * @param arg1 the first argument to the Op
     * @param arg2 the first argument to the Op
     * @return the result of the Op
	 */
	@Override
    public Double apply(Double arg1, Double arg2) {
		return null;
	}
}
```

Note that the only supported functional interfaces that can be used without additional dependencies are `java.util.function.Function` and `java.util.function.BiFunction` - if you'd like to write an Op requiring more than two inputs, or to write an Op that takes a pre-allocated output buffer, you'll need to depend on the SciJava Function library:

```xml
<dependencies>
    <dependency>
        <groupId>org.scijava</groupId>
        <artifactId>scijava-function</artifactId>
    </dependency>
</dependencies>
```

### Declaring Ops as Fields

Any `Field` whose type is a `FunctionalInterface` (such as `java.util.function.Function`, `java.util.function.BiFunction`, `org.scijava.computers.Computers.Arity1`, etc.) can also be declared as an Op. Function Ops are useful for very simple Ops, such as [Lambda Expressions](https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html) or [Method references](https://docs.oracle.com/javase/tutorial/java/javaOO/methodreferences.html). For `Field`s, the `@implNote` syntax should be placed on Javadoc on the Field, as shown below:

```java
public class MyOpCollection {

	/**
     * @implNote op names='my.op'
	 */
    public final BiFunction<Double, Double, Double> myFieldOp =
        (arg1, arg2) -> {...computation...};
	
}
```

Note again that the only supported functional interfaces that can be used without additional dependencies are `java.util.function.Function` and `java.util.function.BiFunction` - if you'd like to write an Op requiring more than two inputs, or to write an Op that takes a pre-allocated output buffer, you'll need to depend on the SciJava Function library:

```xml
<dependencies>
    <dependency>
        <groupId>org.scijava</groupId>
        <artifactId>scijava-function</artifactId>
    </dependency>
</dependencies>
```

## Ops using JPMS

The other way to expose Ops is by using the [Java Platform Module System](https://www.oracle.com/corporate/features/understanding-java-9-modules.html). This mechanism is used to expose the Ops declared within SciJava Ops Engine, and may be preferred for its usage of plain Java mechanisms:

In opposition to the Javadoc mechanism, all projects wishing to declare Ops using JPMS must add a dependency on the SciJava Ops SPI library:

```xml
<dependencies>
    <dependency>
        <groupId>org.scijava</groupId>
        <artifactId>scijava-ops-spi</artifactId>
    </dependency>
</dependencies>
```

### Declaring Ops as Classes

Using JPMS, Ops can be declared as Classes using the `OpClass` annotation and the `Op` interface, as shown below:

```java
@OpClass(names = "my.op")
public class MyClassOp implements BiFunction<Double, Double, Double>, Op {
	@Override
    public Double apply(Double arg1, Double arg2) {
      ...computation...
    }
}
```
Note the following:
* The `@OpClass` annotation provides the names of the Op
* The `BiFunction` interface determines the functional type of the Op
* The `Op` interface allows us to declare the Class as a service using JPMS

Below, we'll see how to expose the Op within the `module-info.java`.

### Declaring Ops as Fields and Methods

Using JPMS, Ops can be declared as Fields using the `OpField` annotation, or as Methods using the `OpMethod` annotation, within any class that implements the `OpCollection` interface. An example is shown below:

```java
public class MyOpCollection implements OpCollection {
	@OpField(names="my.fieldOp")
    public final BiFunction<Double, Double, Double> myFieldOp =
        (arg1, arg2) -> {...computation...};
	
	@OpMethod(names="my.methodOp", type=BiFunction.class)
    public Double myMethodOp(final Double arg1, final Double arg2) {
      ...computation...
    }
}
```
Note the following:
* The `OpCollection` interface allows us to declare the Class as a service using JPMS
* The `@OpField` annotation declares the `Field` as an Op and also specifies the name(s) of the Op
* The `@OpMethod` annotation declares the `Method` as an Op and also specifies the name(s) of the Op. **In addition**, it specifies the functional interface of the resulting Op.

### Exposing the Ops to SciJava Ops using the `module-info.java`

The last step of declaring Ops using JPMS is to declare them in a `module-info.java`, located in the root package directory of your project (`src/main/java` for Maven projects).

For an example module `com.example.ops` declaring the above Ops, we use the [`provides...with`] syntax to declare our `Op`s and our `OpCollection`s:

```java
module com.example.ops {
	provides org.scijava.ops.spi.Op with com.example.ops.MyClassOp;
	
	provides org.scijava.ops.spi.OpCollection with com.example.ops.MyOpCollection;
}
```

