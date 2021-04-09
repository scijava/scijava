package org.scijava.taglets;

import java.util.Map;

import jdk.javadoc.doclet.Taglet;

/**
 * A taglet handling {@code @dependency} tags.
 * 
 * @author Gabriel Selzer
 */
public class OpDependencyTaglet extends ParamTaglet {

	public static void register(Map<String, Taglet> tagletMap) {
		new OpDependencyTaglet().registerMe(tagletMap);
	}

	public OpDependencyTaglet() {
		super("Dependency");
	}
}