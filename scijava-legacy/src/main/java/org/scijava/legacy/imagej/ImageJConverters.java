/*-
 * #%L
 * Interoperability with legacy SciJava libraries.
 * %%
 * Copyright (C) 2023 - 2024 SciJava developers.
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
package org.scijava.legacy.imagej;

import ij.ImagePlus;
import net.imagej.Dataset;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpHints;

import java.util.function.Function;

/**
 * A set of {@code engine.convert} Ops for ImageJ-related data types.
 *
 * @author Curtis Rueden
 */
public class ImageJConverters< T extends NumericType< T > & NativeType< T >> implements OpCollection {

	@OpHints(hints = { "conversion.FORBIDDEN" })
	@OpField(names = "engine.convert")
	public final Function<ImagePlus, Img<T>> impToImg = ImageJFunctions::wrap;

	@OpHints(hints = { "conversion.FORBIDDEN" })
	@OpField(names = "engine.convert")
	public final Function<RandomAccessibleInterval<T>, ImagePlus> raiToImp = rai -> ImageJFunctions.wrap(rai, "Image");

	@OpHints(hints = { "conversion.FORBIDDEN" })
	@OpField(names = "engine.convert")
	public final Function<Dataset, ImagePlus> datasetToImp = dataset -> ImageJFunctions.wrap((RandomAccessibleInterval) dataset, dataset.getName());

}
