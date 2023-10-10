
package org.scijava.ops.indexer;

import static org.scijava.ops.indexer.ProcessingUtils.blockSeparator;
import static org.scijava.ops.indexer.ProcessingUtils.tagElementSeparator;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

/**
 * A data structure containing all the metadata needed to define an Op
 *
 * @author Gabriel Selzer
 */
public abstract class OpImplData {

	/**
	 * A {@link Map} used to store any implementation-specific and/or nullable
	 * tags.
	 */
	protected final Map<String, Object> tags = new HashMap<>();

	/**
	 * A {@link List} of {@link String}s describing the name(s) of the Op. There
	 * must be at least one.
	 */
	protected final List<String> names = new ArrayList<>();

	/**
	 * A {@link List} of {@link OpParameter}, describing the input and output
	 * parameters of this Op.
	 */
	protected final List<OpParameter> params = new ArrayList<>();

	/**
	 * A {@link URI} identifying the code providing an Op's functionality. In this
	 * URI, the path is to a Java {@link Class}, {@link java.lang.reflect.Method},
	 * or {@link java.lang.reflect.Field}, and the corresponding scheme being
	 * either {@code javaClass}, {@code javaMethod}, or {@code javaField}.
	 */
	protected String source;

	protected String version;

	/**
	 * The priority of this Op.
	 */
	protected double priority = 0.0;

	/**
	 * A description of the functionality provided by this Op.
	 */
	protected String description = "";

	/**
	 * A {@link List} of the authors of this Op
	 */
	protected final List<String> authors = new ArrayList<>();

	protected final ProcessingEnvironment env;

	/**
	 * Abstract constructor parsing tags that all Ops share
	 * @param source the {@link Element} that has been declared as an Op
	 * @param doc the Javadoc for {@code source}
	 * @param env the {@link ProcessingEnvironment}
	 */
	public OpImplData(Element source, String doc, ProcessingEnvironment env) {
		this.env = env;
		this.source = formulateSource(source);
		this.version = env.getOptions().getOrDefault(OpImplNoteParser.OP_VERSION, "UNKNOWN");
				List<String[]> tags = blockSeparator.splitAsStream(doc) //
		 .map(section -> tagElementSeparator.split(section, 2)) //
		 .collect( Collectors.toList());
		List<String[]> remaining = parseUniversalTags(tags);
		if (!remaining.isEmpty()) {
			parseAdditionalTags(source, remaining);
		}
	}

	private List<String[]> parseUniversalTags(List<String[]> tags) {
		List<String[]> remainingTags = new ArrayList<>();
		for (String[] tag : tags) {
			// Parse descriptions
			if (!tag[0].startsWith("@")) {
				if (description.isBlank()) this.description = String.join(" ", tag);
			}
			// Parse universal Javadoc tags
			else if (tag[0].equals("@author")) {
				addAuthor(tag[1]);
			}
			else if (tag[0].equals("@implNote")) {
				parseImplNote(tag[1]);
			}
			else {
				remainingTags.add(tag);
			}
		}

		return remainingTags;
	}

	/**
	 * Abstract method used to parse tags specific to the {@link OpImplData} subclass.
	 *
	 * @param source         the {@link Element} that is identified as an Op.
	 * @param additionalTags the remaining tags that are not universal across all Op implementation types.
	 */
	abstract void parseAdditionalTags(Element source, List<String[]> additionalTags);

	abstract String formulateSource(Element source);

	private void parseImplNote(String implTag) {
		var implElements = tagElementSeparator.split(implTag);
		if (implElements.length > 1) {
			for (int i = 1; i < implElements.length; i++) {
				String[] kv = implElements[i].split("=", 2);
				if (kv.length == 2) {
					String value = kv[1].replaceAll("^[,\"']+|[,\"']+$", "");
					if ("priority".equals(kv[0])) {
						this.priority = Double.parseDouble(value);
					}
					else if ("names".equals(kv[0]) || "name".equals(kv[0])) {
						names.addAll(Arrays.asList(value.split("\\s*,\\s*")));
					}
					else {
						if (value.contains(",")) {
							tags.put(kv[0], value.split(","));
						}
						else {
							tags.put(kv[0], value);
						}
					}
				}
			}
		}
	}

	protected void addAuthor(String author) {
		if (!authors.contains(author)) authors.add(author);
	}

	/**
	 * Returns a {@link Map} storing the needed Op data hierarchically.
	 * 
	 * @return the {@link Map} of data.
	 */
	public Map<String, Object> dumpData() {
		Map<String, Object> map = new HashMap<>();
		map.put("source", source);
		map.put("version", version);
		map.put("names", names.toArray(String[]::new));
		map.put("description", description);
		map.put("priority", priority);
		map.put("authors", authors.toArray(String[]::new));
		List<Map<String, Object>> foo = params.stream() //
			.map(OpParameter::data) //
			.collect(Collectors.toList());
		map.put("parameters", foo.toArray(Map[]::new));
		map.put("tags", tags);
		return Collections.singletonMap("op", map);
	}

}
