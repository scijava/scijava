=============================================
Richardson-Lucy Total Variation Deconvolution
=============================================

In this example we will use SciJava Ops to perform Richardson-Lucy (RL) deconvolution on a 3D dataset (X, Y, Z) of
a HeLa cell nucleus stained with DAPI (4′,6-diamidino-2-phenylindole) and imaged on an epifluorescent microscope at 100x.
The SciJava Ops framework currently supports the standard RL algorithm as well as the Richardson-Lucy Total Variation (RLTV)
algorithm, which utilizes a regularization factor to limit the noise amplified by the RL algorithm :sup:`1`. Typically,
the RLTV algorithm returns improved axial and lateral resolution when compared to RL.

You can download the 3D HeLa cell nuclus dataset `here`_.

.. figure:: https://media.scijava.org/scijava-ops/1.0.0/rltv_example_1.gif

    Results of RLTV deconvolution on the sample data.

RLTV parameter descriptions
===========================

The table below contains the necessary parameter values needed for the ``kernelDiffraction`` Op to create the synthetic
point spread function (PSF) using the Gibson-Lanni model :sup:`2` for the sample HeLa cell nucleus dataset.

+--------------------------------------+-------+
| Parameter                            | Value |
+======================================+=======+
| Iterations                           | 15    |
+--------------------------------------+-------+
| Numerical aperature                  | 1.45  |
+--------------------------------------+-------+
| Emission wavelength (nm)             | 457   |
+--------------------------------------+-------+
| Refractive index (Immersion)         | 1.5   |
+--------------------------------------+-------+
| Refractive index (Sample)            | 1.4   |
+--------------------------------------+-------+
| Lateral resolution (μm/pixel)        | 0.065 |
+--------------------------------------+-------+
| Axial resolution (μm/pixel)          | 0.1   |
+--------------------------------------+-------+
| Particle/sample position (μm/pixel)  | 0.0   |
+--------------------------------------+-------+
| Regularization factor                | 0.002 |
+--------------------------------------+-------+

SciJava Ops via Fiji's scripting engine with `script parameters`_:

.. tabs::

    .. code-tab:: scijava-groovy

        #@ OpEnvironment ops
        #@ ImgPlus img
        #@ Integer iterations(label="Iterations", value=30)
        #@ Float numericalAperture(label="Numerical Aperture", style="format:0.00", min=0.00, value=1.45)
        #@ Integer wavelength(label="Emission Wavelength (nm)", value=550)
        #@ Float riImmersion(label="Refractive Index (immersion)", style="format:0.00", min=0.00, value=1.5)
        #@ Float riSample(label="Refractive Index (sample)", style="format:0.00", min=0.00, value=1.4)
        #@ Float lateral_res(label="Lateral resolution (μm/pixel)", style="format:0.0000", min=0.0000, value=0.065)
        #@ Float axial_res(label="Axial resolution (μm/pixel)", style="format:0.0000", min=0.0000, value=0.1)
        #@ Float pZ(label="Particle/sample Position (μm)", style="format:0.0000", min=0.0000, value=0)
        #@ Float regularizationFactor(label="Regularization factor", style="format:0.00000", min=0.00000, value=0.002)
        #@output ImgPlus psf
        #@output ImgPlus result

        import net.imglib2.FinalDimensions
        import net.imglib2.type.numeric.real.FloatType
        import net.imglib2.type.numeric.complex.ComplexFloatType

        // convert input image to float
        img_float = ops.op("create.img").input(img, new FloatType()).apply()
        ops.op("convert.float32").input(img).output(img_float).compute()

        // use image dimensions for PSF size
        psf_size = new FinalDimensions(img.dimensionsAsLongArray())

        // convert the input parameters to meters (m)
        wavelength = wavelength.toFloat() * 1E-9
        lateral_res = lateral_res * 1E-6
        axial_res = axial_res * 1E-6
        pZ = pZ * 1E-6

        // create the synthetic PSF
        psf = ops.op("create.kernelDiffraction").input(psf_size,
                                                       numericalAperture,
                                                       wavelength,
                                                       riSample,
                                                       riImmersion,
                                                       lateral_res,
                                                       axial_res,
                                                       pZ,
                                                       new FloatType()).apply()

        // deconvolve image
        result = ops.op("deconvolve.richardsonLucyTV").input(img_float, psf, new FloatType(), new ComplexFloatType(), iterations, false, false, regularizationFactor).apply()

    .. code-tab:: python

        #@ OpEnvironment ops
        #@ ImgPlus img
        #@ Integer iterations(label="Iterations", value=30)
        #@ Float numericalAperture(label="Numerical Aperture", style="format:0.00", min=0.00, value=1.45)
        #@ Integer wavelength(label="Emission Wavelength (nm)", value=550)
        #@ Float riImmersion(label="Refractive Index (immersion)", style="format:0.00", min=0.00, value=1.5)
        #@ Float riSample(label="Refractive Index (sample)", style="format:0.00", min=0.00, value=1.4)
        #@ Float lateral_res(label="Lateral resolution (μm/pixel)", style="format:0.0000", min=0.0000, value=0.065)
        #@ Float axial_res(label="Axial resolution (μm/pixel)", style="format:0.0000", min=0.0000, value=0.1)
        #@ Float pZ(label="Particle/sample Position (μm)", style="format:0.0000", min=0.0000, value=0)
        #@ Float regularizationFactor(label="Regularization factor", style="format:0.00000", min=0.00000, value=0.002)
        #@output ImgPlus psf
        #@output ImgPlus result

        from net.imglib2 import FinalDimensions
        from net.imglib2.type.numeric.real import FloatType
        from net.imglib2.type.numeric.complex import ComplexFloatType

        # convert input image to float
        img_float = ops.op("create.img").input(img, FloatType()).apply()
        ops.op("convert.float32").input(img).output(img_float).compute()

        # use image dimensions for PSF size
        psf_size = FinalDimensions(img.dimensionsAsLongArray())

        # convert the input parameters to meters (m)
        wavelength = float(wavelength) * 1E-9
        lateral_res = lateral_res * 1E-6
        axial_res = axial_res * 1E-6
        pZ = pZ * 1E-6

        # create the synthetic PSF
        psf = ops.op("create.kernelDiffraction").input(psf_size,
                                                                numericalAperture,
                                                                wavelength,
                                                                riSample,
                                                                riImmersion,
                                                                lateral_res,
                                                                axial_res,
                                                                pZ,
                                                                FloatType()).apply()

        # deconvolve image
        result = ops.op("deconvolve.richardsonLucyTV").input(img_float, psf, FloatType(), ComplexFloatType(), iterations, False, False, regularizationFactor).apply()

| :sup:`1`: `Dey et. al, Micros Res Tech 2006`_
| :sup:`2`: `Gibson & Lanni, JOSA 1992`_

.. _`Dey et. al, Micros Res Tech 2006`: https://pubmed.ncbi.nlm.nih.gov/16586486/
.. _`Gibson & Lanni, JOSA 1992`: https://pubmed.ncbi.nlm.nih.gov/1738047/
.. _`here`: https://media.scijava.org/scijava-ops/1.0.0/hela_nucleus.tif
.. _`script parameters`: https://imagej.net/scripting/parameters
