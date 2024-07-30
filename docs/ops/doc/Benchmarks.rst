SciJava Ops Benchmarks
======================

This page describes a quantitative analysis of the SciJava Ops framework, and is heavily inspired by a similar comparison of `ImgLib2 <https://imagej.net/libs/imglib2/benchmarks>`_. In all figures, benchmark times are displayed using bar charts (describing mean execution times, in microseconds) with error bars (used to denote the range of observed execution times).

Hardware and Software
---------------------

This analysis was performed with the following hardware:

* Mainboard: Gigabyte Technology Co., Ltd. X570 AORUS PRO WIFI
* CPU: AMD Ryzen 7 3700X × 16
* Memory: 32 GB 3200 MHz DDR4 RAM

The following software components were used:

* Ubuntu 24.04 LTS
* OpenJDK 64-Bit Server VM Zulu11.72+19-CA (build 11.0.23+9-LTS, mixed mode)
* SciJava core commit `018d03ed <https://github.com/scijava/scijava/commit/018d03edd2c4fd20747b472d87f65a8a7033bfe1>`_
* ImageJ Ops version ``2.0.0``

All benchmarks are executed using the `Java Microbenchmark Harness <https://github.com/openjdk/jmh>`_, using the following parameters:

* Forked JVM
* 1 warmup execution
* 2 10-second iterations per warm-up execution
* 1 measurement execution
* 5 5-second iterations per measurement execution

Op Matching
-----------

We first analyze the performance of executing the following static method, written to contain the *fewest* instructions possible while also avoiding code removal by the Just In Time compiler:

..  code-block:: java

	/**
	 * Increments a byte value.
	 *
	 * @param data array containing the byte to increment
	 * @implNote op name="benchmark.increment", type=Inplace1
	 */
	public static void incrementByte(final byte[] data) {
		data[0]++;
	}

We first benchmark the overhead of executing this method through SciJava Ops, compared to direct execution of the static method. This method mutates a data structure in place, meaning the Ops engine can match it directly as an inplace Op, or **adapt** it to a function Op. Thus, we test the benchmark the following four scenarios:

* Static method invocation
* Output Buffer creation + static method invocation **(A)**
* SciJava Ops inplace invocation
* SciJava Ops **function** invocation **(A)**

The results are shown in **Figure 1**. We find Op execution through the SciJava Ops framework adds approximately 100 microseconds of additional overhead. An additional millisecond of overhead is observed when SciJava Ops additionally creates an output buffer.

.. chart:: ../images/BenchmarkMatching.json

	**Figure 1:** Algorithm execution performance (lower is better)

Note that the above requests are benchmarked without assistance from the Op cache, measuring the overhead of full matching process. As repeated Op requests will utilize the Op cache, we benchmark cached Op execution separately, with results shown in **Figure 2**. From these results, we conlude that Op matching comprises the majority of SciJava Ops overhead, and repeated executions add only a few microseconds of overhead.

.. chart:: ../images/BenchmarkCaching.json

	**Figure 2:** Algorithm execution performance with Op caching (lower is better)

Finally, we benchmark the overhead of SciJava Ops parameter conversion. Suppose we instead wish to operate upon a ``double[]`` - we must convert it to ``byte[]`` to call our Op. We consider the following procedures:

* Array conversion + static method invocation **(C)**
* Array buffer creation + array conversion + static method invocation **(A+C)**
* SciJava Ops converted inplace invocation **(C)**
* SciJava Ops converted **function** invocation **(A+C)**

The results are shown in **Figure 3**; note the Op cache is **not** enabled. We find that parameter conversion imposes additional overhead of approximately 1 millisecond, and when both parameter conversion and Op adaptation are used the overhead (~2 milliseconds) is *additive*.

.. chart:: ../images/BenchmarkConversion.json

	**Figure 3:** Algorithm execution performance with Op conversion (lower is better)

Framework Comparison
--------------------

To validate our development efforts atop the original `ImageJ Ops <https://imagej.net/libs/imagej-ops/>`_ framework, we additionally wrap the above static method within ImageJ Ops:

.. code-block:: java

	/** Increment Op wrapper for ImageJ Ops. */
	@Plugin(type = Op.class, name = "benchmark.increment")
	public static class IncrementByteOp extends AbstractUnaryInplaceOp<byte[]>
		implements Op
	{

		@Override
		public void mutate(byte[] o) {
			incrementByte(o);
		}
	}

We then benchmark the performance of executing the static method using the following pathways:

* Static method invocation
* SciJava Ops invocation
* ImageJ Ops invocation (using the above wrapper)

The results are shown in **Figure 4**. From this figure we can see that the "Op overhead" from ImageJ Ops is approximately 70x the "Op overhead" from SciJava Ops.

.. chart:: ../images/BenchmarkFrameworks.json

	**Figure 4:** Algorithm execution performance by Framework (lower is better)

We provide a final figure combining all the metrics above:

.. chart:: ../images/BenchmarkCombined.json

	**Figure 5:** All metrics combined (lower is better)

Reproducing these Results
-------------------------

1. Create a local copy of the SciJava core from the `GitHub repository <https://github.com/scijava/scijava>`_
2. Ensure you have package manager `Mamba <https://mamba.readthedocs.io/en/latest/installation/mamba-installation.html#fresh-install-recommended>`_ installed.
3. Run the script `docs/ops/bin/benchmark.sh`, which will:
    * Create the mamba environment
    * Build the benchmarking code
    * Execute all JMH benchmarks
    * Build `plotly <https://plotly.com/>`_ figures for each benchmark
    * Distill each figure into JSON, stored in the correct place

4. View the benchmark results, either by:
    * Viewing the final lines of the JMH output file ``docs/ops/scijava-ops-benchmarks_results.txt``, **or**
    * Locally building the documentation by navigating to ``docs``, executing ``make clean html && python -m http.server`` and navigating to this page.
