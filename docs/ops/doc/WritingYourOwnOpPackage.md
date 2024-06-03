# Writing Your Own Op Package

Right now, there are two ways to write and expose your own Ops.

## Ops Through Javadoc

The recommended way to declare your Ops is through Javadoc. This approach requires no additional runtime dependencies&mdash;only a correctly formatted `@implNote` tag in the javadoc block of each routine you wish to make available as an Op, plus the scijava-ops-indexer annotation processor component present on your annotation processor path at compile time.

### Adding the SciJava Ops Indexer to your POM

Ops written through Javadoc are discovered by the SciJava Ops Indexer, which creates a file `ops.yaml` containing all of the data needed to import each Op you declare.

Until the SciJava Ops annotation processor is integrated into [pom-scijava](https://github.com/scijava/pom-scijava), developers must add the following block of code to the `build` section of their project POM:

TODO: Replace with the pom-scijava version needed to grab this annotation processor.
TODO: Replace the SciJava Ops Indexer version with the correct initial version
```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.scijava</groupId>
                        <artifactId>scijava-ops-indexer</artifactId>
                        <version>1.0.0</version>
                    </path>
                </annotationProcessorPaths>
                <fork>true</fork>
                <showWarnings>true</showWarnings>
                <compilerArgs>
                    <arg>-Ascijava.ops.parse=true</arg>
                    <arg>-Ascijava.ops.opVersion="${project.version}"</arg>
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
 * @implNote op names='<names>' [priority='<priority>']
 */
```

The arguments to the `@implNote op` syntax are described below:
* `names='<names>'` provides the names that the Op will match. If you'd like this Op to be searchable under one name `foo.bar`, you can use the argument `names='foo.bar'`. If you'd like your Op to be searchable using multiple names, you can use a comma-delimited list. For example, if you want your Op to be searchable under the names `foo.bar` and `foo.baz`, then you can use the argument `names='foo.bar,foo.baz'`, you can use the argument `names='foo.bar'`. If you'd like your Op to be searchable using multiple names, you can use a comma-delimited list. For example, if you want your Op to be searchable under the names `foo.bar` and `foo.baz`, then you can use the argument `names='foo.bar,foo.baz'`.
* `priority='<priority>'` provides a decimal-valued priority used to break ties when multiple Ops match a given Op request. *We advise against adding priorities unless you experience matching conflicts*. Op priorities should follow the SciJava Priority standards [insert link].

### Declaring Ops as Methods

Any `static` method can be easily declared as an Op by simply appending the `@implNote` tag to the method's Javadoc:

```java
/**
 * My static method, which is also an Op
 * @implNote op names='my.op'
 * @param arg1 the first argument to the method
 * @param arg2 the first argument to the method
 * @return the result of the method
 */
public static Double myStaticMethodOp(Double arg1, Double arg2) {
    ...computation here...
}
```
Additional Op characteristics are specified by placing parentheticals **at the end** of `@param` tags:
* If an Op input is allowed to be `null`, you can add `(nullable)` to the end. This tells SciJava Ops that your Op will function with our without that parameter.
* If an Op is written as a computer, you must add `(container)` to the end of the `@param` tag corresponding to the preallocated output buffer parameter.
* If an Op is written as an inplace, you must add `(mutable)` to the end of the `@param` tag corresponding to the mutable input parameter.

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
     * @input arg1 the first {@link Double}
     * @input arg2 the second {@link Double}
     * @output arg2 the second {@link Double}
     * @implNote op names='my.op'
	 */
    public final BiFunction<Double, Double, Double> myFieldOp =
        (arg1, arg2) -> {...computation...};
	
}
```
To describe each Op parameter, add the following tags to its javadoc:

* To describe a pure input, add the Javadoc tag `@input <parameter_name> <description>`
* To describe a pure output (for a function Op), add the Javadoc tag `@output <description>`
* To describe a conatiner (for a computer Op), add the Javadoc tag `@container <parameter_name> <description>`
* To describe a mutable input (for an inplace Op), add the Javadoc tag `@mutable <parameter_name> <description>`

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

The other way to expose Ops is by using the [Java Platform Module System](https://www.oracle.com/corporate/features/understanding-java-9-modules.html). This mechanism is used to expose the Ops declared within SciJava Ops Engine, and may be preferred for its usage of plain Java mechanisms and strong type safety:

In contrast to the Javadoc mechanism, all projects wishing to declare Ops using JPMS must add a dependency on the SciJava Ops SPI library:

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

## Best Practices

SciJava Ops is designed for modularity, extensibility, and granularity - you can exploit these aspects by adhering to the following guidelines when writing Ops:

### Using Dependencies

If you are writing an Op that performs many intermediate operations, there's a good chance someone else has written (and even optimized) some or all of them.

If not there's a great chance that others would benefit from you writing them separately so that they can reuse your work!

To accomodate this separation, Ops can reuse other Ops by declaring Op dependencies. When Ops returns an instance of your Op, it will also find and instantiate all of your dependencies!

To illustrate how this might work, here's a trivial example of an Op that computes the mean of a `double[]`:

```java

import org.scijava.ops.spi.OpDependency;

/**
 * A simple mean calculator
 *
 * @implNote op names="stats.mean"
 */
class DoubleMeanOp implements Function<double[], Double> {

    @OpDependency(name="stats.sum")
    public Function<double[], Double> sumOp;

    @OpDependency(name="stats.size")
    public Function<double[], Double> sizeOp;
		
    public Double apply(final double[] inArray) {
        final Double sum = sumOp.apply(inArray);
        final Double size = sizeOp.apply(inArray);
        return sum / size;
    }
}
```
In this example, the two `OpDependency` Ops already exist within the SciJava Ops Engine module - but if they didn't, you'd  define  them just like any other Op:

```java
/**
 * A simple summer
 *
 * @implNote op names="stats.sum"
 */
class DoubleSumOp implements Function<double[], Double> {
	public Double apply(final double[] inArray) {
		double sum = 0;
		for (double v : inArray) {
			sum += v;
		}
		return i;
	}
}

/**
 * A simple size calculator
 *
 * @implNote op names="stats.size"
 */
class DoubleSizeOp implements Function<double[], Double> {
	public Double apply(final double[] inArray) {
		return inArray.length;
	}
}

```

### Defining Op Progress

The `scijava-progress` module provides a mechanism for long-running tasks to describe their progress through textual or graphical means. For example, `scijava-progress` enables [Fiji](https://fiji.sc/) users to observe the progress of every Op invoked within the application, as shown below.

<center>
    <figure>
      <img src="https://media.scijava.org/scijava-ops/1.0.0/scijava_progress_example.png" alt="scijava-progress updates within Fiji" style="width:50%;"/>
      <figcaption><em>scijava-progress provides updates from all Op executions within Fiji's `Tasks` pane. </em></figcaption>
    </figure>
</center>

While all Ops emit "binary" progress (denoting each Op's beginning and end), your Op can provide richer updates by adding the `scijava-progress` module, providing user value for long-running Ops. To add progress to your Op, you must add the following steps to your Op:

* Before any significant computation, add the line `Progress.defineTotal(long elements)` where `elements` is the number of "discrete packets" of computation.
* At convenient spots within your Op, call `Progress.update()` to denote that one packet of computation has finished. 
  * **Alternatively**, it may be more convenient or performant to call `Progress.update(long numElements)` to denote `numElements` packets have completed at once.

```java
import java.util.function.Function;
import org.scijava.progress.Progress;

/**
 * A simple summer
 *
 * @implNote op names="stats.sum"
 */
class DoubleSumOp implements Function<double[], Double> {
    public Double apply(final double[] inArray) {
        // define total progress size
        Progress.defineTotal(inArray.length);
        double sum = 0;
        for (double v : inArray) {
            sum += v;
            // increment progress
            Progress.update();
        }
        return i;
    }
}
```

If your want to include the progress of Op dependencies within your Op's total progress, you can make the following changes.
* For each Op dependency that you want to track, pass the Hint `"progress.TRACK"` within the `@OpDependency` annotation. Note that it is **not** necessary for each Op to explicitly define its progress, but if it does so your Op will provide richer progress updates!
* Replace `Progress.defineTotal(long elements)` with `Progress.defineTotal(long elements, long subTasks)`, where `subTasks` is the **total** number of times you will invoke Op dependencies annotated with `"progress.TRACK"`.


```java
import java.util.function.Function;
import org.scijava.progress.Progress;
import org.scijava.ops.spi.OpDependency;

/**
 * A simple mean calculator
 *
 * @implNote op names="stats.mean"
 */
class DoubleMeanOp implements Function<double[], Double> {

    // This Op will contribute to progress
    @OpDependency(name="stats.sum", hints={"progress.TRACK"})
    public Function<double[], Double> sumOp;
    
    // This Op will also contribute to progress
    @OpDependency(name="stats.size", hints={"progress.TRACK"})
    public Function<double[], Double> sizeOp;

    public Double apply(final double[] inArray) {
        // There's no significant work here, but we do have 2 subtasks.
        Progress.defineTotal(0, 2);
        final Double sum = sumOp.apply(inArray);
        final Double size = sizeOp.apply(inArray);
        return sum / size;
    }
}
```

For best results, ensure your Op records Progress updates at a reasonable frequency. If too frequent, progress updates can detract from algorithm performance, and if too infrequent, they will be of little help to the user!

### Element-wise Ops

Simple pixel-wise operations like addition, inversion, and more can be written on a single pixel (i.e. `RealType`) - therefore, SciJava Ops Image takes care to automagically adapt pixel-wise Ops across a wide variety of image types. If you would like to write a pixel-wise Op, we recommend the following structure.

```java
/**
 * A simple pixelwise Op
 * 
 * @implNote op names="pixel.op"
 */
class MyPixelwiseOp<T extends RealType<T>> implements Computers.Arity2<T, T, T> {
	
	@Override
    public void compute(final T in1, final T in2, final T out) {
        --- pixelwise computation here ---
    }
}
```

The following Op call will match your pixel-wise Op using SciJava Ops's lifting mechanisms:

```java
ArrayImg<UnsignedByteType> in1 = ...
ArrayImg<UnsignedByteType> in2 = ...
ArrayImg<UnsignedByteType> out = ...

ops.op("pixel.op").input(in1, in2).output(out).compute();
```

A similar vein of thought works for simple `List`s and `Array`s - if you have an Op that produces a `Double` from another `Double`, there's no need to write a wrapper to work on `Double[]`s - Ops will do that for you!

```java
/**
 * An element-wise Op
 *
 * @implNote op names="element.op"
 */
class MyElementOp implements Function<Double, Double> {
    @Override
    public Double apply(final Double input) {
        --- pixelwise computation here ---
    }
}
```

If you then have SciJava Ops, the following Op call will match on your arrays, `List`s, `Set`s, and more - an example is shown below:

```java
List<Double> inList = ...
List<Double> outList = ops.op("element.op").input(in1).apply();
```

### Neighborhood-wise Ops

A slightly more complicated class of algorithms operate on local regions around each input pixel. For this class of algorithms, we recommend writing Ops on an imglib2-algorithm `net.imglib2.algorithm.neighborhood.Neighborhood` object.

```java

/**
 * A simple neighborhood-based Op
 *
 * @implNote op names="neighborhood.op"
 */
class MyNeighborhoodOp<T extends RealType<T>> implements Computers.Arity1<Neighborhood<T>, T> {
	@Override
	public void compute(final Neighborhood<T> input, final T output) {
        ... pixelwise computation here ...
	}
}
```

SciJava Ops will then lift this algorithm to operate on images in parallel, requiring users to only define the shape of the neighborhoods to be used:

```java
ArrayImg<DoubleType> input = ...
Shape neighborhoodShape = ...
ArrayImg<DoubleType> output = ...

ops.op("neighborhood.op").input(input, shape).output(output).compute()
```
