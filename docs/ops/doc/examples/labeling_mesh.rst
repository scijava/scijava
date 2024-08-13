======================================
3D Labeling and Mesh Geometry Analysis
======================================

In this example we will use SciJava Ops to segment puncta only in the nuclear space and create individual
meshes for each isolated puncta with geomtry measurements (*e.g.* sphericity). This example uses a dataset consisting of
HeLa cells expressing HIV-1 :sub:`NL4-3` Vif protein (puncta) and stained with DAPI (4′,6-diamidino-2-phenylindole, nuclei).
The dataset was collected on an epifluorescent microscope at 1.45NA 100x magnification (oil immersion) with 0.1 μm step size between
slices.

Using SciJava Ops to segment 3D puncta
--------------------------------------

The goal of this script is to segment and measure the 3D volume of the individual Vif puncta, but only within the nuclear space. In order
to accomplish this analysis goal the script utilizes SciJava Ops to:

1. Extract the selected channels from the open dataset.
2. Performs puncta and nucleus specific image processing steps on the extract channels. *Note: these are intended as example, do your own clean up steps for your data instead!*
3. Extracts a binary mask of only the puncta from the nuclear mask regions using logical operations (*i.e.* OR, AND, XOR, *etc*...).
4. Create label images and meshes for each puncta within a nuclear region.
5. Measure the 3D geometry of the puncta and nuclear regions.
6. Display results in two tables (puncta and nuclear).

To use the script in the example first download the sample dataset `here`_. Next, open Fiji, the sample image and this scipt. Click **Run** to create the script parameter GUI,
where you can customize some values to your own data, such as the channel names, channel position and image calibration values.

.. figure:: https://media.scijava.org/scijava-ops/1.0.1/labeling_mesh_example_dialog.png

Once the script has been configured click **OK** to start the analysis. The script will display two tables (one for each channel with the given channel name) and the labeling
output for the puncta within nuclear regions only.

.. figure:: https://media.scijava.org/scijava-ops/1.0.1/labeling_mesh_example_1.gif


   Results of 3D nuclear puncta segmentation on the sample data.


Script parameter descriptions
-----------------------------

This example script uses 8 different customizable parameters that impact the two results tables produced (one for each channel). Modify the values in the **Channel settings** section with the names and channel posistion numbers respectively.
The **Image calibration** setting values are determined at acquisition time of the dataset. The sample HIV Vif data used in this example has HIV :sub:`NL4-3` Vif in the first channel, DAPI stained nuclei in the second,
and a pixel width and height of 0.0650 μm with a step size of 0.1 μm (see the table below).

+--------------------+---------+
| Parameter          | Value   |
+====================+=========+
| Channel A name     | Vif     |
+--------------------+---------+
| Channel B name     | Nucleus |
+--------------------+---------+
| Channel A position | 1       |
+--------------------+---------+
| Channel B position | 2       |
+--------------------+---------+
| Pixel width (μm)   | 0.0650  |
+--------------------+---------+
| Pixel height (μm)  | 0.0650  |
+--------------------+---------+
| Voxel depth (μm)   | 0.1000  |
+--------------------+---------+

.. tabs::

   .. code-tab:: python

        #@ OpEnvironment ops
        #@ UIService ui
        #@ ImgPlus img
        #@ String (visibility = MESSAGE, value = "<b>Channel settings</b>", required = false) ch_msg
        #@ String (label = "Channel A name", value = "Vif") ch_a_name
        #@ String (label = "Channel B name", value = "Nuclei") ch_b_name
        #@ Integer (label = "Channel A position", value = 1) ch_a
        #@ Integer (label = "Channel B position", value = 2) ch_b
        #@ String (visibility = MESSAGE, value = "<b>Image calibration</b>", required = false) cal_msg
        #@ Float (label = "Pixel width (um)", style = "format:0.0000", value = 0.065) x_cal
        #@ Float (label = "Pixel height (um)", style = "format:0.0000", value = 0.065) y_cal
        #@ Float (label = "Voxel depth (um)", style = "format:0.0000", value = 0.1) z_cal
        
        from net.imglib2.algorithm.labeling.ConnectedComponents import StructuringElement
        from net.imglib2.algorithm.neighborhood import HyperSphereShape
        from net.imglib2.roi import Regions
        from net.imglib2.roi.labeling import LabelRegions
        from net.imglib2.type.logic import BitType
        from net.imglib2.type.numeric.real import FloatType
        
        from org.scijava.table import DefaultGenericTable
        
        
        def extract_channel(image, ch):
            """Extract a channel from the input image.
        
            Extract the given channel from the input image.
        
            :param image:
        
                Input Img.
        
            :param ch:
        
                Channel number to extract.
        
            :return:
        
                A view of the extracted channel.
            """
            # find C and Z axis indicies
            c_idx = find_axis_index(image, "Channel")
        
            return ops.op("transform.hyperSliceView").input(image, c_idx, ch - 1).apply()
        
        
        def extract_inside_mask(mask_a, mask_b):
            """Extract the mask "A" data from regions inside mask "B".
        
            Extract the mask "A" data from regions inside mask "B" using
            logical operations.
        
            :param mask_a:
        
                Input mask "A", data to extract.
        
            :param mask_b:
        
                Input mask "B", region to extract from.
        
            :return:
            
                Mask with extracted "B" region with "A" data.
            """
            # create Img containers
            tmp = ops.op("create.img").input(mask_a, BitType()).apply()
            out = ops.op("create.img").input(mask_a, BitType()).apply()
        
            # perform logical operations on masks
            ops.op("logic.or").input(mask_a, mask_b).output(tmp).compute()
            ops.op("logic.xor").input(tmp, mask_b).output(out).compute()
            ops.op("copy.img").input(out).output(tmp).compute()
            ops.op("logic.xor").input(tmp, mask_a).output(out).compute()
        
            return out
        
        def find_axis_index(image, axis_label):
            """Find the index of the given axis label.
        
            Find the axis index of the given axis label. If no
            label match is found, return None.
        
            :param image:
        
                Input Img.
        
            :param axis_label:
        
                Axis label to find.
        
            :return:
        
                The index of the given axis label in the image.
            """
            for i in range(len(image.dimensionsAsLongArray())):
                if axis_label == image.axis(i).type().toString():
                    return i
                else:
                    continue
        
            return None
        
        
        def gaussian_subtraction(image, sigma):
            """Perform a Gaussian subtraction on an image.
        
            Apply a Gaussian blur and subtract from input image.
        
            :param image:
        
                Input Img.
        
            :param sigma:
        
                Sigma value.
        
            :return:
        
                Gaussian blur subtracted image.
            """
            blur = ops.op("filter.gauss").input(image, sigma).apply()
            out = ops.op("create.img").input(image, FloatType()).apply()
            ops.op("math.sub").input(image, blur).output(out).compute()
        
            return out
        
        # extract channels
        ch_a_img = extract_channel(img, ch_a)
        ch_b_img = extract_channel(img, ch_b)
        
        # customize the following sections below for your own data
        # clean up channel "A" and create a mask
        ch_a_img = gaussian_subtraction(ch_a_img, 8.0)
        ch_a_ths = ops.op("create.img").input(ch_a_img, BitType()).apply()
        ops.op("threshold.triangle").input(ch_a_img).output(ch_a_ths).compute()
        ch_a_mask = ops.op("morphology.open").input(ch_a_ths, HyperSphereShape(1), 4).apply()
        
        # clean up channel "B" and create a mask
        ch_b_ths= ops.op("create.img").input(ch_b_img, BitType()).apply()
        ops.op("threshold.otsu").input(ch_b_img).output(ch_b_ths).compute()
        ch_b_mask = ops.op("morphology.open").input(ch_b_ths, HyperSphereShape(2), 4).apply()
        
        # extract mask "A" data from mask "B" region
        ch_ab_mask = extract_inside_mask(ch_a_mask, ch_b_mask)
        
        # create ImgLabelings from masks
        ab_labeling = ops.op("labeling.cca").input(ch_ab_mask, StructuringElement.EIGHT_CONNECTED).apply()
        b_labeling = ops.op("labeling.cca").input(ch_b_mask, StructuringElement.EIGHT_CONNECTED).apply()
        
        # create a table for the "AB" mask and make mesurements
        ab_table = DefaultGenericTable(3, 0)
        ab_table.setColumnHeader(0, "{} size (pixels)".format(ch_a_name))
        ab_table.setColumnHeader(1, "{} volume (um^3)".format(ch_a_name))
        ab_table.setColumnHeader(2, "{} sphericity".format(ch_a_name))
        ab_regs = LabelRegions(ab_labeling)
        i = 0
        for r in ab_regs:
            # create a sample of mask "A" data in "B" region
            sample = Regions.sample(r, ch_ab_mask)
            # create a crop needed to create a mesh
            crop = ops.op("transform.intervalView").input(
                    ch_ab_mask,
                    r.minAsDoubleArray(),
                    r.maxAsDoubleArray()
                    ).apply()
            mesh = ops.op("geom.marchingCubes").input(crop).apply()
            ab_table.appendRow()
            # measure mesh/sample geometry and stats
            ab_table.set("{} size (pixels)".format(ch_a_name), i, ops.op("stats.size").input(sample).apply())
            ab_table.set("{} volume (um^3)".format(ch_a_name), i, ops.op("geom.size").input(mesh).apply().getRealFloat() * (x_cal * y_cal * z_cal))
            ab_table.set("{} sphericity".format(ch_a_name), i, ops.op("geom.sphericity").input(mesh).apply())
            i += 1
        # create a table for the "B" mask and make measurements
        b_table = DefaultGenericTable(3, 0)
        b_table.setColumnHeader(0, "{} size (pixels)".format(ch_b_name))
        b_table.setColumnHeader(1, "{} volume (um^3)".format(ch_b_name))
        b_table.setColumnHeader(2, "{} sphericity".format(ch_b_name))
        b_regs = LabelRegions(b_labeling)
        j = 0
        for r in b_regs:
            # create a sample of mask "B" data in "B" region
            sample = Regions.sample(r, ch_b_mask)
            # create a crop needed to create a mesh
            crop = ops.op("transform.intervalView").input(
                    ch_b_mask,
                    r.minAsDoubleArray(),
                    r.maxAsDoubleArray()
                    ).apply()
            mesh = ops.op("geom.marchingCubes").input(crop).apply()
            b_table.appendRow()
            # measure mesh/sample geometry and stats
            b_table.set("{} size (pixels)".format(ch_b_name), j, ops.op("stats.size").input(sample).apply())
            b_table.set("{} volume (um^3)".format(ch_b_name), j, ops.op("geom.size").input(mesh).apply().getRealFloat() * (x_cal * y_cal * z_cal))
            b_table.set("{} sphericity".format(ch_b_name), j, ops.op("geom.sphericity").input(mesh).apply())
            j += 1
        
        # display results tables and labeling image
        ui.show(ab_labeling.getIndexImg())
        ui.show("{} results table".format(ch_a_name), ab_table)
        ui.show("{} results table".format(ch_b_name), b_table)

.. _`here`: https://media.scijava.org/scijava-ops/1.0.0/hela_hiv_vif.tif
