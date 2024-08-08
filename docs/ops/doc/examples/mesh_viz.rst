=============================
3D Analysis and Visualization
=============================

In this example, we will use SciJava Ops to construct a 3D mesh from a binary dataset, passing the result into `3D Viewer`_ for visualization. We use the `bat cochlea volume`_ dataset from the ImageJ sample images, which users can either download using the link or open from the ``File → Open Samples → Bat Cochlea Volume`` menu selection within Fiji.

.. TODO: Add image

.. TODO: Update SciJava Ops Image -> imglib2-mesh

The following script accepts the binary dataset as its sole input, and creates the mesh using the `marching cubes`_ algorithm, which is included within SciJava Ops Image. We then use SciJava Ops to compute mesh volume, and then convert the mesh into a ``CustomTriangleMesh`` that can be passed to the 3DViewer.

.. tabs::

    .. code-tab:: scijava-groovy

        #@ OpEnvironment ops
        #@ UIService ui
        #@ Dataset image
        #@ ImagePlus imp
        #@ StatusService status

        import java.util.ArrayList
        import java.util.List

        import net.imagej.mesh.Mesh
        import net.imagej.mesh.Triangle
        import net.imglib2.RandomAccessibleInterval
        import net.imglib2.type.BooleanType
        import net.imglib2.util.Util

        import org.scijava.vecmath.Point3f

        import customnode.CustomTriangleMesh
        import ij3d.Image3DUniverse

        if (image.getType() instanceof BooleanType) {
            // Input image is a binary image.
            mask = image
        }
        else {
            // Binarize the image using Otsu's threshold.
            status.showStatus("Thresholding...")
            bitType = ops.op("create.bit").producer().create()
            mask = ops.op("create.img").input(image, bitType).apply()
            ops.op("threshold.otsu").input(image).output(mask).compute()
        }
        println("Mask = $mask [type=${Util.getTypeFromInterval(mask).getClass().getName()}]")

        //ui.show(mask)

        // Compute surface mesh using marching cubes.
        status.showStatus("Computing surface...")
        mesh = ops.op("geom.marchingCubes").input(mask).apply()
        println("mesh = ${mesh} [${mesh.triangles().size()} triangles, ${mesh.vertices().size()} vertices]")

        meshVolume = ops.op("geom.size").input(mesh).apply().getRealDouble()
        println("mesh volume = " + meshVolume)

        hull = ops.op("geom.convexHull").input(mesh).apply()
        println("hull = ${hull} [${hull.triangles().size()} triangles, ${hull.vertices().size()} vertices]")

        hullVolume = ops.op("geom.size").input(hull).apply().getRealDouble()
        println("hull volume = $hullVolume")

        // Display original image and meshes in 3D Viewer.

        def opsMeshToCustomMesh(opsMesh) {
            points = []
            for (t in hull.triangles()) {
                points.add(new Point3f(t.v0xf(), t.v0yf(), t.v0zf()))
                points.add(new Point3f(t.v1xf(), t.v1yf(), t.v1zf()))
                points.add(new Point3f(t.v2xf(), t.v2yf(), t.v2zf()))
            }
            return new CustomTriangleMesh(points)
        }

        mesh_hull = opsMeshToCustomMesh(hull)
        println("Hull volume according to 3D Viewer: ${mesh_hull.getVolume()}");

        univ = new Image3DUniverse()
        univ.addVoltex(imp, 1)
        univ.addCustomMesh(mesh_hull, "Convex Hull")
        univ.show()

.. _3D Viewer: https://imagej.net/plugins/3d-viewer/
.. _bat cochlea volume: https://imagej.net/images/bat-cochlea-volume.zip
.. _marching cubes: https://en.wikipedia.org/wiki/Marching_cubes