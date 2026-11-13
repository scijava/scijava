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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link CompletionItems}.
 *
 * @author Gabriel Selzer
 */
public class CompletionItemsTest {

	@Test
	public void testMethod() {
		final CompletionItem item = CompletionItems.method("foo", Arrays.asList(
			"bar", "baz"), "(double bar, int baz)");
		assertEquals("foo", item.getLabel());
		assertEquals(CompletionItemKind.Method, item.getKind());
		// A tab stop for each parameter.
		assertEquals(InsertTextFormat.Snippet, item.getInsertTextFormat());
		assertEquals("foo(${1:bar}, ${2:baz})", item.getInsertText());
		// Matched by its name, not by its snippet.
		assertEquals("foo", item.getFilterText());
		// The parameters shown beside the name, as given.
		assertEquals("(double bar, int baz)", item.getLabelDetails().getDetail());
		// NB: Only methods known by reflection have a signature (see members).
		assertNull(item.getDetail());
	}

	@Test
	public void testMembersInstance() {
		// Through an instance: its instance members only.
		final List<CompletionItem> items = CompletionItems.instanceMembers(String.class,
			"");
		assertTrue(labels(items).contains("length"));
		assertFalse(labels(items).contains("valueOf"));
		assertFalse(labels(items).contains("CASE_INSENSITIVE_ORDER"));
        // Now with a prefix
        final List<CompletionItem> prefixItems = CompletionItems.instanceMembers(String.class,
                "v");
        assertFalse(labels(prefixItems).contains("length"));
        assertFalse(labels(prefixItems).contains("valueOf"));
        assertFalse(labels(prefixItems).contains("CASE_INSENSITIVE_ORDER"));
	}

	@Test
	public void testMembersStatic() {
		// Through the class: its static members only.
		final List<CompletionItem> items = CompletionItems.staticMembers(String.class,
			"");
		assertTrue(labels(items).contains("valueOf"));
		assertTrue(labels(items).contains("CASE_INSENSITIVE_ORDER"));
		assertFalse(labels(items).contains("length"));
        // Now with a prefix
        final List<CompletionItem> prefixItems = CompletionItems.staticMembers(String.class,
                "v");
        assertTrue(labels(prefixItems).contains("valueOf"));
        assertFalse(labels(prefixItems).contains("CASE_INSENSITIVE_ORDER"));
        assertFalse(labels(prefixItems).contains("length"));
	}

	// -- Helper methods --

	private static List<String> labels(final List<CompletionItem> items) {
		return items.stream().map(CompletionItem::getLabel).collect(Collectors
			.toList());
	}
}
