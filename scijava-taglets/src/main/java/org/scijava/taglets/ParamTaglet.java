/*-
 * #%L
 * Custom javadoc taglets for SciJava projects.
 * %%
 * Copyright (C) 2021 - 2024 SciJava developers.
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

package org.scijava.taglets;

import com.sun.source.doctree.DocTree;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;

import jdk.javadoc.doclet.Taglet;

public abstract class ParamTaglet implements Taglet {

	private final String tagName;
	private final String title;

	protected ParamTaglet(final String title) {
		this.tagName = title.toLowerCase();
		this.title = title;
	}

	@Override
	public Set<Location> getAllowedLocations() {
		return Collections.singleton(Location.FIELD);
	}

	@Override
	public boolean isInlineTag() {
		return false;
	}

	@Override
	public String getName() {
		return tagName;
	}

	@Override
	public String toString(List<? extends DocTree> tags, Element element) {
		return "\n<dt><span class=\"paramLabel\">" + title +
			" parameters:</span></dt>\n" + //
			String.join("", tags.stream()//
				.map(tag -> "<dd>" + tagToHTML(tag) + "</dd>\n")//
				.collect(Collectors.toList()));
	}

	/** Reformats the tag content to match the HTML we want to have. */
	private String tagToHTML(final DocTree tag) {
		// Strip the leading tag string.
		final String text = tag.toString().trim().replaceFirst("^@" + tagName, "")
			.trim();
		// Extract parameter name.
		final String paramName = text.replaceFirst("\\s.*", "");
		// Extract parameter description, if any.
		final String description = text.substring(paramName.length()).trim();
		final String separator = description.isEmpty() ? "" : " - ";
		// Slap together the HTML.
		return "<code>" + paramName + "</code>" + separator + description;
	}

	// NB: It would be great if we could just add multiple different instances
	// of this ParamTaglet class, which could then be concrete, to the taglet
	// map in a single static register method. Unfortunately, the taglet system
	// does not actually use the instances you add to the map! Instead, it
	// appears to construct new instances via an assumed no-args constructor?
	protected void registerMe(final Map<String, Taglet> tagletMap) {
		tagletMap.put(getName(), this);
	}
}
