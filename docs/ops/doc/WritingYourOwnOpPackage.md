# Writing Your Own Op Package

This document will provide the fundamental concepts for writing your own Op package for the SciJava Ops framework.

## Before you start

We assume that you have successfully setup a Java development environment for SciJava Ops.

### Create a SciJava Ops development environment

While there are multiple ways to setup a development environment for SciJava Ops, we recommend creating your environment with `mamba`.

1. [Install Miniforge3](https://github.com/conda-forge/miniforge#miniforge3)
2. Create a development environment with the following `environment.yml` file:
    
    ```yaml
    name: scijava
    channels:
        - conda-forge
        - defaults
    dependencies:
        - maven
        - openjdk>11
    ```
   Create the above environment locally by running the following command:
   ```bash
   mamba env create -f environment.yml
   ```
3. Add the Scijava Ops Indexer to your POM:

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
                        <arg>-Aparse.ops=true</arg>
                        <arg>-Aop.version="${project.version}"</arg>
                    </compilerArgs>
                </configuration>
            </plugin>
        </plugins>
    </build>
    ```

    _Note: Ops written through Javadoc are disovered by the SciJava Ops Indexer, which creates a file `ops.yaml` containing all the necessary dataneeded to import each declared Op._

4. Activate your new environment with `mamba activate scijava` and build the SciJava Ops indexer:

    ```bash
    mvn clean install -pl scijava-ops-indexer -am
    ```

5. Build SciJava Ops and install it in Fiji:

    ```bash
    mvn clean install -Dscijava.app.directory=/path/to/Fiji.app/
    ```

    _Note: Its not necessary to pass `-Dscijava.app.directory` to maven, however doing so will install the newly built SciJava Ops `.jar` files in the given Fiji location._

### Ops Through Javadoc

The recommended way to declare your Ops is through Javadoc. This approach requires no additional runtime dependencies&mdash;only a correctly formatted `@implNote` tag in the javadoc block of each routine you wish to make available as an Op, plus the scijava-ops-indexer annotation processor component present on your annotation processor path at compile time.

#### Declaring Ops with the `@implNote` Javadoc syntax

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

#### Declaring Ops as Classes with Javadoc

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

#### Declaring Ops as Fields with Javadoc

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

#### Declaring Ops as Methods with Javadoc

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



Note that the only supported functional interfaces that can be used without additional dependencies are `java.util.function.Function` and `java.util.function.BiFunction` - if you'd like to write an Op requiring more than two inputs, or to write an Op that takes a pre-allocated output buffer, you'll need to depend on the SciJava Function library:

```xml
<dependencies>
    <dependency>
        <groupId>org.scijava</groupId>
        <artifactId>scijava-function</artifactId>
    </dependency>
</dependencies>
```

### Ops using JPMS

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
#### Declaring Ops as Classes with JPMS

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

#### Declaring Ops as Fields and Methods with JPMS

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

#### Exposing the Ops to SciJava Ops using the `module-info.java`

The last step of declaring Ops using JPMS is to declare them in a `module-info.java`, located in the root package directory of your project (`src/main/java` for Maven projects).

For an example module `com.example.ops` declaring the above Ops, we use the [`provides...with`] syntax to declare our `Op`s and our `OpCollection`s:

```java
module com.example.ops {
	provides org.scijava.ops.spi.Op with com.example.ops.MyClassOp;
	
	provides org.scijava.ops.spi.OpCollection with com.example.ops.MyOpCollection;
}
```
### Deciding what type of Op to write

After you have decided what kind of alogirthim you want to "Op-ify", the next step is deciding what type of Op to write:

    - **Computer**:
    - **Function**:
    - **Inpalce**:

Notably, the type of Op you choose to write determines what method to overload. For example a `Function` Op will overload the `apply()` method:

```java

/**
 * @param input
 * @param option
 * @return Op output
 */
@Override
public RandomAccessibleInterval<O> apply(RandomAccessibleInterval<I> input,
        Boolean option)
{
    // do something interesting
    return output;
}


```

## Op Tutorial

This tutorial will walk you through writing an example Op using the `@impNote` syntax with Javadoc. In this example
we use the `gaussianSub` Op on a 3D (X, Y, Z) immunoflouresence dataset of a pair of HeLa cells expressing the HIV-1<sub>NL4-3</sub> Vif protein.
The sample data was captured on a Nikon Ti-Eclipse inverted epiflouresence microscope with a 100x Plan Apo objective (NA = 1.45) with
0.1 μm step size between slices.

The example data is available to download from [here](https://media.scijava.org/scijava-ops/1.0.0/hela_hiv_vif.tif).

<center>

![Gaussian subtraction example gif](https://media.scijava.org/scijava-ops/1.0.0/gaussian_sub_example.gif)

</center>

Below is the completed Gaussian blur op, adapted from the [SciJava Ops Gaussian blur subtraction example](examples/gaussian_subtraction).

### 1 Writing the Op

Below is the complete Op. In the following sections we will break down this Op example
and explain what each component does.

```java
package org.scijava.ops.image.filter.gaussianSub;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

/**
 * @author Edward Evans
 * @param <I> input type
 * @implNote op names='filter.gaussianSub', priority='100.'
 */
public class GaussianSubtraction<I extends RealType<I>> implements
	BiFunction<RandomAccessibleInterval<I>, Double, RandomAccessibleInterval<DoubleType>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, DoubleType, RandomAccessibleInterval<DoubleType>> createOp;

	@OpDependency(name = "filter.gauss")
	private BiFunction<RandomAccessibleInterval<I>, Double, RandomAccessibleInterval<DoubleType>> gaussOp;

	@OpDependency(name = "math.sub")
	private Computers.Arity2<RandomAccessibleInterval<I>, RandomAccessibleInterval<DoubleType>, RandomAccessibleInterval<DoubleType>> subOp;

	/**
	 * Gaussian blur subtraction Op. This performs basic feature extraction by
	 * subtracting the input image with a Gaussian blurred copy, leaving the
	 * non-blurred structures.
	 *
	 * @param input input image
	 * @param sigma sigma value for gaussian blur
	 * @return guassian blur subtracted image
	 */
	@Override
	public RandomAccessibleInterval<DoubleType> apply(
		RandomAccessibleInterval<I> input, Double sigma)
	{
		RandomAccessibleInterval<DoubleType> blur = gaussOp.apply(input, sigma);
		RandomAccessibleInterval<DoubleType> output = createOp.apply(input,
			new DoubleType());
		subOp.compute(input, blur, output);
		return output;
	}
}
```

Create a new file in the `image/filter/gaussianSub/GaussianSubtraction.java` and copy the
example Op to it.

#### 1.1 Define the Op name and priority

In this section define the Op name and it's priority. If you're unsure about which priority to set
use `100.`.

```java
/**
 * @author Edward Evans
 * @param <I> input type
 * @implNote op names='filter.gaussianSub', priority='100.'
 */
```

#### 1.2 Using Op dependencies

```java
@OpDependency(name = "create.img")
private BiFunction<Dimensions, DoubleType, RandomAccessibleInterval<DoubleType>> createOp;

@OpDependency(name = "filter.gauss")
private BiFunction<RandomAccessibleInterval<I>, Double, RandomAccessibleInterval<DoubleType>> gaussOp;

@OpDependency(name = "math.sub")
private Computers.Arity2<RandomAccessibleInterval<I>, RandomAccessibleInterval<DoubleType>, RandomAccessibleInterval<DoubleType>> subOp;
```

#### 1.3 Add a Javadoc to the Op

```java
/**
 * Gaussian blur subtraction Op. This performs basic feature extraction by
 * subtracting the input image with a Gaussian blurred copy, leaving the
 * non-blurred structures.
 *
 * @param input input image
 * @param sigma sigma value for gaussian blur
 * @return guassian blur subtracted image
 */
```

Ops help output for gaussiansub Op

```bash
filter.gaussianSub:
	- org.scijava.ops.image.filter.gaussianSub.GaussianSubtraction
		> input : net.imglib2.RandomAccessibleInterval<I extends net.imglib2.type.numeric.RealType<I>>
			input image
			
		> sigma : java.lang.Double
			sigma value for gaussian blur
			
		Returns : net.imglib2.RandomAccessibleInterval<net.imglib2.type.numeric.real.DoubleType>

```

#### 1.4 Override the Op's base computation method

```java
    @Override
	public RandomAccessibleInterval<DoubleType> apply(
		RandomAccessibleInterval<I> input, Double sigma)
	{
		RandomAccessibleInterval<DoubleType> blur = gaussOp.apply(input, sigma);
		RandomAccessibleInterval<DoubleType> output = createOp.apply(input,
			new DoubleType());
		subOp.compute(input, blur, output);
		return output;
	}

```

`apply`, `compute`, `mutate`

### 2 Write an Op test

Place holder text.

### 3 Use your new Op with Fiji

Run the `filter.gaussianSub` Op in Fiji like this:


```scijava-groovy
#@ OpEnvironment ops
#@ Img img
#@output result

result = ops.op("filter.gaussianSub").input(img, 10.0).apply()
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
