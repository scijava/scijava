/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops.create;

import static org.junit.Assert.assertEquals;

import java.util.function.Function;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.complex.ComplexDoubleType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.Test;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.core.Source;
import org.scijava.ops.types.Nil;

/**
 * Tests creating different NativeTypes.
 *
 * @author Brian Northan
 */
public class CreateNativeTypeTest extends AbstractTestEnvironment {

	@Test
	public <T extends NativeType<T>> void testCreateNativeType() {

		Source<NativeType> typeSource = ops.findOp("create.nativeType", new Nil<Source<NativeType>>() {
		}, new Nil[] {}, new Nil<NativeType>() {
		});

		Function<Class<T>, T> typeFunc = ops.findOp("create.nativeType", new Nil<Function<Class<T>, T>>() {
		}, new Nil[] { new Nil<Class<T>>() {
		} }, new Nil<T>() {
		});

		// default
		Object type = typeSource.create();
		assertEquals(type.getClass(), DoubleType.class);

		// FloatType
		type = typeFunc.apply((Class<T>) FloatType.class);
		assertEquals(type.getClass(), FloatType.class);

		// ComplexFloatType
		type = typeFunc.apply((Class<T>) ComplexFloatType.class);
		assertEquals(type.getClass(), ComplexFloatType.class);

		// DoubleType
		type = typeFunc.apply((Class<T>) DoubleType.class);
		assertEquals(type.getClass(), DoubleType.class);

		// ComplexDoubleType
		type = typeFunc.apply((Class<T>) ComplexDoubleType.class);
		assertEquals(type.getClass(), ComplexDoubleType.class);

	}

}
