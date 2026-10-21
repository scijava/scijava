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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.SimpleBindings;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.scijava.code.lsp.jvm.AbstractLanguageServer;
import org.scijava.code.lsp.jvm.Members;

/**
 * A language server knowing only the variables of a running interpreter (see
 * {@link Environment#live()}): their names, and the members of their values'
 * classes. For code typed into an interpreter whose language has no servers
 * of its own (see
 * {@link org.scijava.code.lsp.LanguageServerService#supports}).
 *
 * @author Gabriel Selzer
 */
class BindingsLanguageServer extends AbstractLanguageServer {

	/** A trailing {@code variable.prefix} before the caret. */
	private static final Pattern MEMBER = Pattern.compile(
		"([A-Za-z_][A-Za-z0-9_]*)\\.([A-Za-z0-9_]*)$");

	/** A trailing word before the caret. */
	private static final Pattern WORD = Pattern.compile(
		"([A-Za-z_][A-Za-z0-9_]*)$");

	/** @param environment Where the code runs: its live interpreter, if any. */
	BindingsLanguageServer(final Environment environment) {
		super(environment);
	}

	@Override
	protected CompletionList complete(final Document doc, final int offset) {
		final Bindings bindings = environment().live().map(l -> l.getBindings(
			ScriptContext.ENGINE_SCOPE)).orElseGet(SimpleBindings::new);
		final String line = doc.lineTo(offset);

		final Matcher member = MEMBER.matcher(line);
		if (member.find()) {
			final Object value = bindings.get(member.group(1));
			final String prefix = member.group(2);
			return sorted(doc, offset - prefix.length(), offset, value == null
				? new ArrayList<>() : Members.of(value.getClass(), prefix, false));
		}
		final Matcher word = WORD.matcher(line);
		final String prefix = word.find() ? word.group(1) : "";
		final String lp = prefix.toLowerCase(Locale.ROOT);
		final List<CompletionItem> items = new ArrayList<>();
		for (final String name : bindings.keySet()) {
			if (!name.toLowerCase(Locale.ROOT).startsWith(lp)) continue;
			final CompletionItem item = new CompletionItem(name);
			item.setKind(CompletionItemKind.Variable);
			final Object value = bindings.get(name);
			if (value != null) item.setDetail(Members.typeName(value.getClass()));
			items.add(item);
		}
		return sorted(doc, offset - prefix.length(), offset, items);
	}

	/** By name, without duplicates (e.g. overloads of the same arity). */
	private static CompletionList sorted(final Document doc, final int start,
		final int caret, final List<CompletionItem> items)
	{
		final Map<String, CompletionItem> unique = new LinkedHashMap<>();
		for (final CompletionItem item : items) {
			final String key = item.getLabel() + (item.getLabelDetails() == null ? ""
				: item.getLabelDetails().getDetail());
			unique.putIfAbsent(key, item);
		}
		final List<CompletionItem> out = new ArrayList<>(unique.values());
		out.sort(Comparator.comparing(c -> c.getLabel().toLowerCase(Locale.ROOT)));
		return list(doc, start, caret, out);
	}
}
