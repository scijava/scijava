# Searching for Ops in the Environment

As the `OpEnvironment` is fully extensible, different `OpEnvironment`s might contain different Ops, and it is important to be able to query an `OpEnvironment` about the available Ops.

Users can query the `OpEnvironment` for available Ops using the method `OpEnvironment.help(String)`. The following example shows you how to find all `"filter.gauss"` Ops in an `OpEnvironment`:

```groovy
import org.scijava.ops.api.OpEnvironment

ops = OpEnvironment.getEnvironment()

print(ops.help("filter.gauss"))
```
This script yields the following printout:
```groovy
[filter.gauss(
        Inputs:
                net.imglib2.RandomAccessibleInterval<I> input1
        double[] input2
                net.imglib2.outofbounds.OutOfBoundsFactory<I, net.imglib2.RandomAccessibleInterval<I>> input3?
        Containers (I/O):
        net.imglib2.RandomAccessibleInterval<O> container1
)
 , filter.gauss(
        Inputs:
                net.imglib2.RandomAccessibleInterval<I> input1
        double[] input2
                Containers (I/O):
        net.imglib2.RandomAccessibleInterval<O> container1
)
 , filter.gauss(
        Inputs:
                net.imglib2.RandomAccessibleInterval<I> input1
        java.lang.Double input2
                net.imglib2.outofbounds.OutOfBoundsFactory<I, net.imglib2.RandomAccessibleInterval<I>> input3?
        Containers (I/O):
        net.imglib2.RandomAccessibleInterval<O> container1
)
 , filter.gauss(
        Inputs:
                net.imglib2.RandomAccessibleInterval<I> input1
        java.lang.Double input2
                Containers (I/O):
        net.imglib2.RandomAccessibleInterval<O> container1
)
]
```

**Note**: The no-argument call to `OpEnvironment.descriptions()` will print out *all Ops in the `OpEnvironment`*.
