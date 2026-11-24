# Fiji Plugin Equivalents

Many classic ImageJ and Fiji plugins have equivalent Ops. This page maps each plugin to its Ops,
says how each Op was written, and how closely its results match the plugin's. The javadoc of each
Op has the full details, including the original authors and license notices.

## How to read the tables

**Origin** says how each Op relates to the plugin's code:

- **Port**: translated from the plugin's source code. A port keeps the original copyright and
  license notice in its javadoc, and that license decides where the Op lives. Ports of GPL code
  are in `scijava-ops-image-gpl` and ports of LGPL code in `scijava-ops-image-lgpl`. Ports of
  permissive code (BSD, MIT, Apache) are in `scijava-ops-image`, which is BSD-2 licensed.
- **Clean-room**: written from published papers or documentation, without reading the plugin's
  source. "Tested against the plugin" means the plugin was run as a black box only, to produce
  the expected values of the tests. "Not consulted" means the plugin was neither read nor run.
  All clean-room Ops are in `scijava-ops-image`.
- **Wrapper**: exposes an existing, permissively licensed ImgLib2 algorithm.

**Results** compares each Op with the plugin:

- **Same results**: the Op reproduces the plugin's output exactly on the test images (or within
  the stated tolerance).
- **Differs**: known deviations, either by design or caused by a different algorithm.
- **Fixes**: bugs of the plugin that the Op does not reproduce.
- **Not compared**: no test checks the Op against the plugin.

Most Ops also generalize the plugin to n-dimensional images and calibrated pixel sizes. The tables
mention this only where it changes the results.

## Thresholding

| Plugin | Ops | Origin | Results |
|---|---|---|---|
| [Auto Threshold](https://github.com/fiji/Auto_Threshold) (GPL-3) | `threshold.<method>`, `threshold.huang2` | Existing Ops, corrected to match the plugin. `threshold.huang` is now a clean-room version of Huang & Wang (1995), tested against the plugin; the previous code, adapted from Auto_Threshold 1.14, is now `threshold.huang2`. | **Same results** on 8- and 16-bit test images, including two-level images and `ignoreBlack`/`ignoreWhite`. To get there, integer images now use one histogram bin per grey level, `threshold.otsu` resolves ties toward the lowest bin, and `threshold.minError` handles non-convergence as the plugin does. |
| [Auto Local Threshold](https://github.com/fiji/Auto_Local_Threshold) (GPL-3) | `threshold.local<Method>` | Existing Ops, corrected and tested against the plugin. | **Same results** when the plugin's kernel equals `HyperSphereShape(r)`. Corrections that were needed: `localContrast` was inverted; the `localSauvola` formula and default `r` were wrong; `localPhansalkar` now stretches its input to [0, 1] (unless `normalize` is false); Niblack, Sauvola and Phansalkar now use the population standard deviation; Bernsen rounds the mid grey down on integer images; and rectangles larger than 3×3 no longer fail. |
| [RATS](https://github.com/fiji/RATS) (GPL-3) | `threshold.rats`, `threshold.ratsSurface` | Clean-room, written from the general description of the method; neither the plugin nor the original RATS code (non-commercial license) was consulted. | **Differs** in detail: the gradient operator, reliability criterion, root fallback and noise estimate are choices of this implementation. **Not compared.** |
| [BioVoxxel Toolbox](https://github.com/biovoxxel/BioVoxxel-Toolbox): Limited Mean (GPL-3 file header) | `threshold.limitedMean` (*GPL*) | Port | **Same results** on 8-bit images (tested against the plugin in macro mode). Other types use 256 bins over the data range. NaN pixels are excluded and are always background. |
| [MiToBo](https://github.com/mitobo-hub/mitobo) hysteresis thresholding (GPL) | `threshold.hysteresis` | Clean-room (Canny 1986); not consulted. | **Not compared.** |

## Filters and denoising

| Plugin | Ops | Origin | Results |
|---|---|---|---|
| [Anisotropic Diffusion 2D](https://github.com/fiji/Anisotropic_Diffusion_2D) (GPL-3) | `filter.peronaMalik`, `filter.coherenceEnhancingDiffusion` | Clean-room (Perona & Malik 1990; Weickert 1998–99). | **Differs**: these are related but different algorithms. The plugin's Tschumperlé–Deriche diffusion is not implemented. |
| [BioVoxxel Toolbox](https://github.com/biovoxxel/BioVoxxel-Toolbox) (BSD-3): Pseudo flat field correction | `filter.pseudoFlatField` | Port | **Differs**: the blur is n-dimensional and the arithmetic is floating-point rather than integer. The RGB (HSB brightness) branch is not ported. **Not compared.** |
| BioVoxxel Toolbox (BSD-3): Flat-field correction macro | `filter.flatFieldCorrect` | Port | **Differs**: adds an optional dark-field image. The RGB branch is not ported. **Not compared.** |
| BioVoxxel Toolbox (BSD-3): Convoluted Background Subtraction | `filter.convolvedBackgroundSubtraction` | Port | **Differs**: uses n-D hyperspheres (`d² ≤ r²`) instead of ImageJ's circular kernels (`d² ≤ r² + 1`), with a floating-point background. **Not compared.** |
| BioVoxxel Toolbox: Gaussian Weighted Median (GPL-3 file header) | `filter.gaussianWeightedMedian`, `filter.weightedMedian` | Clean-room, from the general definition of a Gaussian-weighted median. | **Differs**: results need not match the plugin. **Not compared.** |
| [Shape Index Map](https://github.com/fiji/Fiji_Plugins/blob/main/src/main/java/fiji/geom/Shape_Index_Map.java) (GPL-2), Curvatures ([VIB](https://github.com/fiji/VIB), GPL-3) | `filter.hessianEigenvalues`, `filter.shapeIndex` | Clean-room (Koenderink & van Doorn 1992); the GPL sources served only as behavioral references. | Same sign convention as Shape Index Map. **Differs**: uses Gaussian-smoothed central differences instead of one-sided differences. **Not compared.** |
| [OrientationJ](https://github.com/Biomedical-Imaging-Group/OrientationJ) (GPL-3) | `filter.structureTensor`, `filter.structureTensorEigenvalues`, `filter.orientation`, `filter.coherency` | Clean-room (Rezakhaniha et al. 2012; Püspöki et al. 2016); OrientationJ served only as a behavioral reference. | Same definitions as OrientationJ. **Differs**: gradients are always central differences of the smoothed image; OrientationJ's cubic-spline gradient is not offered. **Not compared.** |
| Tubeness, Surfaceness, Harris Corner Detector ([VIB](https://github.com/fiji/VIB), [VIB-lib](https://github.com/fiji/VIB-lib), GPL-3) | `filter.tubeness` (existing), `filter.sheetness`, `filter.harris` | Clean-room; VIB served only as a behavioral reference. | **Differs**: with a calibration, `filter.tubeness` differentiates in calibrated units, while VIB differentiates in pixel units, so VIB's result depends on the sampling. `filter.sheetness` uses Sato's sheet measure instead of VIB's Surfaceness ratio, which is unbounded and vanishes at the center of a plate. **Fixes**: `filter.harris` does not reproduce VIB's determinant bug (`gyy·gyy` instead of `gyy·gzz`). The `filter.tubeness` op itself used to ignore its calibration; this is fixed too. |
| [Helmholtz Analysis](https://github.com/fiji/Helmholtz_Analysis) (BSD-3) | `filter.helmholtzWavelength`, `filter.helmholtzOrientation` | Port | **Differs** slightly: the plugin band-pass filters with ImageJ's FFT filter, which autoscales and saturates; the Op uses the equivalent spatial Gaussians instead. Tested against a replication of the plugin's pipeline. |
| [Kalman Stack Filter](https://imagej.net/ij/plugins/kalman.html) (non-commercial license) | `filter.kalman` | Clean-room, from the algorithm documented for the plugin; the code was not consulted. | Implements the documented update, without the plugin's 16-bit and file-streaming limits. **Not compared.** |
| [Kuwahara Filter](https://github.com/fiji/Kuwahara_Filter) (GPL-3) | `filter.kuwahara` | Clean-room (Kuwahara et al. 1976), tested against the plugin. | **Same results** after rounding on 2D 8-bit images, away from the image border. |
| [Linear Kuwahara](https://github.com/fiji/Linear_Kuwahara) (GPL-3) | `filter.kuwaharaLinear` | Clean-room (Bakker, van Vliet & Verbeek 1999); not consulted. | **Differs**: lines are digital lines with linear interpolation, and borders use an out-of-bounds strategy instead of being copied. **Not compared.** |
| [Retinex](https://github.com/fiji/Fiji_Plugins/blob/main/src/main/java/Retinex_.java) (GPL-2) | `filter.retinex` | Clean-room (Jobson, Rahman & Woodell 1997); not consulted. | **Differs**: each scale's surround is applied to the original image, as in the paper. The plugin blurs its working buffer in place, so its scales accumulate. |
| [ROF Denoise](https://github.com/fiji/Fiji_Plugins/blob/main/src/main/java/fiji/denoise/ROF_Denoise.java) (GPL-2) | `filter.tvDenoise` | Clean-room (Chambolle 2004). | **Differs**: same algorithm with Neumann boundary conditions, so results differ near the image border and slightly elsewhere. |
| [thunderSTORM](https://github.com/zitmen/thunderstorm), [NanoJ-Core](https://github.com/HenriquesLab/NanoJ-Core) wavelet filters (GPL) | `filter.waveletATrous`, `filter.waveletDenoise` | Clean-room (Starck & Murtagh); not consulted. | **Not compared.** |
| [Iterative Deconvolve 3D](https://github.com/fiji/cookbook/blob/main/src/main/java/sc/fiji/cookbook/Iterative_Deconvolve_3D.java) (GPL-3) | `deconvolve.wiener`, `deconvolve.tikhonov`, `deconvolve.landweber` | Clean-room (Wiener 1949; Tikhonov & Arsenin 1977; Landweber 1951; Bertero & Boccacci); not consulted. | **Differs**: these complement the plugin rather than reproduce it. **Not compared.** |

## Morphology and skeletons

| Plugin | Ops | Origin | Results |
|---|---|---|---|
| [MorphoLibJ](https://github.com/ijpb/MorphoLibJ) (LGPL-3): reconstruction, extrema, h-transforms, kill borders, fill holes | `morphology.reconstructBy*`, `morphology.regional*`, `morphology.extended*`, `morphology.h*`, `morphology.impose*`, `morphology.killBorders`, `morphology.fillHoles` | Clean-room (Vincent 1993; Soille 2003), tested against MorphoLibJ 1.6.4. | **Same results.** NaN inputs are rejected. |
| MorphoLibJ: attribute filtering | `morphology.areaOpening`, `morphology.boxDiagonalOpening`, `morphology.grayVolumeOpening`, `morphology.attributeOpening` and the matching closings | Clean-room (Salembier et al. 1998; Najman & Couprie 2006; Berger et al. 2007), tested against MorphoLibJ 1.6.4. | **Same results** (2D area, 2D box diagonal, 3D size). MorphoLibJ's 3D "volume opening" is `morphology.areaOpening` on a 3D image; `grayVolume*` is the grayscale volume attribute of the literature. |
| MorphoLibJ: morphological gradients and Laplacian | `morphology.gradient`, `morphology.internalGradient`, `morphology.externalGradient`, `morphology.laplacian` | Clean-room (Soille 2003; van Vliet et al. 1989). | **Differs**: the Laplacian is not halved, and not offset for 8-bit images. **Not compared.** |
| MorphoLibJ: directional filtering | `morphology.directionalFilter` | Clean-room (Soille & Talbot 2001; Hendriks & van Vliet 2005); not consulted. | **Differs**: line segments are discretized differently, so results are not identical. |
| MorphoLibJ: geodesic distance and diameter | `morphology.geodesicDistanceMap`, `labeling.geodesicDiameter`, `labeling.geodesicDiameterPath` | Clean-room (Lantuéjoul & Beucher 1981; Legland & Beaugrand 2013), tested against MorphoLibJ 1.6.4. | **Differs** slightly: steps have exact Euclidean lengths, where MorphoLibJ uses chamfer weights. |
| [Local Thickness](https://github.com/fiji/LocalThickness) (algorithm BSD-3 by OptiNav; Fiji wrappers GPL-3) | `morphology.localThickness` | Port of the BSD-3 algorithm code only; the GPL wrapper classes were not used. | **Same results** with unit calibration: the test compares sums of the thickness map with the plugin's, to 1e-2. Also supports anisotropic calibration. |
| [Skeletonize3D](https://github.com/fiji/Skeletonize3D) (GPL-3) | `morphology.skeletonize` (*GPL*) | Port | **Same results** on 2D and 3D images. |
| [AnalyzeSkeleton](https://github.com/fiji/AnalyzeSkeleton) (GPL-3) | `morphology.tagSkeleton`, `topology.skeletonGraph`, `topology.skeletonStatistics` (*GPL*) | Port | **Same results** in all pruning modes, including the plugin's neighbor visit order. **Differs**: branch intensities are measured in the given intensity image, not the skeleton. **Fixes**: if end pruning removes a tree's root vertex, another vertex becomes the root, so cycle pruning still runs (the plugin silently prunes nothing); an end point whose only branch loops back to itself is pruned without error. |
| Implicit Interpolate Binary ([VIB](https://github.com/fiji/VIB), GPL-3) | `morphology.interpolateSlices`, `image.signedDistanceTransform` | Clean-room (Raya & Udupa 1990; Herman et al. 1992); not consulted. | **Differs**: uses exact Euclidean instead of chamfer distances. **Not compared.** |

## Segmentation

| Plugin | Ops | Origin | Results |
|---|---|---|---|
| [3D ImageJ Suite](https://framagit.org/mcib3d/mcib3d-core) (GPL-3): Canny edge 3D | `filter.derichePartialDerivative`, `filter.dericheGradientMagnitude`, `segment.cannyDericheEdges` | Clean-room (Deriche 1987; Monga et al. 1991), tested against mcib3d-core. | The 1D derivative matches the plugin's, up to a normalization constant. **Differs** in n-D: the plugin neither smooths along the other axes, nor normalizes the kernel, nor suppresses non-maxima, so its gradients differ by more than a constant factor. |
| 3D ImageJ Suite: 3D Spot Segmentation | `segment.spotsFromSeeds` (*GPL*) | Port | **Same results** against mcib3d-core 3.96.31, for all growth rules and threshold methods. **Differs**: radii are in pixels; there is no watershed option; the Gaussian fit has its own solver, so ill-conditioned fits may differ; `"diff"` uses the image value at the seed; a seed whose fit fails gives no spot, where the plugin reuses the previous seed's threshold; thresholds are not truncated on non-integer images. **Fixes**: pixels of a spot stopped by `maxVolume` are not reused by the next spot. |
| [CircleSkinner](https://github.com/tinevez/CircleSkinner) (BSD-2) | `transform.houghCircle`, `segment.detectHoughCircles` | Port | **Differs**: votes on discrete n-D spherical shells rather than 2D midpoint circles, and the circle thickness is fixed to 1 (divide the sensitivity by the thickness to reproduce the plugin's threshold). **Not compared.** |
| [Graph Cut](https://github.com/fiji/Graph_Cut) (GPL) | `segment.graphCut` | Clean-room (Boykov & Kolmogorov 2004); neither the plugin nor Kolmogorov's maxflow library was read. | **Differs**: the energy and parameters are this Op's own. **Not compared.** |
| [Interactive H-Watershed](https://github.com/mpicbg-scicomp/Interactive-H-Watershed) (BSD-3) | `segment.hWatershed` | Port | **Same results** on integer-valued 2D and 3D images. **Differs**: intensities are not truncated to integers, and the default connectivity is full (the plugin's is face). |
| [Level Sets](https://github.com/fiji/level_sets) (GPL-3) | `segment.levelSet`, `image.levelSetFunction`, `segment.fastMarching`, `image.fastMarchingTimes` | Clean-room (Chan & Vese 2001; Caselles, Kimmel & Sapiro 1997; Sethian 1996); not consulted. | **Differs**: the plugin's parameters do not carry over. **Not compared.** |
| MorphoLibJ (LGPL-3): morphological segmentation | `segment.extendedMinimaWatershed`, `segment.compactWatershed` | Clean-room (Meyer & Beucher 1990; Neubert & Protzel 2014), tested against MorphoLibJ 1.6.4. | **Same results** with watershed lines. Watershed lines are unlabeled unless `watershedLabel` is given. |
| MorphoLibJ: distance transform watershed | `segment.distanceTransformWatershed` | Clean-room. | **Differs**: uses the exact Euclidean distance transform instead of chamfer distances. **Not compared.** |
| [PHANTAST](https://github.com/nicjac/PHANTAST-FIJI) (BSD-3) | `segment.phantast`, `filter.localCoefficientOfVariation` | Port | **Same results**, apart from the fixes. **Fixes**: object removal and hole filling follow the sizes stated in the plugin's comments (remove objects under 100 pixels, fill holes under 25); the plugin swaps them, or inverts its mask, depending on ImageJ's "black background" setting. Both sizes are now parameters. Halo correction no longer skips the first queued pixel of each iteration, nor the first row and column. |
| [Ridge Detection](https://github.com/thorstenwagner/ij-ridgedetection) (GPL-2+) | `segment.detectLines`, `segment.estimateLineParameters` (*GPL*) | Port | **Same results.** **Differs**: line IDs restart at 0 for each detection; overlap resolution iterates in a deterministic order (the plugin uses identity hash order); estimated parameters are not rounded to two decimals. **Fixes**: lines longer than 100 points no longer lose the responses of their first points. |
| [SIOX Segmentation](https://github.com/fiji/Siox_Segmentation) (Apache-2.0) | `segment.siox` | Port | **Same results** on 2D ARGB images, apart from the fix. **Fixes**: a colour signature whose samples all fall into one cluster no longer averages in unused (black) buffer entries. |
| [Statistical Region Merging](https://github.com/fiji/Statistical_Region_Merging) (BSD-2) | `segment.statisticalRegionMerging` | Port | **Same results** on 8-bit images spanning 0–255. Other types are mapped linearly onto the g levels. Labels are the plugin's region indices plus one. |
| [ijp-toolkit](https://github.com/ij-plugins/ijp-toolkit) (LGPL-2.1): k-means, seeded region growing | `segment.kMeans`, `segment.seededRegionGrowing` | Clean-room (Lloyd 1982; Arthur & Vassilvitskii 2007; Adams & Bischof 1994; Mehnert & Jackway 1997); not consulted. | **Not compared.** |
| [Descriptor-based registration](https://github.com/fiji/Descriptor_based_registration), [TrackMate](https://github.com/trackmate-sc/TrackMate) DoG detector | `segment.detectSpotsDoG` | Wrapper of ImgLib2's `DogDetection` (BSD-2), on which both plugins build. | Same detector core. **Not compared.** |

## Labelings and measurements

| Plugin | Ops | Origin | Results |
|---|---|---|---|
| [MorphoLibJ](https://github.com/ijpb/MorphoLibJ) (LGPL-3): label utilities | `labeling.relabel`, `mergeLabels`, `fillHoles`, `cropLabel`, `filter`, `filterBySize`, `filterByValue`, `keepLabels`, `removeLabels`, `keepLargest`, `removeLargest`, `removeBorderLabels`, `boundaries`, `adjacency` | Clean-room, from the documented behavior. | **Differs**: overlapping labels are supported. `labeling.boundaries` marks the labeled pixels on both sides of a boundary, where MorphoLibJ marks only the pixel before each change. `labeling.adjacency` pairs labels that touch, where MorphoLibJ pairs labels separated by a one-pixel line. **Not compared.** |
| MorphoLibJ: label distance map and label morphology | `labeling.distanceMap`, `dilate`, `erode`, `open`, `close`, `influenceZones` | Clean-room (Saito & Toriwaki 1994; Felzenszwalb & Huttenlocher 2012). | **Differs**: uses exact Euclidean instead of chamfer distances. **Not compared.** |
| MorphoLibJ: region analysis | `labeling.inscribedBall`, `labeling.equivalentEllipsoid`, `labeling.maximumFeret`, `labeling.maximumFeretsDiameter` | Clean-room, tested against MorphoLibJ 1.6.4. | Agrees with MorphoLibJ. **Differs**: inscribed balls use exact Euclidean instead of chamfer distances. |
| MorphoLibJ: label overlap measures | `labeling.overlap.*` | Clean-room (Tustison & Gee 2009), tested against MorphoLibJ 1.6.4. | **Differs**: a label missing from one labeling gets the value of the formula (NaN where undefined), where MorphoLibJ reports 0. |
| MorphoLibJ: intrinsic volumes (Analyze Regions) | `features.intrinsicVolumes.*` (*LGPL*) | Port | **Same results** to 1e-9 on anisotropically calibrated test images. **Differs**: Euler numbers default to full connectivity (MorphoLibJ's default is face). **Fixes**: the 3-direction interface area honors the calibration and agrees with the surface area; MorphoLibJ returns twice that value, whatever the spacing. |
| [3D Objects Counter](https://github.com/fiji/3D_Objects_Counter) (GPL): edge exclusion and size filter | `labeling.removeBorderLabels`, `labeling.filterBySize` | Clean-room, from the documented behavior. | **Not compared.** |
| [BioVoxxel Toolbox](https://github.com/biovoxxel/BioVoxxel-Toolbox) (BSD-3): Extended Particle Analyzer shape descriptors | `geom.compactness`, `geom.extent`, `geom.feretAspectRatio` | Port | `geom.extent` also accepts n-D regions. **Not compared.** |
| BioVoxxel Toolbox (BSD-3): Shape Descriptor Maps | `labeling.featureMap` | Port | **Differs**: paints any per-label value, not only a fixed set of 2D shape descriptors. **Not compared.** |
| BioVoxxel Toolbox (BSD-3): Field-of-view measure correction | `labeling.edgeCorrectedCount`, `labeling.edgeCorrectionWeights` | Port | **Not compared.** |
| BioVoxxel Toolbox (BSD-3): Particle Distribution 2D, Distribution Analysis | `labeling.nearestNeighborDistances`, `labeling.clarkEvans` | Clean-room (Clark & Evans 1954), inspired by the plugins. | **Differs**: uses the Clark–Evans statistics directly instead of the plugins' tabulated F and t tests. **Not compared.** |
| BioVoxxel Toolbox (BSD-3): Binary Feature Extractor | `labeling.selectByOverlap` | Clean-room, from the documented behavior. | **Not compared.** |
| BioVoxxel Toolbox: Speckle Inspector (GPL-3 file header) | `labeling.countContained`, `labeling.filterByContainedCount` (*GPL*) | Port | **Same results** against the plugin in macro and batch mode. **Differs**: filter the primary objects (size, circularity, edges) beforehand with the `labeling.*` Ops; to count secondary objects in holes as the plugin does, fill the holes first (`labeling.fillHoles`); secondary circularity limits are not supported. |
| [Directionality](https://github.com/fiji/Directionality) (GPL-3) | `features.directionalityHistogram` (*GPL*) | Port | **Same results** for the Fourier method. **Differs**: analyzes one 2D plane; there is no orientation map. **Fixes**: the gradient method no longer truncates orientations into bins, which biased the plugin's histogram and fitted direction by half a bin; the amount wraps around the 180° period, as the plugin documents but does not do. |
| [RadiomicsJ](https://github.com/tatsunidas/RadiomicsJ) (Apache-2.0) | `image.runLengthMatrix`, `image.sizeZoneMatrix`, `image.distanceZoneMatrix`, `image.neighborhoodGrayToneDifferenceMatrix`, `image.neighboringGrayLevelDependenceMatrix`, and `features.glrlm.*`, `features.glszm.*`, `features.gldzm.*`, `features.ngtdm.*`, `features.ngldm.*` | Port | Match the IBSI digital phantom reference values. **Not compared** with RadiomicsJ itself. |
| RadiomicsJ: GLCM features | seven new `features.haralick.*` features | Clean-room, from the IBSI reference manual. | Match an independent evaluation of the IBSI formulas. |
| Two Point Correlation ([VIB](https://github.com/fiji/VIB), GPL-3) | `features.twoPointCorrelation` | Clean-room (Berryman & Blair 1986; Torquato 2002); not consulted. | **Not compared.** |
| Shape-based averaging ([VIB-lib](https://github.com/fiji/VIB-lib), GPL-3) | `labeling.shapeBasedAverage` | Clean-room (Rohlfing & Maurer 2007); not consulted. | **Not compared.** |

## Colocalization and colour

| Plugin | Ops | Origin | Results |
|---|---|---|---|
| [Coloc 2](https://github.com/fiji/Colocalisation_Analysis) (GPL-3) | `coloc.costesThreshold`, `coloc.manders`, `coloc.spearman` | Clean-room (Costes et al. 2004; Manders et al. 1993; Spearman 1904), tested against Coloc 2 3.1.0. | Agrees with Coloc 2 on its sample images, apart from threshold conventions. **Differs**: Coloc 2 counts a pixel equal to a threshold as above it. Its Costes thresholds are therefore the smallest integers above the Op's, and its tM1/tM2 at `(t1, t2)` equal the Op's at `(t1 - 1, t2 - 1)`. |
| Coloc 2 | `coloc.pValue` (existing), `image.histogram2D` | `coloc.pValue` is an existing Op; `image.histogram2D` is clean-room. | **Differs**: `coloc.pValue` is the block randomization test of Costes et al. (2004) as published; Coloc 2 also smooths the shuffled images and uses a normal-CDF P-value. **Fixes**: `coloc.pValue` used to fail or misalign on images that are not a multiple of the block size. |
| [Colour Deconvolution](https://github.com/fiji/Colour_Deconvolution) (GPL) | `color.deconvolveStains` | Clean-room (Ruifrok & Johnston 2001), tested against the plugin. | Within 1.5 grey levels of the plugin's 8-bit output. **Differs**: the output is stain amounts in optical density units, not 8-bit transmittance images; a missing third vector is the cross product of the other two; `H&E` uses the published Ruifrok vectors, not the plugin's measured set. |
| [Color Inspector 3D](https://github.com/fiji/Color_Inspector_3D) (GPL), [ijp-color](https://github.com/ij-plugins/ijp-color) (LGPL) | `color.convert`, `color.deltaE` | Clean-room (IEC 61966-2-1; CIE 15:2004; Sharma, Wu & Dalal 2005); not consulted. | `color.deltaE` matches the test data of Sharma, Wu & Dalal. **Not compared.** |

## Time series, registration and resampling

| Plugin | Ops | Origin | Results |
|---|---|---|---|
| [Bleach Correction](https://github.com/fiji/CorrectBleach) (GPL-3) | `image.bleachCorrect`, `image.bleachFit`, `image.histogramMatch` | Clean-room (Miura 2020); not consulted. | **Not compared.** |
| [Gaussian Stack Focuser](https://github.com/fiji/Time_Lapse/blob/main/src/main/java/sc/fiji/timelapse/Gaussian_Stack_Focuser.java) (GPL-3) | `transform.focusStack`, `transform.focusStackIndex` | Clean-room (Pech-Pacheco et al. 2000); not consulted. | **Differs**: the focus measure is the local variance of the Laplacian. The weighted mode is similar in spirit to the plugin. **Not compared.** |
| [KymographBuilder](https://github.com/fiji/KymographBuilder) (MIT) | `image.kymograph` | Port | **Differs**: samples at uniform arc-length steps in real coordinates, interpolates n-linearly by default (`"nearest"` reproduces the plugin's rounding), and keeps all non-spatial, non-time dimensions. **Fixes**: row 0 and column 0 are sampled; the plugin skips them because of an off-by-one bounds check. |
| [PIV analyser](https://github.com/fiji/PIV_analyser) (GPL-3) | `image.piv` | Clean-room (Willert & Gharib 1991; Raffel et al.; Westerweel & Scarano 2005); not consulted. | Single-pass analysis, like the plugin. **Differs**: windows may have any size, not only powers of two. **Not compared.** |
| [Stitching](https://github.com/fiji/Stitching) (GPL-2): pairwise phase correlation | `filter.phaseCorrelate` | Clean-room (Kuglin & Hines 1975; Preibisch, Saalfeld & Tomancak 2009); no code used. | Translation only. **Differs**: no window function, a regularized cross-power spectrum, and subpixel shifts from the Pearson correlation instead of a quadratic fit. **Not compared.** |
| Resample ([VIB](https://github.com/fiji/VIB), GPL-3); ImageJ's Image › Transform › Bin | `transform.bin` | Clean-room. | Drops incomplete edge blocks, as ImageJ's Bin does. **Not compared.** |
| Quantile Based Normalization ([VIB](https://github.com/fiji/VIB), GPL-3) | `image.quantileNormalize` | Clean-room (Bolstad et al. 2003); not consulted. | **Not compared.** |

## New Ops without a plugin counterpart

- `image.localExtrema`: a wrapper of ImgLib2's `LocalExtrema`.
- `coloc.mutualInformation` and `stats.entropy`: clean-room (Cover & Thomas 2006; Shannon 1948).
