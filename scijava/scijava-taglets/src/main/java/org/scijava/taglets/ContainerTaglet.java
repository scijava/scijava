package org.scijava.taglets;

import java.util.Map;

import jdk.javadoc.doclet.Taglet;

/**
 * A taglet handling {@code @container} tags.
 * 
 * @author Curtis Rueden
 */
public class ContainerTaglet extends ParamTaglet {

	public static void register(Map<String, Taglet> tagletMap) {
		new ContainerTaglet().registerMe(tagletMap);
	}

	public ContainerTaglet() {
		super("Container");
	}
}