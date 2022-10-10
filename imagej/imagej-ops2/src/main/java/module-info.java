/*-
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2022 ImageJ2 developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
module net.imagej.ops2 {
	exports net.imagej.ops2;
	
	// -- Open plugins to scijava-ops, therapi
	opens net.imagej.ops2.coloc to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.coloc.icq to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.coloc.kendallTau to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.coloc.maxTKendallTau to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.coloc.pearsons to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.coloc.pValue to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.convert to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.convert.clip to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.convert.copy to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.copy to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.create to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.deconvolve to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.deconvolve.accelerate to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.eval to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.features.haralick to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.features.haralick.helper to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.features.hog to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.features.lbp2d to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.features.tamura2d to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.features.zernike to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.addNoise to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.bilateral to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.convolve to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.correlate to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.derivative to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.derivativeGauss to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.dog to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.fft to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.fftSize to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.gauss to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.hessian to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.ifft to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.max to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.mean to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.median to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.min to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.pad to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.sigma to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.sobel to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.tubeness to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.variance to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.filter.vesselness to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.geom to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.geom.geom2d to org.scijava, therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.geom.geom3d to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.identity to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.image.ascii to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.image.cooccurrenceMatrix to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.image.distancetransform to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.image.fill to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.image.histogram to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.image.integral to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.image.invert to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.image.normalize to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.image.watershed to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.imagemoments to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.imagemoments.centralmoments to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.imagemoments.hu to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.imagemoments.moments to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.imagemoments.normalizedcentralmoments to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.labeling to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.labeling.cca to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.linalg.rotate to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.logic to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.map.neighborhood to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.math to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.math.multiply to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.morphology to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.morphology.thin to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.project to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.segment.detectJunctions to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.segment.detectRidges to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.slice to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.stats to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.stats.regression.leastSquares to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.thread.chunker to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.apply to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.huang to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.ij1 to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.intermodes to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.isoData to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.li to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.localBernsen to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.localContrast to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.localMean to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.localMedian to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.localMidGrey to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.localNiblack to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.localPhansalkar to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.localSauvola to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.maxEntropy to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.maxLikelihood to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.mean to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.minError to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.minimum to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.moments to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.otsu to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.percentile to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.renyiEntropy to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.rosin to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.shanbhag to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.triangle to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.threshold.yen to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.topology to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.topology.eulerCharacteristic to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.transform to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.types to therapi.runtime.javadoc, org.scijava.ops.engine, org.scijava.types;
	opens net.imagej.ops2.types.adapt to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.types.maxValue to therapi.runtime.javadoc, org.scijava.ops.engine;
	opens net.imagej.ops2.types.minValue to therapi.runtime.javadoc, org.scijava.ops.engine;
	
	requires java.xml; // TEMP: for org.scijava:scijava-common org.scijava.util.POM
	requires java.scripting;
	requires net.imagej.mesh;
	requires net.imglib2;
	requires net.imglib2.algorithm;
	requires net.imglib2.algorithm.fft2;
	requires net.imglib2.roi;
	requires org.joml;
	requires org.scijava;
	requires org.scijava.function;
	requires org.scijava.ops.api;
	requires org.scijava.ops.engine;
	requires org.scijava.ops.spi;
	requires org.scijava.parsington;
	requires org.scijava.types;
	
	// FIXME: these module names derive from filenames and are thus unstable
	requires commons.math3;
	requires ojalgo;
	requires jama;
	requires mines.jtk;

	provides org.scijava.types.TypeExtractor with
			net.imagej.ops2.types.ImgFactoryTypeExtractor,
			net.imagej.ops2.types.ImgLabelingTypeExtractor,
			net.imagej.ops2.types.NativeImgTypeExtractor,
			net.imagej.ops2.types.LabelingMappingTypeExtractor,
			net.imagej.ops2.types.OutOfBoundsConstantValueFactoryTypeExtractor,
			net.imagej.ops2.types.OutOfBoundsFactoryTypeExtractor,
			net.imagej.ops2.types.OutOfBoundsRandomValueFactoryTypeExtractor,
			net.imagej.ops2.types.RAITypeExtractor;

	uses org.scijava.ops.api.features.MatchingRoutine;
}
