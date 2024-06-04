/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

package org.scijava.ops.engine;

import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.types.Nil;

public class OpMethodInParentTest extends AbstractTestEnvironment {

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new SuperOpMethodHousingClass());
		ops.register(new SuperOpMethodHousingInterface());
	}

	@Test
	public void testFMethodInSuperclass() {
		String actual = ops.op("test.superMethod").input("Foo").outType(
			String.class).apply();
		String expected = "This string came from " +
			SuperOpMethodHousingClass.class;
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testFMethodInInterface() {
		String actual = ops.op("test.superMethodIface").input("Foo").outType(
			String.class).apply();
		String expected = "This string came from " +
			SuperOpMethodHousingInterface.class;
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testRequestingFunctionalTypeSubclass() {
		// Assert that things work just fine when asking for a Function
		Function<String, String> function = ops//
			.op("test.superMethod", new Nil<>()
			{}, new Nil[] { new Nil<String>() {} }, new Nil<String>() {});

		// Assert that things don't work when asking for a SuperOpMethodHousingClass
		Assertions.assertThrows(ClassCastException.class, () -> {
			SuperOpMethodHousingClass op = ops.op("test.superMethod", new Nil<>() {

			}, new Nil[] { new Nil<String>() {} }, new Nil<String>() {});
		});

	}

}

abstract class OpMethodHousingClass<T> implements Function<T, T> {

	abstract T getT();

	@Override
	public T apply(T t) {
		return getT();
	}

}

@OpClass(names = "test.superMethod")
class SuperOpMethodHousingClass //
	extends OpMethodHousingClass<String> //
	implements Op
{

	@Override
	String getT() {
		return "This string came from " + this.getClass();
	}

}

interface OpMethodHousingInterface<T> extends Function<T, T> {

	T getT();

	@Override
	default T apply(T t) {
		return getT();
	}

}

@OpClass(names = "test.superMethodIface")
class SuperOpMethodHousingInterface implements OpMethodHousingInterface<String>,
	Op
{

	@Override
	public String getT() {
		return "This string came from " + this.getClass();
	}

}
