# SciJava Ops OpenCV

This module contains a YAML-based wrapping of the
[Bytedeco](https://github.com/bytedeco/javacv) Java interface to
[OpenCV](https://opencv.org/), with minimal code to provide
interoperability with ImgLib2 data structures.

YAML is generated from `opencv-ops.yaml` using the `scijava-ops-ext-parser`
component.

## Usage

To use these Ops simply add a Maven dependency on:

```xml
<dependency>
	<groupId>org.scijava</groupId>
	<artifactId>scijava-ops-opencv</artifactId>
	<scope>runtime</scope>
</dependency>
```

Ops can be accessed in one of two ways:

1. Classic OpenCV nomenclature, e.g. `cv.GaussianBlur`
2. Aligned with Ops-style namespaces, e.g. `filter.gauss`

## Caveats

ImageJ-OpenCV currently only supports [single-channel
Mats](https://github.com/imagej/imagej-opencv/issues/2). Thus if you are using
multi-channel images, we currently recommend:

* Calling the OpenCV Op as a `Computer` with pre-allocated output
* Avoiding interuse of multi-channel `Mats` and `RandomAccessibleIntervals` (or
		their subclasses)
