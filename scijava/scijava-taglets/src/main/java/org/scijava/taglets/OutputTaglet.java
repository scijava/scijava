package org.scijava.taglets;

import java.util.Map;

import jdk.javadoc.doclet.Taglet;

/**
 * A taglet handling {@code @output} tags.
 * 
 * @author Curtis Rueden
 */
public class OutputTaglet extends ParamTaglet {

	public static void register(Map<String, Taglet> tagletMap) {
		new OutputTaglet().registerMe(tagletMap);
	}

	public OutputTaglet() {
		super("Output");
	}
}