SciJava Ops Benchmarks
======================

This page describes a quantitative analysis of the SciJava Ops framework, and is heavily inspired by a similar comparison of `ImgLib2 <https://imagej.net/libs/imglib2/benchmarks>`_.

Hardware and Software
---------------------

This analysis was performed with the following hardware:

* Dell Precision 7770
* 12th Gen Intel i9-12950HX (24) @ 4.900GHz
* 32 GB 4800 MT/s DDR5 RAM

The following software components were used:

* Ubuntu 23.10
* Kernel 6.5.0-26-generic
* OpenJDK Runtime Environment AdoptOpenJDK (build 11.0.8+10) with OpenJDK 64-Bit Server VM AdoptOpenJDK (build 11.0.8+10, mixed mode)
* SciJava Incubator commit `77006edc <https://github.com/scijava/incubator/commit/77006edc6a567a08ec5aba39e56fdfab8d79a0b9>`_
* ImageJ Ops version ``2.0.0``

All benchmarks are executed using the `Java Microbenchmark Harness <https://github.com/openjdk/jmh>`_, using the following parameters:

* Forked JVM
* 2 warmup executions
* 2 10-second iterations per warm-up execution
* 1 measurement execution
* 5 5-second iterations per measurement execution

Op Matching
-----------

We first analyze the performance of executing the following static method:

..  code-block:: java

	/**
	 * @param in the data to input to our function
	 * @param d the value to add to each element in the input
	 * @param out the preallocated storage buffer
	 * @implNote op name="benchmark.match",type=Computer
	 */
	public static void op( //
		final RandomAccessibleInterval<DoubleType> in, //
		final Double d, //
		final RandomAccessibleInterval<DoubleType> out //
	) {
		LoopBuilder.setImages(in, out)
			.multiThreaded()
			.forEachPixel((i, o) -> o.set(i.get() + d));
	}

We first benchmark the base penalty of executing this method using SciJava Ops, compared to direct execution of the static method. Notably, as this method requires a preallocated output buffer, we must either create it ourselves, *or* allow SciJava Ops to create it for us using an Op adaptation. Thus, we test the benchmark the following three scenarios:

* Output buffer creation + static method invocation
* Output buffer creation + SciJava Ops invocation
* SciJava Ops invocation using Op adaptation

The results are shown in **Figure 1**. We find Op execution through the SciJava Ops framework adds a few milliseconds of additional overhead. A few additional milliseconds of overhead are observed when SciJava Ops is additionally tasked with creating an output buffer.

.. chart:: ../images/BenchmarkMatching.json

	**Figure 1:** Algorithm execution performance (lower is better)

Note that the avove requests are benchmarked without assistance from the Op cache, i.e. they are designed to model the full matching process. As repeated Op requests will utilize the Op cache, we benchmark cached Op retrieval separately, with results shown in **Figure 2**. These benchmarks suggest Op caching helps avoid the additional overhead of Op adaptation as its performance approaches that of normal Op execution.

.. chart:: ../images/BenchmarkCaching.json

	**Figure 2:** Algorithm execution performance with Op caching (lower is better)

Finally, we benchmark the overhead of SciJava Ops parameter conversion. Suppose we instead wish to operate upon a ``RandomAccessibleInterval<ByteType>`` - we must convert it to call our Op. We consider the following procedures:

* Image conversion + output buffer creation + static method invocation
* output buffer creation + SciJava Ops invocation using Op conversion
* SciJava Ops invocation using Op conversion and Op adaptation

The results are shown in **Figure 3**; note the Op cache is **not** enabled. We observe overheads on the order of 10 milliseconds to perform Op conversion with and without Op adaptation.

.. chart:: ../images/BenchmarkConversion.json

	**Figure 3:** Algorithm execution performance with Op conversion (lower is better)

Framework Comparison
--------------------

To validate our development efforts atop the original `ImageJ Ops <https://imagej.net/libs/imagej-ops/>`_ framework, we benchmark executions of the following method:

.. code-block:: java

	/**
	 * @param data the data to invert
	 * @implNote op name="benchmark.invert",type=Inplace1
	 */
	public static void invertRaw(final byte[] data) {
		for (int i = 0; i < data.length; i++) {
			final int value = data[i] & 0xff;
			final int result = 255 - value;
			data[i] = (byte) result;
		}
	}

We then benchmark the performance of executing this code using the following pathways:

* Static method invocation
* SciJava Ops invocation
* ImageJ Ops invocation (using a ``Class`` wrapper to make the method discoverable within ImageJ Ops)

The results are shown in **Figure 4**. When algorithm matching dominates execution time, the SciJava Ops matching framework provides significant improvement in matching performance in comparison with the original ImageJ Ops framework.

.. chart:: ../images/BenchmarkFrameworks.json

	**Figure 4:** Algorithm execution performance by Framework (lower is better)

Finally, here is a figure combining all the metrics above:

.. chart:: ../images/BenchmarkCombined.json

	**Figure 5:** All metrics combined (lower is better)

Reproducing these Results
-------------------------

1. Create a local copy of the SciJava Ops incubator from the `GitHub repository <https://github.com/scijava/incubator>`_
2. Ensure you have package manager `Mamba <https://mamba.readthedocs.io/en/latest/installation/mamba-installation.html#fresh-install-recommended>`_ installed.
3. Run the script `docs/ops/bin/benchmark.sh`, which will:
    * Create the mamba Environment
    * Build the benchmarking code
    * Execute all JMH benchmarks
    * Build `plotly <https://plotly.com/>`_ figures for each benchmark
    * Distill each figure into JSON, stored in the correct place

4. View the benchmark results, either by:
    * Viewing the final lines of the JMH output file ``docs/ops/scijava-ops-benchmarks_results.txt``, **or**
    * Locally building the documentation by navigating to ``docs``, executing ``make clean html && python -m http.server`` and navigating to this page.
