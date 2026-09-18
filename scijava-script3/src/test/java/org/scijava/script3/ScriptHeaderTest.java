/*
 * #%L
 * Running scripts as things that declare their inputs and outputs.
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

package org.scijava.script3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.scijava.struct.ItemIO;

/**
 * Tests reading what a script declares about itself.
 *
 * @author Curtis Rueden
 */
public class ScriptHeaderTest {

	@Test
	public void testPlainParameters() {
		final ScriptHeader header = ScriptHeader.parse("" + //
			"#@ String name\n" + //
			"#@ int count\n" + //
			"#@output String greeting\n" + //
			"println(\"hi\")\n");

		final List<ScriptHeader.Parameter> params = header.parameters();
		assertEquals(3, params.size());
		assertEquals("name", params.get(0).name());
		assertEquals(String.class, params.get(0).type());
		assertEquals(ItemIO.INPUT, params.get(0).io());
		assertEquals(int.class, params.get(1).type());
		assertEquals(ItemIO.OUTPUT, params.get(2).io());
	}

	/** The attributes are the ones a Java {@code @Parameter} carries. */
	@Test
	public void testAttributes() {
		final ScriptHeader header = ScriptHeader.parse( //
			"#@ String(label = \"Your name\", value = \"ada\", " + //
				"choices = \"ada,grace\") name\n");

		final Map<String, String> attrs = header.parameters().get(0).attrs();
		assertEquals("Your name", attrs.get("label"));
		assertEquals("ada", attrs.get("value"));
		// NB: the comma inside the quotes is part of the value, not a separator.
		assertEquals("ada,grace", attrs.get("choices"));
	}

	/** A type may be left out, and a direction stated instead. */
	@Test
	public void testTypeAndDirectionVariants() {
		final ScriptHeader header = ScriptHeader.parse("" + //
			"#@output result\n" + //
			"#@both double weight\n" + //
			"#@input(persist = false) boolean verbose\n");

		assertEquals(Object.class, header.parameters().get(0).type());
		assertEquals(ItemIO.OUTPUT, header.parameters().get(0).io());
		assertEquals(ItemIO.MUTABLE, header.parameters().get(1).io());
		assertEquals(ItemIO.INPUT, header.parameters().get(2).io());
		assertEquals("false", header.parameters().get(2).attrs().get("persist"));
	}

	/** Unqualified type names resolve the way a script writer expects. */
	@Test
	public void testTypeNames() {
		final ScriptHeader header = ScriptHeader.parse("" + //
			"#@ File input\n" + //
			"#@ BigDecimal exact\n" + //
			"#@ java.util.List items\n");

		assertEquals(File.class, header.parameters().get(0).type());
		assertEquals(java.math.BigDecimal.class, header.parameters().get(1)
			.type());
		assertEquals(List.class, header.parameters().get(2).type());
	}

	/** A script may say things about itself, not only about its parameters. */
	@Test
	public void testDirectives() {
		final ScriptHeader header = ScriptHeader.parse( //
			"#@script(menu = \"Plugins>Gauss\", label = \"Gaussian Blur\")\n");

		assertEquals("Plugins>Gauss", header.directives().get("menu"));
		assertEquals("Gaussian Blur", header.directives().get("label"));
		assertTrue(header.parameters().isEmpty());
	}

	/** Lines that are not declarations are left alone. */
	@Test
	public void testOrdinaryLines() {
		final ScriptHeader header = ScriptHeader.parse("" + //
			"# an ordinary comment\n" + //
			"x = 5 # @ not a parameter\n" + //
			"#@ String name\n");

		assertEquals(1, header.parameters().size());
		assertEquals("name", header.parameters().get(0).name());
	}
}
