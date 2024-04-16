=============
FLIM Analysis
=============

In this example we will use SciJava Ops within Fiji to perform `FLIM`_ analysis, which is used in many situations including photosensitizer detection and `FRET`_ measurement.

.. image:: https://media.imagej.net/scijava-ops/1.0.0/flim_example_input.gif
    :width: 49%
.. image:: https://media.imagej.net/scijava-ops/1.0.0/flim_example_pseudocolored.png
    :width: 49%

We use a sample of `FluoCells™ Prepared Slide #1`_, imaged by `Jenu Chacko`_ using `Openscan-LSM`_ and SPC180 electronics with multiphoton excitation and a 40x WI lens.

  FluoCells™ Prepared Slide #1 contains bovine pulmonary artery endothelial cells (BPAEC). MitoTracker™ Red CMXRos was used to stain the mitochondria in the live cells, with accumulation dependent upon membrane potential. Following fixation and permeabilization, F-actin was stained with Alexa Fluor™ 488 phalloidin, and the nuclei were counterstained with the blue-fluorescent DNA stain DAPI.

The sample data can be downloaded `here <https://media.imagej.net/scijava-ops/1.0.0/flim_example_data.sdt>`_ and can be loaded into Fiji with `Bio-Formats`_ using ``File → Open``. When presented with the ``Bio-Formats Import Options`` screen, it may be helpful to select ``Metadata viewing → Display metadata`` to determine values necessary for analysis. Then, select ``OK``. The data may take a minute to load.

Within the script, the `Levenberg-Marquardt algorithm`_ fitting Op of SciJava Ops FLIM is used to fit the data.

Basic analysis
---------------------

Script execution requires a number of parameters, which may be useful for adapting this script to other datasets. For this dataset, we use the following values:

+--------------------------------------+-------+
| Parameter                            | Value |
+======================================+=======+
| Time Base                            | 12.5  |
+--------------------------------------+-------+
| Time Bins                            | 256   |
+--------------------------------------+-------+
| Lifetime Axis                        | 2     |
+--------------------------------------+-------+
| Intensity Threshold                  | 18    |
+--------------------------------------+-------+
| Bin Kernel Radius                    | 1     |
+--------------------------------------+-------+

The script above will display the fit results, as well as a *pseudocolored* output image. To visualize , it should be contrasted using ImageJ's B&C plugin (``Ctrl + Shift + C``). Using that plugin, the minimum and maximum can be set by selecting the ``Set`` option, and providing ``0`` as the minimum and ``3`` as the maximum.

The results are shown in the panels below, and are described from left to right:

* The first initial fluorescence parameter A\ :subscript:`1`

* The first fluorescence lifetime τ\ :subscript:`1`.

* The pseudocolored result, an HSV image where

  * Hue is a function of τ\ :subscript:`1`, where the function is a LUT

  * Value is a function of A\ :subscript:`1`

.. image:: https://media.imagej.net/scijava-ops/1.0.0/flim_example_a1.png
    :width: 32%

.. image:: https://media.imagej.net/scijava-ops/1.0.0/flim_example_tau1.png
    :width: 32%

.. image:: https://media.imagej.net/scijava-ops/1.0.0/flim_example_pseudocolored.png
    :width: 32%

The pseudocolored result shows a clear separation of fluorophores, which could be segmented and further processed.

Subsampling Within ROIs
-----------------------

Curve fitting can be an intensive process, requiring significant resources to process larger datasets. For this reason, there can be significant benefit in restricting computation to Regions of Interest (ROIs), and SciJava Ops FLIM allows ROIs to restrict computation for all fitting Ops.

The provided script allows users to specify ROIs by drawing selections using the ImageJ UI. These selections are converted to ImgLib2 ``RealMask`` objects, which are then optionally passed to the Op.

In the panels below, we show script execution with computation restricted to the area around a single cell. In the top left panel, we can see the original dataset, annotated with an elliptical selection using the ImageJ UI. In the top right, bottom left, and bottom right panels, we see the A\ :subscript:`1` component, τ\ :subscript:`1` component, and pseudocolored results, respectively, all limited to the area within the selection.

.. image:: https://media.imagej.net/scijava-ops/1.0.0/flim_example_input_roi.png
    :width: 49%

.. image:: https://media.imagej.net/scijava-ops/1.0.0/flim_example_a1_roi.png
    :width: 49%

.. image:: https://media.imagej.net/scijava-ops/1.0.0/flim_example_tau1_roi.png
    :width: 49%

.. image:: https://media.imagej.net/scijava-ops/1.0.0/flim_example_pseudocolored_roi.png
    :width: 49%


.. tabs::

    .. code-tab:: scijava-groovy

        #@ OpEnvironment ops
        #@ ROIService roiService
        #@ Img input
        #@ Float (description="The total time (ns) (timeBase in metadata)", label = "Time Base") timeBase
        #@ Integer (description="The number of time bins (timeBins in metadata)", label = "Time Bins") timeBins
        #@ Integer (description="The index of the lifetime axis (from metadata)", label = "Lifetime Axis", value=2) lifetimeAxis
        #@ Float (description="The minimal pixel intensity (across all time bins) threshold for fitting", label = "Intensity Threshold") iThresh
        #@ Integer (description="The radius of the binning kernel", label = "Bin Kernel Radius", value=0, min=0) kernelRad
        #@OUTPUT Img A1
        #@OUTPUT Img Tau1
        #@OUTPUT Img pseudocolored

        import net.imglib2.roi.Regions
        import java.lang.System

        // Utility function to collapse all ROIs into a single mask for FLIM fitting
        def getMask() {
            // No ROIs
            if (!roiService.hasROIs(input)) {
                return null
            }
            // 1+ ROIs
            rois = roiService.getROIs(input)
            mask = rois.children()remove(0).data()
            for(roi: rois.children()) {
                mask = mask.or(roi.data())
            }
            return mask;
        }

        import net.imglib2.type.numeric.real.DoubleType
        def getPercentile(img, mask, percentile) {
            if (mask != null) {
                img = Regions.sampleWithRealMask(mask, img)
            }
            return ops.op("stats.percentile")
                .input(img, percentile)
                .outType(DoubleType.class)
                .apply()
                .getRealFloat()
        }

        start = System.currentTimeMillis()

        // The FitParams contain a set of reasonable defaults for FLIM curve fitting
        import org.scijava.ops.flim.FitParams
        param = new FitParams()
        param.transMap = input
        param.ltAxis = lifetimeAxis
        param.iThresh = iThresh
        // xInc is the difference (ns) between two bins
        param.xInc = timeBase / timeBins

        // Fit curves
        kernel = ops.op("create.kernelSum").input(1 + 2 * kernelRad).apply()
        lma = ops.op("flim.fitLMA").input(param, getMask(), kernel).apply()

        // The fit results paramMap is a XYC image, with result attributes along the Channel axis
        fittedImg = lma.paramMap
        // For LMA, we have Z, A1, and Tau1 as the three attributes
        A1 = ops.op("transform.hyperSliceView").input(fittedImg, lifetimeAxis, 1).apply()
        Tau1 = ops.op("transform.hyperSliceView").input(fittedImg, lifetimeAxis, 2).apply()

        // Finally, generate a pseudocolored result
        cMin = getPercentile(Tau1, mask, 5.0)
        cMax = getPercentile(Tau1, mask, 95.0)
        pseudocolored = ops.op("flim.pseudocolor").input(lma, cMin, cMax).apply()

        end = System.currentTimeMillis()
        println("Finished fitting in " + (end - start) + " milliseconds")

.. _`Bio-Formats` : https://www.openmicroscopy.org/bio-formats/
.. _`FLIM` : https://en.wikipedia.org/wiki/Fluorescence-lifetime_imaging_microscopy
.. _`FluoCells™ Prepared Slide #1` : https://www.thermofisher.com/order/catalog/product/F36924
.. _`FRET` : https://en.wikipedia.org/wiki/F%C3%B6rster_resonance_energy_transfer
.. _`Jenu Chacko` : https://loci.wisc.edu/staff/chacko-jenu/
.. _`Levenberg-Marquardt algorithm` : https://en.wikipedia.org/wiki/Levenberg%E2%80%93Marquardt_algorithm
.. _`Openscan-LSM` : https://github.com/openscan-lsm
