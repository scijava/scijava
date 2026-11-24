# Changelog

All notable changes to SciJava Ops Image. Details of each op (origin,
licensing, differences from other implementations) are in its javadoc.

## [Unreleased]

### Changed

These change the results of existing ops.

- Global thresholds (`threshold.*`) now match Fiji's Auto Threshold:
  - integer images get one histogram bin per grey level, instead of 256
    bins over the data range;
  - an image with only two grey levels is thresholded at the lower one;
  - `threshold.otsu` resolves ties toward the lowest bin (this also changes
    `coloc.maxTKendallTau` results);
  - `threshold.minError` no longer fails when its iteration does not
    converge;
  - `threshold.huang` is now Fiji's "Huang" method; the previous algorithm
    is available as `threshold.huang2` (Fiji's "Huang2").
- Local thresholds (`threshold.local*`) now match Fiji's Auto Local
  Threshold:
  - `localContrast` was inverted, and now treats zero pixels as background;
  - `localSauvola` used a wrong formula; its default `r` is now half the
    type's range (128 for 8-bit);
  - `localPhansalkar` stretches its input to [0, 1], unless the new
    `normalize` parameter is false;
  - `localNiblack`, `localSauvola` and `localPhansalkar` use the population
    standard deviation;
  - `localBernsen` rounds the mid grey down on integer images;
  - histogram-based local methods use one bin per grey level, at most 256.
- `filter.tubeness` now honors its calibration, and rejects images that are
  not 2D or 3D.

### Added

- Optional `ignoreBlack` and `ignoreWhite` parameters for global thresholds.
- `threshold.huang2`, `threshold.hysteresis`, `threshold.rats`,
  `threshold.ratsSurface`.
- `filter.*`: `coherenceEnhancingDiffusion`, `convolvedBackgroundSubtraction`,
  `dericheGradientMagnitude`, `derichePartialDerivative`, `flatFieldCorrect`,
  `gaussianWeightedMedian`, `helmholtzOrientation`, `helmholtzWavelength`,
  `kalman`, `kuwahara`, `kuwaharaLinear`, `localCoefficientOfVariation`,
  `peronaMalik`, `phaseCorrelate`, `pseudoFlatField`, `retinex`, `tvDenoise`,
  `waveletATrous`, `waveletDenoise`, `weightedMedian`; tensor filters
  `coherency`, `harris`, `hessianEigenvalues`, `orientation`, `shapeIndex`,
  `sheetness`, `structureTensor`, `structureTensorEigenvalues`.
- `color.*`: `convert` (RGB, HSB, XYZ, Lab, Luv, YCbCr), `deconvolveStains`,
  `deltaE`.
- `deconvolve.*`: `landweber`, `tikhonov`, `wiener`.
- `morphology.*`: `reconstructByDilation`, `reconstructByErosion`,
  `regionalMaxima`/`Minima`, `extendedMaxima`/`Minima`, `hMaxima`/`Minima`,
  `imposeMaxima`/`Minima`, `killBorders`, grayscale `fillHoles`;
  `areaOpening`/`Closing`, `boxDiagonalOpening`/`Closing`,
  `grayVolumeOpening`/`Closing`, `attributeOpening`/`Closing`;
  `gradient`, `internalGradient`, `externalGradient`, `laplacian`,
  `directionalFilter`, `geodesicDistanceMap`, `interpolateSlices`,
  `localThickness`.
- `labeling.*`: `adjacency`, `boundaries`, `clarkEvans`, `cropLabel`,
  `distanceMap`, `dilate`, `erode`, `open`, `close`, `edgeCorrectedCount`,
  `edgeCorrectionWeights`, `equivalentEllipsoid`, `featureMap`, `fillHoles`,
  `filter`, `filterBySize`, `filterByValue`, `geodesicDiameter`,
  `geodesicDiameterPath`, `influenceZones`, `inscribedBall`, `keepLabels`,
  `removeLabels`, `keepLargest`, `removeLargest`, `maximumFeret`,
  `maximumFeretsDiameter`, `mergeLabels`, `nearestNeighborDistances`,
  `relabel`, `removeBorderLabels`, `selectByOverlap`, `shapeBasedAverage`;
  segmentation evaluation `labeling.overlap.*` (Dice, Jaccard, target
  overlap, volume similarity, false negative/positive error, total and per
  label).
- `segment.*`: `cannyDericheEdges`, `compactWatershed`, `detectHoughCircles`,
  `detectSpotsDoG`, `distanceTransformWatershed`, `extendedMinimaWatershed`,
  `fastMarching`, `graphCut`, `hWatershed`, `kMeans`, `levelSet`
  (Chan–Vese, geodesic active contours), `phantast`, `seededRegionGrowing`,
  `siox`, `statisticalRegionMerging`.
- `image.*`: `bleachCorrect`, `bleachFit`, `fastMarchingTimes`,
  `histogram2D`, `histogramMatch`, `kymograph`, `levelSetFunction`,
  `localExtrema`, `piv`, `quantileNormalize`, `signedDistanceTransform`.
- `transform.*`: `bin`, `focusStack`, `focusStackIndex`, `houghCircle`.
- `coloc.*`: `costesThreshold`, `manders`, `mutualInformation`, `spearman`;
  `stats.entropy`.
- `geom.*`: `compactness`, `extent`, `feretAspectRatio`.
- IBSI texture features: matrix ops `image.runLengthMatrix`,
  `sizeZoneMatrix`, `distanceZoneMatrix`,
  `neighborhoodGrayToneDifferenceMatrix`,
  `neighboringGrayLevelDependenceMatrix`, with the feature families
  `features.glrlm.*`, `glszm.*`, `gldzm.*`, `ngtdm.*`, `ngldm.*`; seven more
  `features.haralick.*` features; `features.twoPointCorrelation`.

### Fixed

- `threshold.localMean`, `localNiblack`, `localSauvola` and
  `localPhansalkar` failed on rectangular neighborhoods larger than 3x3.
- `image.integral` and `image.squareIntegral` were wrong for images with
  three or more dimensions.
- `image.distanceTransform` and `filter.tubeness` misread inputs whose
  minimum is not zero.
- `geom.majorAxis` and `geom.minorAxis` (and the ops built on them, such as
  `geom.eccentricity` and `geom.roundness`) were inaccurate for axis-aligned
  shapes and returned NaN for clockwise polygons.
- `coloc.pValue` misaligned or failed on images that are not a multiple of
  the block size.
