/*-
 * #%L
 * SciJava Ops External Parser: A tool for parsing external libraries to ops
 * %%
 * Copyright (C) 2024 SciJava developers.
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

package org.scijava.ops.parser;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A data structure containing all the metadata needed to define an Op
 *
 * @author Gabriel Selzer
 * @author Mark Hiner
 */
public class OpData {

	/**
	 * A {@link Map} used to store any implementation-specific and/or nullable
	 * tags.
	 */
	private final Map<String, Object> tags;

	/**
	 * A {@link List} of {@link String}s describing the name(s) of the Op. There
	 * must be at least one.
	 */
	private final List<String> names;

	/**
	 * A {@link List} of {@link OpParameter}, describing the input and output
	 * parameters of this Op.
	 */
	private final List<OpParameter> params;

	/**
	 * A {@link String} identifying the code providing an Op's functionality.
	 */
	private final String source;

	/**
	 * The version of this Op.
	 */
	private final String version;

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
	private final List<String> authors;

	public OpData(final String source, final String version,
			final List<String> names, final List<OpParameter> params,
			final Map<String, Object> tags, List<String> authors)
	{
		this.source = source;
		this.version = version;
		this.names = names;
		this.params = params;
		this.tags = tags;
		this.authors = authors;

		validateOpData();
	}

	/**
	 * Helper method to ensure this OpImpl is valid. Throws
	 * {@link InvalidOpException} if problems are detected. Parallel
	 * implementation of org.scijava.ops.engine.util.Infos#validate in
	 * scijava-ops-engine
	 */
	private void validateOpData() {
		if (Objects.isNull(names) || names.isEmpty()) {
			throw new InvalidOpException(
					"Invalid Op defined in : " + source + ". Op names cannot be empty!");
		}

		int outputs = 0;
		for (OpParameter p : params) {
			if (p.ioType.equals(OpParameter.IO_TYPE.OUTPUT)) outputs++;
		}
		if (outputs > 1) {
			throw new InvalidOpException(
					"Invalid Op defined in : " + source + ". Ops cannot have more than one output!");
		}
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
