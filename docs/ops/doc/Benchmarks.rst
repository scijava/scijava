SciJava Ops Benchmarks
======================


This page describes a quantitative analysis of the SciJava Ops framework, and is heavily inspired by a similar comparison of `ImgLib2 <https://imagej.net/libs/imglib2/benchmarks>`_.

Hardware and Software
---------------------

This analysis was performed with the following hardware:

* 2021 Dell OptiPlex 5090 Small Form Factor
* Intel(R) Core(TM) i7-10700 CPU @ 2.90GHz
* 64 GB 3200 MHz DDR4 RAM

The following software components were used:

* Ubuntu 22.04.3 LTS
* OpenJDK Runtime Environment (build 11.0.21) with OpenJDK 64-Bit Server VM (build 11.0.21, mixed mode, sharing)
* SciJava Ops Engine version ``0.0-SNAPSHOT``
* ImageJ Ops version ``2.0.0``

All benchmarks are executed using the `Java Microbenchmark Harness <https://github.com/openjdk/jmh>`_, using the following parameters:

* Forked JVM
* 2 warmup executions
* 2 10-second iterations per warm-up execution
* 1 measurement execution
* 5 10-second iterations per measurement execution

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
  
The results are shown in the chart below:

.. chart:: ../images/BenchmarkMatching.json

	Algorithm execution performance (lower is better)

Note that the avove requests are benchmarked without assistance from the Op cache, i.e. they are designed to model the full matching process. As repeated Op requests will utilize the Op cache, we benchmark cached Op retrieval separately, with these results shown below:

.. chart:: ../images/BenchmarkCaching.json

	Algorithm execution performance with Op caching (lower is better)

We can see that with Op caching, TODO

Finally, we benchmark the overhead of SciJava Ops parameter conversion, by considering the situation where we initially have a ``RandomAccessibleInterval<ByteType>``, which we must convert to call our method. In such a situation, we consider the following procedures:

* Image conversion + output buffer creation + static method invocation
* output buffer creation + SciJava Ops invocation using Op conversion
* SciJava Ops invocation using Op conversion and Op adaptation

The results are shown in the chart below:

.. chart:: ../images/BenchmarkConversion.json

	Algorithm execution performance with Op conversion (lower is better)

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

The results are shown in the chart below:

.. chart:: ../images/BenchmarkFrameworks.json

	Algorithm execution performance by Framework (lower is better)

From these results, we can see that, when algorithm matching dominates execution time, the SciJava Ops matching framework provides significant improvement in matching performance in comparison with the original ImageJ Ops framework.

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
