# Searching for Ops in the Environment

As the `OpEnvironment` is fully extensible, different `OpEnvironment`s might contain different Ops, and it is important to be able to query an `OpEnvironment` about the available Ops.

Users can query the `OpEnvironment` for Ops matching a given name using the method `OpEnvironment.descriptions(String)`. The following SciJava script queries an `OpEnvironment` for all Ops matching the name `filter.gauss`:

```groovy
import org.scijava.ops.engine.DefaultOpEnvironment
ops = new DefaultOpEnvironment()

print(ops.descriptions("filter.gauss"))
```
This script yields the following printout:
```groovy
[filter.gauss(
	 Inputs:
		net.imglib2.RandomAccessibleInterval<T> input1
		java.util.concurrent.ExecutorService input2
		double[] input3
		net.imglib2.outofbounds.OutOfBoundsFactory<T, net.imglib2.RandomAccessibleInterval<T>> input4?
	 Containers (I/O):
		net.imglib2.RandomAccessibleInterval<T> container1
)
, filter.gauss(
	 Inputs:
		net.imglib2.RandomAccessibleInterval<T> input1
		java.util.concurrent.ExecutorService input2
		double[] input3
	 Containers (I/O):
		net.imglib2.RandomAccessibleInterval<T> container1
)
, filter.gauss(
	 Inputs:
		net.imglib2.RandomAccessibleInterval<T> input1
		java.util.concurrent.ExecutorService input2
		java.lang.Double input3
		net.imglib2.outofbounds.OutOfBoundsFactory<T, net.imglib2.RandomAccessibleInterval<T>> input4?
	 Containers (I/O):
		net.imglib2.RandomAccessibleInterval<T> container1
)
, filter.gauss(
	 Inputs:
		net.imglib2.RandomAccessibleInterval<T> input1
		java.util.concurrent.ExecutorService input2
		java.lang.Double input3
	 Containers (I/O):
		net.imglib2.RandomAccessibleInterval<T> container1
)
]
```
