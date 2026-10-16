/*-
 * #%L
 * Jython code intelligence for SciJava, via Jython's own parser.
 * %%
 * Copyright (C) 2020 - 2025 SciJava developers.
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
package org.scijava.code.lsp.jython;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.LanguageServerService;

/**
 * Tests {@link PythonCompanion}: a Python language server's environment for
 * Jython scripts (Java stubs).
 *
 * @author Gabriel Selzer
 */
public class PythonCompanionTest {

	private Context context;
	private File stubsDir;
	private JavaStubs stubs;

	@BeforeEach
	public void setUp() throws Exception {
		context = new Context(LanguageServerService.class);
		stubsDir = Files.createTempDirectory("jython-stubs").toFile();
		stubs = new JavaStubs(stubsDir, getClass().getClassLoader(),
			pkg -> Collections.emptyList());
	}

	@AfterEach
	public void tearDown() {
		context.dispose();
		stubsDir.delete();
	}

	@Test
	public void testOnlyWithAPythonServer() {
		// NB: No plugin offers jedi here (scijava-code-lsp-jedi does, but is left
		// out of the tests; see pom.xml): Jython is served by its own analysis
		// alone.
		assertNull(PythonCompanion.launch(context.service(
			LanguageServerService.class), stubs));
	}

	@Test
	public void testEnvironment() {
		// The server's own Python, searching the Java stubs.
		final Environment env = PythonCompanion.environment(stubs);
		assertEquals(null, env.interpreter());
		assertEquals(Collections.singletonList(stubsDir.getAbsolutePath()), env
			.paths());
	}

	@Test
	public void testMerge() {
		final CompletionItem own = new CompletionItem("max");
		own.setData(1);
		final CompletionList merged = PythonCompanion.merge(new CompletionList(
			Arrays.asList(new CompletionItem("min"), own)), new CompletionList(Arrays
				.asList(new CompletionItem("max"), new CompletionItem("math"))));
		// Jython's first, each in its own order; the server's only for names
		// Jython has not completed.
		assertEquals(Arrays.asList("max", "min", "math"), merged.getItems().stream()
			.map(CompletionItem::getLabel).collect(Collectors.toList()));
		assertEquals(1, merged.getItems().get(0).getData());
		final List<String> sortTexts = merged.getItems().stream().map(
			CompletionItem::getSortText).collect(Collectors.toList());
		final List<String> sorted = new ArrayList<>(sortTexts);
		Collections.sort(sorted);
		assertEquals(sorted, sortTexts);
	}
}
