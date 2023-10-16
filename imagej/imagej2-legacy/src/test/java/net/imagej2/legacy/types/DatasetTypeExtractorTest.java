/*-
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2023 ImageJ2 developers.
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

package net.imagej2.legacy.types;

import java.lang.reflect.Type;
import java.util.ServiceLoader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.discovery.Discoverer;
import org.scijava.types.Any;
import org.scijava.types.DefaultTypeReifier;
import org.scijava.types.Types;

import net.imagej.DefaultDataset;
import net.imagej.ImgPlus;
import net.imglib2.img.array.ArrayImgs;

/**
 * Tests {@link DatasetTypeExtractor}
 * 
 * @author Gabriel Selzer
 */
public class DatasetTypeExtractorTest {

	@Test
	public void testDatasetTypeExtractor() {
		// Create a TypeReifier
		var reifier = new DefaultTypeReifier(Discoverer.all(
			ServiceLoader::load));
		// Create a Dataset
		var ctx = new Context();
		var img = ArrayImgs.unsignedBytes(10, 10);
		var imgPlus = new ImgPlus<>(img);
		var ds = new DefaultDataset(ctx, imgPlus);
		// Assert correct reification
		// NB without a dependency on imagej-ops2 we cannot reify
		// the ImgPlus, but asserting an ImgPlus is the reified type
		// is enough to unit test the TypeExtractor
		var actual = new DatasetTypeExtractor().reify(reifier, ds);
		var expected = Types.parameterize(ImgPlus.class, new Type[] { new Any() });
		Assertions.assertEquals(expected, actual);
	}

}
