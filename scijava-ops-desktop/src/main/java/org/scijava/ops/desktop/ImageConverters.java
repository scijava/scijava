/*-
 * #%L
 * Interoperability with java.desktop image types.
 * %%
 * Copyright (C) 2024 SciJava developers.
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

package org.scijava.ops.desktop;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpHints;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Function;

/**
 * A set of {@code engine.convert} Ops for {@code java.desktop} image types.
 *
 * @author Curtis Rueden
 */
public class ImageConverters< T extends RealType< T >> implements OpCollection {

	@OpHints(hints = { "conversion.FORBIDDEN" })
	@OpField(names = "engine.convert")
	public final Function<Image, Img<T>> imageToImg = ImageConverters::img;

	@OpHints(hints = { "conversion.FORBIDDEN" })
	@OpField(names = "engine.convert")
	public final Function<RandomAccessibleInterval<T>, BufferedImage> raiToImage = ImageConverters::bi;

	private static <T extends RealType<T>> Img<T> img(Image image) {
		return null;
	}

	private static <T extends RealType<T>> BufferedImage bi(RandomAccessibleInterval<T> image) {
		return null;
	}

}
