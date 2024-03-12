========================================
Fast Non-Local Means Denoise with OpenCV
========================================

In this example we will use a denoise algorithm defined in an `external libray`_: OpenCV.

This method progressively scans neighborhoods of an image looking for repeated search template. These
repeated areas can then be averaged to eliminate gaussian noise, without the requirement of additional
images for comparison.

Sample data can be found in the `images folder`_.

SciJava Ops via Fiji's sripting engine with `script parameters`_:

.. tabs::

    .. code-tab:: groovy
        #@ ImgPlus img
        #@ Integer (label="strength:", value=4.0) strength
        #@ Integer (label="template size:", value=7) template
        #@ Integer (label="search size:", value=21) search
        #@output ImgPlus result

        import org.scijava.ops.api.OpEnvironment;
        import net.imglib2.type.numeric.integer.UnsignedByteType;

        // build the Ops environment
        ops = OpEnvironment.build();

        // Get the min and max values of our input image
        oldMin = ops.unary("stats.min").input(img).apply();
        oldMax = ops.unary("stats.max").input(img).apply();

        // We need to convert to 8-bit since not all data types are currently supported in OpenCV
        var type = new UnsignedByteType(100);
        var img8bit = ops.binary("create.img").input(img, type).apply();

        // Normalize our input data to the 8-bit min/max
        newMin = new UnsignedByteType((int)type.getMinValue());
        newMax = new UnsignedByteType((int)type.getMaxValue());

        ops.op("image.normalize").arity5().input(img, oldMin, oldMax, newMin, newMax).output(img8bit).compute()

        // Create a container for the denoise output
        result = img8bit.copy()

        // Run the denoise op
        ops.quaternary("filter.denoise").input(img8bit, strength, template, search).output(result).compute();
.. _`script parameters`: https://imagej.net/scripting/parameters
.. _`external libray`: https://docs.opencv.org/4.x/d5/d69/tutorial_py_non_local_means.html
.. _`images folder`: https://github.com/scijava/incubator/tree/main/docs/ops/images/sample_16bit_T24.png
