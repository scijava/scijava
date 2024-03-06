# SciJava Ops OpenCV

This module contains a code-free (YAML-based) wrapping of the
[JavaCV](https://github.com/bytedeco/javacv) Java interface to
[OpenCV](https://opencv.org/).

YAML generated using the `scijava-ops-ext-parser` component.

To use these Ops simply add a Maven dependency on:

```xml
<dependency>
	<groupId>org.scijava</groupId>
	<artifactId>scijava-ops-opencv</artifactId>
</dependency>
```

Ops can be accessed in one of two ways:

1. Classic OpenCV nomenclature, e.g. `cv.GaussianBlur`
2. Ops-style nomenclature, e.g. `filter.gauss`
