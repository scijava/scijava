/*-
 * #%L
 * Language Server Protocol bridge for SciJava code intelligence.
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
package org.scijava.code.lsp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.scijava.code.api.Completion;

/**
 * Tests {@link PythonLspClient}'s reading of signatures.
 *
 * @author Gabriel Selzer
 */
public class PythonLspClientTest {

	@Test
	public void testParse() {
		Completion c = parse("dumps",
			"def dumps(obj, *, skipkeys: bool=False, cls=None, **kw) -> str");
		assertEquals("[obj, bool skipkeys, cls, kw]", String.valueOf(c
			.parameters()));
		assertEquals("str", c.returnType());

		// Brackets and strings in annotations and defaults; positional-only.
		c = parse("zeros", "def zeros(shape: tuple[int, ...], dtype: str=\"f,8\"" +
			", /, order: Literal['C', 'F']='C') -> NDArray[float64]");
		assertEquals("[tuple[int, ...] shape, str dtype, " +
			"Literal['C', 'F'] order]", String.valueOf(c.parameters()));
		assertEquals("NDArray[float64]", c.returnType());

		// Classes (their constructors), without parameters or return type.
		c = parse("Path", "class Path()");
		assertEquals(0, c.parameters().size());
		assertNull(c.returnType());
		c = parse("f", "def f(*args)");
		assertEquals("args", c.parameters().get(0).name());

		// Not a signature.
		assertNull(PythonLspClient.parse("x", "instance x"));
	}

	private static Completion parse(final String name, final String sig) {
		return PythonLspClient.parse(name, sig).kind(Completion.Kind.METHOD)
			.build();
	}
}
