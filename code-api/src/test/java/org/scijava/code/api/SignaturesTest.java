/*-
 * #%L
 * Core API for SciJava code intelligence features.
 * %%
 * Copyright (C) 2026 SciJava developers.
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

package org.scijava.code.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.scijava.code.api.SignatureHelp.Fit;

/**
 * Tests {@link Signatures}.
 *
 * @author Gabriel Selzer
 */
public class SignaturesTest {

	private static final ClassLoader LOADER = SignaturesTest.class
		.getClassLoader();

	private static final BiFunction<String, String, Fit> FIT = (a,
		p) -> Signatures.fit(a, p, LOADER);

	@Test
	public void testFit() {
		// Integers match integral parameters, and convert to floating ones.
		assertEquals(Fit.MATCH, FIT.apply("long", "int"));
		assertEquals(Fit.CONVERSION, FIT.apply("int", "double"));
		// Floating-point numbers (incl. BigDecimal) match floating ones only.
		assertEquals(Fit.MATCH, FIT.apply("double", "float"));
		assertEquals(Fit.MATCH, FIT.apply("java.math.BigDecimal", "double"));
		assertEquals(Fit.MISMATCH, FIT.apply("double", "int"));
		// Reference types follow assignability, with boxing and arrays.
		assertEquals(Fit.MATCH, FIT.apply("java.util.ArrayList",
			"java.util.List"));
		assertEquals(Fit.MATCH, FIT.apply("java.lang.Integer", "int"));
		assertEquals(Fit.MISMATCH, FIT.apply("java.lang.String", "int[]"));
		// Unknown types are unrated.
		assertEquals(Fit.UNKNOWN, FIT.apply(null, "int"));
		assertEquals(Fit.UNKNOWN, FIT.apply("no.such.Type", "int"));
	}

	@Test
	public void testRate() {
		final Completion floats = method("max", "float", "float");
		// As good as the worst argument; unknown ones do not count.
		assertEquals(Fit.CONVERSION, Signatures.rate(floats, Arrays.asList(
			"double", "long"), FIT));
		assertEquals(Fit.MATCH, Signatures.rate(floats, Arrays.asList(null,
			"double"), FIT));
		assertEquals(Fit.MISMATCH, Signatures.rate(floats, Arrays.asList(
			"java.lang.String", null), FIT));
		// Too many arguments.
		assertEquals(Fit.MISMATCH, Signatures.rate(floats, Arrays.asList(null,
			null, "int"), FIT));
		// Nothing known yet.
		assertEquals(Fit.UNKNOWN, Signatures.rate(floats, Arrays.asList(null,
			null), FIT));
	}

	@Test
	public void testRateAll() {
		final List<Completion> overloads = Arrays.asList(method("max", "int",
			"int"), method("max", "java.lang.String"), method("max", "float",
				"float"), method("max", "long", "long"));
		// Typed a float: the float overload first, mismatches last.
		final List<String> order = Signatures.rateAll(overloads, Arrays.asList(
			"double"), FIT).stream().map(s -> s.callable().parameters().get(0)
				.type() + ":" + s.fit()).collect(Collectors.toList());
		assertEquals(Arrays.asList("float:MATCH", "int:MISMATCH",
			"java.lang.String:MISMATCH", "long:MISMATCH"), order);
	}

	@Test
	public void testArguments() {
		final String text = "x = f(g(1, 2), 'a,b', [3, 4], y)";
		final int open = text.indexOf("f(") + 1;
		assertEquals(open, Signatures.callStart(text, text.indexOf('y')));
		assertEquals(Arrays.asList("g(1, 2)", " 'a,b'", " [3, 4]", " "),
			Signatures.arguments(text, open, text.indexOf('y')));
		// Inside a nested call, the innermost.
		final int inner = text.indexOf("g(") + 1;
		assertEquals(inner, Signatures.callStart(text, text.indexOf('2')));
		// Outside any call, or inside a list: none.
		assertEquals(-1, Signatures.callStart(text, 2));
		assertEquals(-1, Signatures.callStart(text, text.indexOf('4')));
		// The call is closed before the caret.
		assertNull(Signatures.arguments(text, open, text.length()));
	}

	private static Completion method(final String name,
		final String... paramTypes)
	{
		final List<Completion.Parameter> params = new ArrayList<>();
		for (final String type : paramTypes) {
			params.add(new Completion.Parameter(null, type));
		}
		return Completion.builder(name).kind(Completion.Kind.METHOD).parameters(
			params).build();
	}
}
