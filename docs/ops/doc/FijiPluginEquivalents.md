# Fiji Plugin Equivalents

Many classic ImageJ and Fiji plugins have equivalent Ops. This page maps each plugin to its Ops;
the javadoc of each Op details its differences from the plugin. Ops marked *GPL* or *LGPL* live in the
`scijava-ops-image-gpl` or `scijava-ops-image-lgpl` components, since they derive from code under
those licenses; all other Ops are in `scijava-ops-image`.

"Same results" means the Op reproduces the plugin's output exactly on our test images; most Ops also
generalize the plugin to n-dimensional images and calibrated pixel sizes.

| Plugin | Ops | Notes |
|---|---|---|
| Auto Threshold | `threshold.<method>` | Same results, including Huang and Huang2. |
| Auto Local Threshold | `threshold.local<Method>` | Same results. |
| 3D ImageJ Suite: Canny edge 3D | `filter.derichePartialDerivative`, `filter.dericheGradientMagnitude`, `segment.cannyDericheEdges` | Normalized, separable Deriche filter; magnitudes differ by a constant factor. |
| 3D ImageJ Suite: 3D Spot Segmentation | `segment.spotsFromSeeds` (*GPL*) | Same results; no watershed option. |
| Anisotropic Diffusion 2D | — | Not ported; see `filter.peronaMalik` and `filter.coherenceEnhancingDiffusion`. |
| Bleach Correction | `image.bleachCorrect`, `image.bleachFit`, `image.histogramMatch` | |
| BioVoxxel Toolbox | `filter.pseudoFlatField`, `filter.flatFieldCorrect`, `filter.convolvedBackgroundSubtraction`, `filter.gaussianWeightedMedian`, `labeling.edgeCorrectedCount`, `labeling.clarkEvans`; `threshold.limitedMean`, `labeling.countContained` (*GPL*) | |
| CircleSkinner | `transform.houghCircle`, `segment.detectHoughCircles` | Votes on full rings rather than midpoint circles. |
| Coloc 2 | `coloc.pearsons`, `coloc.manders`, `coloc.costesThreshold`, `coloc.spearman`, `coloc.pValue` | Costes thresholds count a pixel equal to the threshold as below it. |
| Colour Deconvolution | `color.deconvolveStains` | Within one grey level. |
| Curvatures, Shape Index Map | `filter.hessianEigenvalues`, `filter.shapeIndex` | |
| Directionality | `features.directionalityHistogram` (*GPL*) | Same results for the Fourier method; the gradient method's half-bin bias is fixed. |
| Gaussian Stack Focuser | `transform.focusStack`, `transform.focusStackIndex` | |
| Graph Cut | `segment.graphCut` | Independent implementation of Boykov–Kolmogorov max-flow. |
| Helmholtz Analysis | `filter.helmholtzWavelength`, `filter.helmholtzOrientation` | Spatial instead of FFT band-pass; results differ slightly. |
| Interactive H-Watershed | `segment.hWatershed` | Same results on integer images. |
| Iterative Deconvolve 3D | `deconvolve.wiener`, `deconvolve.tikhonov`, `deconvolve.landweber` | Independent implementations. |
| Kalman Stack Filter | `filter.kalman` | Independent implementation of the documented algorithm. |
| Kuwahara Filter | `filter.kuwahara` | Same results away from the image border. |
| Linear Kuwahara | `filter.kuwaharaLinear` | Independent implementation. |
| KymographBuilder | `image.kymograph` | Samples the first row and column, which the plugin skips. |
| Level Sets | `segment.levelSet`, `segment.fastMarching` | Independent implementations (Chan–Vese, geodesic active contours). |
| Local Thickness | `morphology.localThickness` | Same results to 1e-5. |
| MorphoLibJ: morphological reconstruction, extrema, h-transforms, kill borders, fill holes | `morphology.reconstructBy*`, `morphology.regional*`, `morphology.extended*`, `morphology.h*`, `morphology.impose*`, `morphology.killBorders`, `morphology.fillHoles` | Same results. |
| MorphoLibJ: attribute filtering | `morphology.areaOpening`, `morphology.boxDiagonalOpening`, `morphology.attributeOpening` and closings | Same results. MorphoLibJ's "volume opening" is `morphology.areaOpening` on a 3D image. |
| MorphoLibJ: morphological segmentation | `segment.extendedMinimaWatershed`, `segment.compactWatershed` | Same results with watershed lines. |
| MorphoLibJ: distance transform watershed | `segment.distanceTransformWatershed` | Uses the exact Euclidean distance transform instead of chamfer. |
| MorphoLibJ: label utilities and label morphology | `labeling.*` (e.g. `removeBorderLabels`, `filterBySize`, `dilate`, `distanceMap`, `adjacency`) | Exact Euclidean distances instead of chamfer. |
| MorphoLibJ: geodesic distance, region analysis, label overlap | `morphology.geodesicDistanceMap`, `labeling.geodesicDiameter`, `labeling.inscribedBall`, `labeling.equivalentEllipsoid`, `labeling.maximumFeret`, `labeling.overlap.*` | |
| MorphoLibJ: intrinsic volumes (Analyze Regions) | `features.intrinsicVolumes.*` (*LGPL*) | Same results, except that interface area honors the pixel calibration. |
| OrientationJ | `filter.structureTensor`, `filter.orientation`, `filter.coherency` | |
| PHANTAST | `segment.phantast`, `filter.localCoefficientOfVariation` | Same results; two halo-correction bugs fixed. |
| PIV analyser | `image.piv` | |
| RadiomicsJ texture features | `features.glrlm.*`, `features.glszm.*`, `features.gldzm.*`, `features.ngtdm.*`, `features.ngldm.*` | Match the IBSI reference values. |
| RATS | `threshold.rats` | Independent implementation; results differ in detail. |
| Retinex | `filter.retinex` | Each scale is applied to the original image. |
| Ridge Detection | `segment.detectLines` (*GPL*) | Same results. |
| ROF Denoise | `filter.tvDenoise` | Chambolle's algorithm; borders differ. |
| SIOX Segmentation | `segment.siox` | Same results. |
| Skeletonize3D, AnalyzeSkeleton | `morphology.skeletonize`, `morphology.tagSkeleton`, `topology.skeletonGraph`, `topology.skeletonStatistics` (*GPL*) | Same results; fixes pruning after the root vertex is removed. |
| Statistical Region Merging | `segment.statisticalRegionMerging` | Same results on full-range 8-bit images. |
| Stitching (pairwise phase correlation) | `filter.phaseCorrelate` | Translation only. |
| Tubeness, Frangi Vesselness | `filter.tubeness`, `filter.frangiVesselness`, `filter.sheetness` | `filter.tubeness` differentiates in calibrated units, unlike the plugin. |
