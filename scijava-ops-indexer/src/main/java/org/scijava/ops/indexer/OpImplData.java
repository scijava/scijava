/*-
 * #%L
 * An annotation processor for indexing Ops with javadoc.
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

package org.scijava.ops.indexer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static org.scijava.ops.indexer.ProcessingUtils.blockSeparator;
import static org.scijava.ops.indexer.ProcessingUtils.tagElementSeparator;

/**
 * A data structure containing all the metadata needed to define an Op
 *
 * @author Gabriel Selzer
 */
abstract class OpImplData {

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
	 *
	 * @param source the {@link Element} that has been declared as an Op
	 * @param doc the Javadoc for {@code source}
	 * @param env the {@link ProcessingEnvironment}
	 */
	public OpImplData(Element source, String doc, ProcessingEnvironment env) {
		this.env = env;
		this.source = formulateSource(source);
		this.version = env.getOptions().getOrDefault(OpImplNoteParser.OP_VERSION,
			"UNKNOWN");
        List<String[]> tags = blockSeparator.splitAsStream(doc) //
			.map(section -> tagElementSeparator.split(section, 2)) //
			.collect(Collectors.toList());
        List<String[]> remaining = parseUniversalTags(tags);
		parseAdditionalTags(source, remaining);
		validateOpImpl();
	}

	/**
	 * Helper method to ensure this OpImpl is valid. Throws
	 * {@link InvalidOpImplException} i problems are detected. Parallel
	 * compile-time implementation of org.scijava.ops.engine.util.Infos#validate
	 * in scijava-ops-engine
	 */
	private void validateOpImpl() {
		if (Objects.isNull(names) || names.isEmpty()) {
			throw new InvalidOpImplException("Invalid Op defined in : " + source +
				". Op names cannot be empty!");
		}

        int outputs = 0;
		for (OpParameter p : params) {
			if (p.ioType.equals(OpParameter.IO_TYPE.OUTPUT)) outputs++;
		}
		if (outputs > 1) {
			throw new InvalidOpImplException("Invalid Op defined in : " + source +
				". Ops cannot have more than one output!");
		}
	}

	private List<String[]> parseUniversalTags(List<String[]> tags) {
		List<String[]> remainingTags = new ArrayList<>();
		for (String[] tag : tags) {
			// Parse descriptions
			if (!tag[0].startsWith("@")) {
				if (description.trim().isEmpty()) this.description = String.join(" ", tag);
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
	 * Abstract method used to parse tags specific to the {@link OpImplData}
	 * subclass.
	 *
	 * @param source the {@link Element} that is identified as an Op.
	 * @param additionalTags the remaining tags that are not universal across all
	 *          Op implementation types.
	 */
	abstract void parseAdditionalTags(Element source,
		List<String[]> additionalTags);

	abstract String formulateSource(Element source);

	/**
	 * Method for parsing the actual "@implNote" tag
	 *
	 * @param implTag Tag to parse
	 */
	private void parseImplNote(String implTag) {
		String[] implElements = tagElementSeparator.split(implTag);
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
		map.put("names", names.toArray());
		map.put("description", description);
		map.put("priority", priority);
		map.put("authors", authors.toArray());
        map.put("parameters", params.stream() //
                .map(OpParameter::data).toArray());
		map.put("tags", tags);
		return Collections.singletonMap("op", map);
	}

}
