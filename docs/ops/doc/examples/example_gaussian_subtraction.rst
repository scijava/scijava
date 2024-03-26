=========================
Gaussian blur subtraction
=========================

In this example we will use SciJava Ops to open an image, apply a guassian blur and subract the blurred image from the input image.
This technique can be used to extract features, such as puncta, from a noisy background.

SciJava Ops via Fiji's sripting engine with `script parameters`_:

.. tabs::

    .. code-tab:: fijigroovy

        #@ ImgPlus img
        #@ Double (label="Sigma:", value=5.0) sigma
        #@output ImgPlus result

        import org.scijava.ops.api.OpEnvironment
        import net.imglib2.type.numeric.real.FloatType 

        // build the Ops environment
        ops = OpEnvironment.build();

        // convert input ImgPlus image to float32
        img = ops.op("convert.float32").arity1().input(img).apply();

        // create gaussian blurred image
        img_gauss = ops.op("filter.gauss").arity2().input(img, sigma).apply();

        // subtract the input and blurred images
        result = ops.op("create.img").arity2().input(img, new FloatType()).apply();
        ops.op("math.sub").arity2().input(img, img_gauss).output(result).compute();

    .. code-tab:: python

        #@ ImgPlus img
        #@ Double (label="Sigma:", value=5.0) sigma
        #@output ImgPlus result

        from org.scijava.ops.api import OpEnvironment
        from net.imglib2.type.numeric.real import FloatType

        # build the Ops environment
        ops = OpEnvironment.build()

        # convert input ImgPlus image to float32
        img = ops.op("convert.float32").arity1().input(img).apply()

        # create gaussian blurred image
        img_gauss = ops.op("filter.gauss").arity2().input(img, sigma).apply()

        # subtract the input and blurred images
        result = ops.op("create.img").arity2().input(img, FloatType()).apply()
        ops.op("math.sub").arity2().input(img, img_gauss).output(result).compute()

.. _`script parameters`: https://imagej.net/scripting/parameters 