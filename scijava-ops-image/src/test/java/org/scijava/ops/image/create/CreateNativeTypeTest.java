/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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

package org.scijava.ops.image.create;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.scijava.function.Producer;
import org.scijava.types.Nil;

import org.scijava.ops.image.AbstractOpTest;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.complex.ComplexDoubleType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Tests creating different NativeTypes.
 *
 * @author Brian Northan
 */
public class CreateNativeTypeTest extends AbstractOpTest {

	@Test
	public <T extends NativeType<T>> void testCreateNativeType() {

		Producer<NativeType> typeSource = ops.op("create.type") //
			.outType(NativeType.class) //
			.producer();

		Function<Class<T>, T> typeFunc = ops.op("create.object") //
			.inType(new Nil<Class<T>>()
			{}) //
			.outType(new Nil<T>()
			{}).function();

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
