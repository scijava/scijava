# Scripting in Fiji

Scripts provide a simple, familiar interface for accessing SciJava Ops and allows combination with additional resources for image processing and beyond. The following page will explain how you can write a script in Fiji's [Script Editor](https://imagej.net/scripting/) that utilizes Ops for image processing.

## Obtaining an OpEnvironment

To run Ops we always start with an `OpEnvironment`. The easiest way to obtain an `OpEnvironment` with all available Ops is to declare an `OpEnvironment` as a [script parameter](https://imagej.net/scripting/parameters):

```text
#@ OpEnvironment ops
```

## Setting inputs and outputs

A good starting point is to declare script parameters that match your desired Op's parameters. When performing image processing we are modifying an existing image, so we also want to create an output image in the script so our result will be shown. The following lines use SciJava's script parameters to obtain the active image as an input along with a user-defined sigma, while establishing our output.

```text
#@ Img imgInput
#@ Double sigma
#@output Img out
```

## Calling Ops

The [OpBuilder syntax](CallingOps) should be used to retrieve and execute Ops from the `OpEnvironment`. The following line executes a Gaussian Blur on an input image using a `filter.gauss` Op:
```text
out = ops.op("filter.gauss").input(imgInput, sigma).apply()
```

## Putting it all together

The below script can be pasted into the Script Editor. **Ensure that the Script Editor is configured to run a Groovy script**.

```text
#@ OpEnvironment ops
#@ Img imgInput
#@ Double sigma
#@output Img out

// Call our Op!
out = ops.op("filter.gauss").input(imgInput, sigma).apply()
```

## Add your Op to the menu

If you want to re-use an Op outside of the script editor: good news! All SciJava scripts are ImageJ plugins and can be [installed](https://imagej.net/plugins/index#installing-plugins-manually) into your Fiji installation. For example, if we create a new `Ops` folder in our `Fiji.app/plugins/` directory and save our script there as `FilterGauss_.groovy` (or similar - just don't forget the `_`!) then after re-starting Fiji we can run our Op from the `Plugins > Ops` menu, and it will also show up in the [search bar](https://imagej.net/learn/#the-search-bar).

## Next steps

Check out the How-To Guides for important information like how to [explore the available Ops](SearchingForOps).

Check out some examples such as [image deconvolution](examples/deconvolution.rst) or [FLIM analysis](examples/flim_analysis.rst) to see more complete cases of Ops being used in the Script editor! 
