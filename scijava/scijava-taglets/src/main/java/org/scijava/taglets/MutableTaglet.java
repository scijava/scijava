package org.scijava.taglets;

import java.util.Map;

import jdk.javadoc.doclet.Taglet;

/**
 * A taglet handling {@code @mutable} tags.
 * 
 * @author Curtis Rueden
 */
public class MutableTaglet extends ParamTaglet {

	public static void register(Map<String, Taglet> tagletMap) {
		new MutableTaglet().registerMe(tagletMap);
	}

	public MutableTaglet() {
		super("Mutable");
	}
}