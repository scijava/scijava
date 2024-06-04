=========================
Gaussian Blur Subtraction
=========================

In this example we will use SciJava Ops to open an image, apply a gaussian blur and subtract the blurred image from the input image.
This technique can be used to extract features, such as puncta, from a noisy background.

Here is a script using `script parameters`_, runnable in Fiji's `Script Editor`_:

.. tabs::

    .. code-tab:: scijava-groovy

        #@ ImgPlus img
        #@ Double (label="Sigma:", value=5.0) sigma
        #@output ImgPlus result

        import org.scijava.ops.api.OpEnvironment
        import net.imglib2.type.numeric.real.FloatType 

        // build the Ops environment
        ops = OpEnvironment.build();

        // convert input ImgPlus image to float32
        img = ops.op("convert.float32").input(img).apply();

        // create gaussian blurred image
        img_gauss = ops.op("filter.gauss").input(img, sigma).apply();

        // subtract the input and blurred images
        result = ops.op("create.img").input(img, new FloatType()).apply();
        ops.op("math.sub").input(img, img_gauss).output(result).compute();

    .. code-tab:: python

        #@ ImgPlus img
        #@ Double (label="Sigma:", value=5.0) sigma
        #@output ImgPlus result

        from org.scijava.ops.api import OpEnvironment
        from net.imglib2.type.numeric.real import FloatType

        # build the Ops environment
        ops = OpEnvironment.build()

        # convert input ImgPlus image to float32
        img = ops.op("convert.float32").input(img).apply()

        # create gaussian blurred image
        img_gauss = ops.op("filter.gauss").input(img, sigma).apply()

        # subtract the input and blurred images
        result = ops.op("create.img").input(img, FloatType()).apply()
        ops.op("math.sub").input(img, img_gauss).output(result).compute()

.. _`script parameters`: https://imagej.net/scripting/parameters
.. _`Script Editor`: https://imagej.net/scripting/script-editor
