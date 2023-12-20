# SciJava Ops Benchmarks

This page describes a quantitative analysis of the SciJava Ops framework, and is heavily inspired by a similar comparison of [ImgLib2](https://imagej.net/libs/imglib2/benchmarks).

For this analysis, we compare SciJava Ops against:
1) Raw code execution
2) [ImageJ Ops](https://imagej.net/libs/imagej-ops/index)

Some of the charts shown plot execution time as a function of the number of executions - this allows us to show the impact of both the just-in-time (JIT) compiler and the framework's ability to improve performance on repeated function calls.

Other charts plot execution time as a function of input size, which allows analysis of framework overhead.

## Hardware and Software

This analysis was performed with the following hardware:
* 2017 Dell Inspiron 15 7000 Gaming
* Intel(R) Core(TM) i7-7700HQ CPU @ 2.80GHz
* 16 GB 2400 MHz DDR4 RAM
The following software components were used:
* Ubuntu 22.04.3 LTS
* OpenJDK Runtime Environment (build 11.0.21) with OpenJDK 64-Bit Server VM (build 11.0.21, mixed mode, sharing)
* SciJava Ops Engine version `0.0-SNAPSHOT`
* ImageJ Ops version `2.0.0`

## "Cheap" Operations

Following the precedent of ImgLib2, we first analyze the performance of each execution method in performing a simple byte inversion, which can be performed very quickly. This operation lends itself to the analysis of framework overhead.
* The `Raw` execution method simply calls a static method
* The `SciJava Ops` execution method discovers the static method through the `@implNote` Op declaration and calls the functionality through an `OpEnvironment`
* The `ImageJ Ops` execution method discovers the static method through an `@Plugin` wrapper class and calls the functionality through an `OpService`

```java
/**
 * @param data the data to invert
 * @implNote op name="invert",type=Inplace1
 */
public static void invertRaw(final byte[] data) {
    for (int i = 0; i < data.length; i++) {
        final int value = data[i] & 0xff;
        final int result = 255 - value;
        data[i] = (byte) result;
    }
}
```

### Repetition Results

We first note that the JIT improves performance benefits starting from the second iteration on all tested methods.

For the very first execution, both SciJava Ops and ImageJ Ops incur decreased performance due to the overhead of discovering and matching the required functionality. The overhead for SciJava Ops is approximately half as much as the overhead of ImageJ Ops.

Starting from the second execution, SciJava Ops rivals the performance of Raw execution, as it caches the results of prior matching calls. ImageJ Ops does not provide similar functionality.

<div>
<div class="dygraph" id="cheapIterationVsTime1" style="width: 50%; float:left"></div>
<div class="dygraph" id="cheapIterationVsTime25" style="width: 50%"></div>
</div>

### Scaling Results

For the first iteration, we see a consistent hierarchy, where Raw execution outperforms SciJava Ops, and SciJava Ops outperforms ImageJ Ops. This hierarchy stems from matching overhead.

For the final iteration, we see that SciJava Ops consistently performs equivalently to Raw execution, while ImageJ Ops maintains worse performance from matching overhead.

<div>
<div class="dygraph" id="cheapResolutionVsTime1" style="width: 50%; float:left"></div>
<div class="dygraph" id="cheapResolutionVsTime10" style="width: 50%"></div>
</div>

## "Expensive" Operations

We now analyze the performance of each execution method in performing randomization. This operation is much more intensive, and allows us to ensure that the computation dominates any framework overhead.
* The `Raw` execution method simply calls a static method
* The `SciJava Ops` execution method discovers the static method through the `@implNote` Op declaration and calls the functionality through an `OpEnvironment`
* The `ImageJ Ops` execution method discovers the static method through an `@Plugin` wrapper class and calls the functionality through an `OpService`

```java
/**
 * @param data the data to invert
 * @implNote op name="randomize",type=Inplace1
 */
public static void randomizeRaw(final byte[] data) {
    for (int i = 0; i < data.length; i++) {
        final int value = data[i] & 0xff;
        final double result = expensiveOperation(value);
        data[i] = (byte) result;
    }
}
```

### Repetition Results

For this more expensive operation, we see that computation does indeed dominate any framework overhead for both SciJava Ops and ImageJ Ops. At the smallest input size, we see that ImageJ Ops still shows noticeable matching overhead at every iteration, and SciJava Ops shows noticeable overhead for only the first few iterations.

<div>
<div class="dygraph" id="expensiveIterationVsTime1" style="width: 50%; float:left"></div>
<div class="dygraph" id="expensiveIterationVsTime25" style="width: 50%"></div>
</div>

### Scaling Results

As computation dominates overhead, we see no noticeable difference between the three execution methods.

<div>
<div class="dygraph" id="expensiveResolutionVsTime1" style="width: 50%; float:left"></div>
<div class="dygraph" id="expensiveResolutionVsTime10" style="width: 50%"></div>
</div>

## Reproducing these results

To reproduce these results, take the following steps:
1) Assuming you have a supported Python 3.x installed, you can run `src/main/scripts.benchmark.sh` within the SciJava Ops Benchmarks module. This will generate a file `copyme.txt` in your current directory
2) Copy the contents of `copyme.txt` into the HTML at the bottom of this page, starting at the tag `<!--Begin Pasting Benchmark Data-->`
3) Rebuild the document and view the graphs in a browser

<!-- Populate graphs -->

<script type="text/javascript" src="https://cdn.jsdelivr.net/npm/dygraphs@2.1.0/dist/dygraph.min.js"></script>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/dygraphs@2.1.0/dist/dygraph.min.css" />
<style type="text/css">
  .dygraph {
    display: inline-block;
    max-width: 100%;
    width: 435px;
    height: 250px;
  }
  .dygraph-legend {
    background-color: rgba(200, 200, 255, 0.75) !important;
    padding: 4px;
    border: 1px solid #000;
    border-radius: 10px;
    box-shadow: 4px 4px 4px #888;
    pointer-events: none;
    width: 12em;
  }
  .dygraph-legend > span.highlight { background-color: rgba(255, 255, 200, 0.75) !important; }
  .dygraph-legend > span.highlight { display: inline; }
</style>


<script type="text/javascript">
  function plot(id, title, xlabel, data) {
    new Dygraph(document.getElementById(id), data, {
      title: title,
      titleHeight: 24,
      xlabel: xlabel,
      ylabel: "Time",
      includeZero: true,
      labelsSeparateLines: true,
      drawPoints: true,
      pointSize: 3,
      highlightCircleSize: 2,
      strokeWidth: 1,
      strokeBorderWidth: 1,
      highlightSeriesOpts: {
        strokeWidth: 3,
        strokeBorderWidth: 1,
        highlightCircleSize: 5
      }
    });
  }

<!--Begin Pasting Benchmark Data-->
plot("cheapIterationVsTime1", "Iteration x Time (ms) at 1 Mpx", "Iteration",
	"Iteration,ImageJ Ops,Raw,SciJava Ops\n" +
	"0,119,11,79\n" +
	"1,15,1,0\n" +
	"2,11,0,0\n" +
	"3,10,1,0\n" +
	"4,10,0,1\n" +
	"5,13,1,0\n" +
	"6,10,0,2\n" +
	"7,7,0,1\n" +
	"8,7,0,0\n" +
	"9,7,0,0");

plot("cheapIterationVsTime25", "Iteration x Time (ms) at 25 Mpx", "Iteration",
	"Iteration,ImageJ Ops,Raw,SciJava Ops\n" +
	"0,129,76,84\n" +
	"1,21,4,4\n" +
	"2,22,4,4\n" +
	"3,25,7,6\n" +
	"4,12,9,4\n" +
	"5,10,4,4\n" +
	"6,14,4,3\n" +
	"7,12,4,6\n" +
	"8,20,5,6\n" +
	"9,13,5,6");

plot("cheapResolutionVsTime1", "Resolution x Time (ms) at iteration #1", "Mpx",
	"Mpx,ImageJ Ops,Raw,SciJava Ops\n" +
	"0,119,11,79\n" +
	"1,118,20,67\n" +
	"2,122,34,100\n" +
	"3,122,14,67\n" +
	"4,118,60,58\n" +
	"5,118,26,85\n" +
	"6,116,100,59\n" +
	"7,113,23,66\n" +
	"8,129,76,84");

plot("cheapResolutionVsTime10", "Resolution x Time (ms) at iteration #10", "Mpx",
	"Mpx,ImageJ Ops,Raw,SciJava Ops\n" +
	"0,7,0,0\n" +
	"1,7,1,1\n" +
	"2,11,1,2\n" +
	"3,8,3,3\n" +
	"4,11,3,2\n" +
	"5,10,3,3\n" +
	"6,8,3,4\n" +
	"7,11,5,7\n" +
	"8,13,5,6");

plot("expensiveIterationVsTime1", "Iteration x Time (ms) at 1 Mpx", "Iteration",
	"Iteration,ImageJ Ops,Raw,SciJava Ops\n" +
	"0,61,60,50\n" +
	"1,40,41,33\n" +
	"2,39,33,34\n" +
	"3,37,33,32\n" +
	"4,34,30,29\n" +
	"5,33,29,27\n" +
	"6,30,26,26\n" +
	"7,29,25,23\n" +
	"8,27,23,23\n" +
	"9,27,23,22");

plot("expensiveIterationVsTime25", "Iteration x Time (ms) at 25 Mpx", "Iteration",
	"Iteration,ImageJ Ops,Raw,SciJava Ops\n" +
	"0,798,837,783\n" +
	"1,790,784,781\n" +
	"2,785,780,774\n" +
	"3,761,756,757\n" +
	"4,731,730,731\n" +
	"5,699,694,693\n" +
	"6,649,645,640\n" +
	"7,599,590,594\n" +
	"8,570,566,566\n" +
	"9,557,551,550");

plot("expensiveResolutionVsTime1", "Resolution x Time (ms) at iteration #1", "Mpx",
	"Mpx,ImageJ Ops,Raw,SciJava Ops\n" +
	"0,61,60,50\n" +
	"1,139,205,124\n" +
	"2,235,304,221\n" +
	"3,331,368,307\n" +
	"4,424,466,407\n" +
	"5,513,557,488\n" +
	"6,596,601,592\n" +
	"7,700,743,674\n" +
	"8,798,837,783");

plot("expensiveResolutionVsTime10", "Resolution x Time (ms) at iteration #10", "Mpx",
	"Mpx,ImageJ Ops,Raw,SciJava Ops\n" +
	"0,27,23,22\n" +
	"1,92,87,87\n" +
	"2,160,156,154\n" +
	"3,222,217,218\n" +
	"4,293,287,286\n" +
	"5,358,352,346\n" +
	"6,424,420,416\n" +
	"7,480,478,478\n" +
	"8,557,551,550");
<!--End Pasting Benchmark Data-->
</script>
