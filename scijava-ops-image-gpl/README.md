# SciJava Ops Image GPL

Image processing ops for [SciJava Ops](https://github.com/scijava/scijava)
translated from GPL-licensed ImageJ and Fiji plugins.

[SciJava Ops Image](../scijava-ops-image) is BSD-2 licensed, so algorithms
that are only available as GPL code, and that cannot practically be
reimplemented from their published descriptions, live here instead. Each op
credits the original authors, project and license in its javadoc.

## Contents

| Ops | Derived from |
|---|---|
| `morphology.skeletonize`, `morphology.tagSkeleton`, `topology.skeletonGraph`, `topology.skeletonStatistics` | Fiji's Skeletonize3D and AnalyzeSkeleton |
| `features.directionalityHistogram` | Fiji's Directionality |
| `segment.detectLines`, `segment.estimateLineParameters` | Ridge Detection (Steger's curvilinear structure detector) |
| `segment.spotsFromSeeds` | the 3D ImageJ Suite (mcib3d), Segment3DSpots |
| `threshold.limitedMean`, `labeling.countContained`, `labeling.filterByContainedCount` | BioVoxxel Toolbox (Limited Mean, Speckle Inspector) |

## License

This component is licensed under the GNU General Public License v3 or later;
see [LICENSE.txt](LICENSE.txt).
