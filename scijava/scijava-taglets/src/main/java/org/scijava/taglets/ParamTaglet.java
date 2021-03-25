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
		return "\n<dt><span class=\"paramLabel\">" + title + " parameters:</span></dt>\n" + //
				String.join("", tags.stream()//
						.map(tag -> "<dd>" + tagToHTML(tag) + "</dd>\n")//
						.collect(Collectors.toList()));
	}

	/** Reformats the tag content to match the HTML we want to have. */
	private String tagToHTML(final DocTree tag) {
		// Strip the leading tag string.
		final String text = tag.toString().trim().replaceFirst("^@" + tagName, "").trim();
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
