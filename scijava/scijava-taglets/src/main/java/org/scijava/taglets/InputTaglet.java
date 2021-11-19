package org.scijava.taglets;

import java.util.Map;

import jdk.javadoc.doclet.Taglet;

/**
 * A taglet handling {@code @input} tags.
 * 
 * @author Curtis Rueden
 */
public class InputTaglet extends ParamTaglet {

	public static void register(Map<String, Taglet> tagletMap) {
		new InputTaglet().registerMe(tagletMap);
	}

	public InputTaglet() {
		super("Input");
	}
}