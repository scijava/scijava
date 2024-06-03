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

package org.scijava.legacy.types;

import java.lang.reflect.Type;
import java.util.ServiceLoader;

import net.imagej.DefaultDataset;
import net.imagej.ImgPlus;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.discovery.Discoverer;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.common3.Any;
import org.scijava.common3.Types;
import org.scijava.types.extract.DefaultTypeReifier;

/**
 * Tests {@link DatasetTypeExtractor}
 *
 * @author Gabriel Selzer
 */
public class DatasetTypeExtractorTest {

	@Test
	public void testDatasetTypeExtractor() {
		// Create a TypeReifier
		// NB the use of Discoverer.all(ServiceLoader::load) finds no TypeExtractors
		// because the scijava-legacy module does not use the TypeExtractor
		// interface. Thus it cannot actually reify anything.
		var reifier = new DefaultTypeReifier(Discoverer.all(ServiceLoader::load));
		// Create a Dataset
		var img = ArrayImgs.unsignedBytes(10, 10);
		var imgPlus = new ImgPlus<>(img);
		// NB we pass a null context to avoid a SciJava Common dependency
		var ds = new DefaultDataset(null, imgPlus);
		// Assert correct reification
		// NB The generic parameter will be an Any due to the empty TypeReifier
		// as described above.
		var actual = new DatasetTypeExtractor().reify(reifier, ds);
		var expected = Types.parameterize(ImgPlus.class, new Type[] { Any.class });
		Assertions.assertEquals(expected, actual);
		// NB In the below check, the generic parameter will be UnsignedByteType,
		// because the scijava-ops-engine module DOES use TypeExtractor, so it can
		// discover the implementations. The IterableTypeExtractor is likely the
		// concrete TypeExtractor utilized to get the UnsignedByteType!
		OpEnvironment env = OpEnvironment.build();
		actual = env.genericType(ds);
		expected = Types.parameterize(ImgPlus.class, new Type[] {
			UnsignedByteType.class });
		Assertions.assertEquals(expected, actual);
	}

}
