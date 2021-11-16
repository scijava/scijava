/*
 * #%L
 * SciJava Operations: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2019 SciJava Ops developers.
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

package org.scijava.types;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.convert.ConvertService;
import org.scijava.convert.Converter;

/**
 * Tests {@link NilConverter}.
 *
 * @author Curtis Rueden
 */
public class NilConverterTest {

	private ConvertService convert;

	/** Sets up a SciJava context and injects needed services. */
	@BeforeEach
	public void setUp() {
		convert = new Context(ConvertService.class).service(ConvertService.class);
	}

	/**
	 * Disposes of the {@link Context} that was initialized in {@link #setUp()}.
	 */
	@AfterEach
	public synchronized void cleanUp() {
		if (convert != null) {
			convert.context().dispose();
			convert = null;
		}
	}

	@Test
	public void testCanConvert() {
		final Nil<?> nil = Nil.of(Observer.class);
		assertCanConvert(nil, Observer.class);
		assertCanConvert(nil, List.class);
		assertCanConvert(nil, new Nil<Map<?, ?>>() {}.getType());
		// TODO: Enable after ConvertService is rewritten to fully
		// support generic types. Right now, CastingConverter steals this.
//		assertCanConvert(nil, new Nil<Nil<Map<?, ?>>>() {}.getType());
	}

	@SuppressWarnings("cast")
	@Test
	public void testConvert() {
		final Nil<?> nil = Nil.of(Observer.class);
		assertConvert(nil, Observer.class);
		assertConvert(nil, List.class);
		assertConvert(nil, new Nil<Map<?, ?>>() {}.getType());
		// TODO: Enable after ConvertService is rewritten to fully
		// support generic types. Right now, CastingConverter steals this.
//		assertConvert(nil, new Nil<Nil<Map<?, ?>>>() {}.getType());
		final Converter<?, ?> converter = convert.getHandler(nil, Observer.class);
		Assertions.assertTrue(converter instanceof NilConverter);
		// TODO: Enable after Nil proxying is improved to support non-interfaces.
//		Assertions.assertTrue(converter.convert(nil, String.class) instanceof String);
		Assertions.assertTrue(converter.convert(nil, List.class) instanceof List);
		Assertions.assertTrue(converter.convert(nil, new Nil<Map<?, ?>>() {}.getType()) instanceof Map);
		// TODO: Enable after ConvertService is rewritten to fully
		// support generic types. Right now, CastingConverter steals this.
//		Assertions.assertTrue(converter.convert(nil, new Nil<Nil<Map<?, ?>>>() {}.getType()) instanceof Nil);
	}

	private void assertCanConvert(final Nil<?> nil, final Type destType) {
		final Converter<?, ?> converter = convert.getHandler(nil, destType);
		Assertions.assertTrue(converter instanceof NilConverter);
		Assertions.assertTrue(converter.canConvert(nil, destType));
	}

	private void assertConvert(final Nil<?> nil, final Type destType) {
		final Converter<?, ?> converter = convert.getHandler(nil, destType);
		Assertions.assertTrue(converter instanceof NilConverter);
		final Object o = converter.convert(nil, destType);
		Assertions.assertTrue(Types.raw(destType).isAssignableFrom(o.getClass()));
	}

}
