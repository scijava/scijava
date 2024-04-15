# SciJava Ops FLIM: A fluorescence lifetime analysis library for SciJava Ops.

SciJava Ops FLIM is a collection of FLIM analysis ops based on [FLIMLib](https://github.com/flimlib/flimlib). It extends the single-transient fitting functions in FLIMLib to dataset-level fitting ops. Currently supported fitting ops include: RLD, MLA, Global, Phasor, and single-component Bayesian.

Besides curve fitting, SciJava Ops FLIM also provides a variety of pre-processing options such as pixel binning, intensity thresholding, ROI masking as well as post-processing utility ops for e.g. calculating τ<sub>m</sub> (mean lifetime), A<sub>i</sub>% (fractional contribution) and pseudocoloring the result with LUT.

# Example usage
Open [test2.sdt](test_files/test2.sdt) in [Fiji](https://fiji.github.io/). Execute in [Script Editor](http://imagej.github.io/Using_the_Script_Editor) as Groovy:

```groovy
#@ UIService ui
#@ OpEnvironment op
#@ ImgPlus img

// set up parameters
import org.scijava.ops.flim.FitParams

param = new FitParams()
param.transMap = img; // input 3-dimensional (x, y, t) dataset
param.xInc= 0.040     // time difference between bins (ns)
param.ltAxis = 2      // time bins lay along axis #2

// op call
fittedImg = op.unary("flim.fitLMA").input(param).apply().paramMap

// display each parameter
zImg = op.op("transform.hyperSliceView").input(fittedImg, param.ltAxis, 0L).apply()
AImg = op.op("transform.hyperSliceView").input(fittedImg, param.ltAxis, 1L).apply()
tauImg = op.op("transform.hyperSliceView").input(fittedImg, param.ltAxis, 2L).apply()

ui.show("z", zImg)
ui.show("A", AImg)
ui.show("tau", tauImg)
```

After running this script, the output shown below can be seen by first running the script, and then brightness and contrast (Ctrl + Shift + C, select "Set" and bound the contrast to each image's corresponding range below).

(z in [-1, 1], A in [0, 4], tau in[0, 3]):

![example output](images/example%20z.png)![example output](images/example%20A.png)![example output](images/example%20tau.png)

See more examples in [Demo.ipynb](notebooks/Demo.ipynb) and [groovy.md](groovy.md).

# Using from a Java project

To depend on SciJava Ops FLIM, copy the following to your `pom.xml`:

```xml
  <properties>
    <scijava-ops-flim.version>0-SNAPSHOT</scijava-ops-flim.version>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.scijava</groupId>
      <artifactId>scijava-ops-flim</artifactId>
      <version>${scijava-ops-flim.version}</version>
    </dependency>
  </dependencies>
```

# See also

- [FLIMLib](https://github.com/flimlib/flimlib): Curve fitting library for FLIM
    - [Debug tutorial](https://github.com/flimlib/flimlib/wiki/Debugging)
- [FLIMJ Ops](https://github.com/flimlib/flimj-ops): ImageJ Ops for accessing FLIM

# Citation

Coming soon...
