# Migrating from ImageJ Ops

The [ImageJ Ops](https://imagej.net/libs/imagej-ops/index) framework is the predecessor of the SciJava Ops framework, and many steps were taken to make expressing Ops much easier. This document shows ImageJ Ops developers how to convert their Ops to SciJava Ops - we'll convert [`DefaultGaussRAI`](https://github.com/imagej/imagej-ops/blob/master/src/main/java/net/imagej/ops/filter/gauss/DefaultGaussRAI.java) from the ImageJ Ops repository as an example.

## 1. Upgrading pom-scijava

Before doing anything else, you should ensure that your version of pom-scijava is new enough to make use of SciJava 3 goodness.

TODO: Replace with the pom-scijava version needed to grab this annotation processor.

Until the SciJava 3 annotation processor is added to pom-scijava, developers must add the following block of code to the `build` section of your POM:

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
                    <arg>-Ajavadoc.packages="${therapi.packages}"</arg>
                </compilerArgs>
            </configuration>
        </plugin>
    </plugins>
</build>
```

The other step recommended in your POM is to remove the dependency on ImageJ Ops!

## 2. Removing all the boilerplate

In ImageJ Ops, all Ops were written as `Class`es, with SciJava `@Plugin` and `@Parameter` annotations everywhere - most of these annotations can be removed in SciJava Ops!

### 2.1 Replace the `@Plugin` annotation with an `@implNote` in the javadoc

SciJava Ops uses a separate annotation processor to record plugins at compile time. This allows SciJava Ops to discover Ops *without any runtime dependencies*! Thus, instead of using an annotation, plugin declaration is performed *within the javadoc* of all plugins, using the `@implNote` tag new to Java 9. The general schema for op declaration is as follows:

```java
/**
 * My sweet Op
 * 
 * @implNote op names='name1'
 */
```

Additional options include:
* Providing multiple names by replacing `names='name1'` with `names='name1,name2,...'` to allow your Op to be discoverable under multiple names
* Providing the Op's priority by appending `priority=<the priority>`. SciJava Ops uses the same priority values as ImageJ Ops2, and relative priorities can be retained by using the new SciJava Priority library.

The following diff shows the changes needed to replace the `@Plugin` annotation:

```diff
  * @author Christian Dietz (University of Konstanz)
  * @author Stephan Saalfeld
  * @param <T> type of input and output
+ * @implNote op names='filter.gauss', priority='1.0'
  */
 @SuppressWarnings({ "unchecked", "rawtypes" })
-@Plugin(type = Ops.Filter.Gauss.class, priority = 1.0)
 public class DefaultGaussRAI<T extends NumericType<T> & NativeType<T>> extends
        AbstractUnaryHybridCF<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
        implements Ops.Filter.Gauss
```

### 2.2 Moving parameters to the functional method

SciJava Ops no longer uses the `@Parameter` annotation to declare parameters - *all* Op parameters are instead passed through the functional method.

To conform `DefaultGaussRAI` to this schema, the following diff should be applied:

```diff
 public class DefaultGaussRAI<T extends NumericType<T> & NativeType<T>> extends
        AbstractUnaryHybridCF<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
        implements Ops.Filter.Gauss
 {
 
-       @Parameter
-       private ThreadService threads;
-
-       @Parameter
-       private double[] sigmas;
-
-       @Parameter(required = false)
-       private OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBounds;
-
        @Override
        public void compute(final RandomAccessibleInterval<T> input,
+               final ThreadService threads,
+               final double[] sigmas,
+               @Nullable OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBounds,
                final RandomAccessibleInterval<T> output)
        {
```

**Note**: nullable parameters are denoted using the `org.scijava.ops.spi.Nullable` annotation. Just like in ImageJ Ops, if the user does *not* decide to pass that parameter, it will be assigned to `null`, so leave in any null-checks for nullable parameters.

## 3 Implementing the right functional interface

Ops in the SciJava Ops framework implement a new set of Op types, designed for functional simplicity. These functional interfaces are built off of the `java.util.function.Function` interface, and are housed in the SciJava Functions library. 

The first step to finding the right functional interface for the Op being converted. While the `Computer`, `Function`, and `Inplace` Op interfaces persist in SciJava Ops, there are many more interfaces *because* all parameters are defined in the functional method.

If your Op was originally a `Function`, then the Op should implement one of the following interfaces:
* `java.util.function.Function`, if your Op has only one parameter
* `java.util.function.BiFunction`, if your Op has only two parameters
* `org.scijava.function.Functions.ArityX`, if your Op has `X>2` parameters

If your Op was originally a `Computer` and now has `X` parameters in its functional method, then the Op should implement `org.scijava.function.Computers.ArityX`.

Finally, if your Op was originally an `Inplace` and now has `X` parameters in its functional method, with parameter `Y` being the one to mutate, then the Op should implement `org.scijava.function.Inplace.ArityX_Y`.

After the last step, we see that our example Op (a `Computer`) has 4 inputs - thus it should implement `org.scijava.function.Computers.Arity4` - below is the diff of the change necessary:

```diff
  * @author Christian Dietz (University of Konstanz)
  * @author Stephan Saalfeld
  * @param <T> type of input and output
  * @implNote op names='filter.gauss', priority='1.0'
  */
 @SuppressWarnings({ "unchecked", "rawtypes" })
-public class DefaultGaussRAI<T extends NumericType<T> & NativeType<T>> extends
-       AbstractUnaryHybridCF<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
-       implements Ops.Filter.Gauss
+public class DefaultGaussRAI<T extends NumericType<T> & NativeType<T>> implements
+       Computers.Arity4<RandomAccessibleInterval<T>, ThreadSerice, double[], OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>>
 {
```

Note two things when you change the interface:
* The interface `Ops.Filter.Gauss` is no longer necessary - if you are implementing some such interface, you no longer need it!
* Additional interface methods, including hybrid Op method like `createOutput` are no longer necessary, and can also be deleted. Below is the diff of removing `createOutput` from our example Op:
  ```diff
                    }
            }

    -       @Override
    -       public RandomAccessibleInterval<T> createOutput(
    -               final RandomAccessibleInterval<T> input)
    -       {
    -               return ops().create().img(input);
    -       }
    -
    }
  ```

## The Example Op, configured for SciJava Ops

And that's it! Your Op is now ready to be used in SciJava Ops!

```java
/**
 * Gaussian filter, wrapping {@link Gauss3} of imglib2-algorithms.
 *
 * @author Christian Dietz (University of Konstanz)
 * @author Stephan Saalfeld
 * @param <T> type of input and output
 * @implNote op names='filter.gauss', priority='1.0'
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DefaultGaussRAI<T extends NumericType<T> & NativeType<T>> implements
		Computers.Arity4<RandomAccessibleInterval<T>, ThreadSerice, double[], OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>>
{

	@Override
	public void compute(final RandomAccessibleInterval<T> input,
			final ThreadService threads,
			final double[] sigmas,
			@Nullable OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBounds,
			final RandomAccessibleInterval<T> output)
	{

		if (outOfBounds == null) {
			outOfBounds = new OutOfBoundsMirrorFactory<>(Boundary.SINGLE);
		}

		final RandomAccessible<FloatType> eIn = //
				(RandomAccessible) Views.extend(input, outOfBounds);

		try {
			SeparableSymmetricConvolution.convolve(Gauss3.halfkernels(sigmas), eIn,
					output, threads.getExecutorService());
		}
		catch (final IncompatibleTypeException e) {
			throw new RuntimeException(e);
		}
	}

}
```
