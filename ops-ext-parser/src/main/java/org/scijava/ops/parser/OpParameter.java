/*-
 * #%L
 * A tool for wrapping external libraries as SciJava Ops.
 * %%
 * Copyright (C) 2024 - 2025 SciJava developers.
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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Record class wrangling data pertaining to an Op parameter
 *
 * @author Gabriel Selzer
 * @author Mark Hiner
 */
public class OpParameter {

	/** Op parameters must be one of the following types */
	public enum IO_TYPE {
			INPUT, OUTPUT, MUTABLE, CONTAINER
	}

	protected final IO_TYPE ioType;
	protected final String name;
	protected final String type;
	protected final String desc;

	/**
	 * Default constructor
	 *
	 * @param name the name of the parameter
	 * @param type the {@link Type} of the parameter, stringified
	 * @param ioType the {@link IO_TYPE} of the parameter. Note that functional
	 *          outputs should use {@link IO_TYPE#OUTPUT}, output buffers should
	 *          use {@link IO_TYPE#CONTAINER}, and data structures that are
	 *          operated on inplace should use {@link IO_TYPE#MUTABLE}.
	 * @param description a description of the parameter
	 */
	public OpParameter(String name, String type, IO_TYPE ioType,
		String description)
	{
		// Assign io
		this.name = name;
		this.type = type;
		this.ioType = ioType;
		this.desc = description;
	}

	/**
	 * Creates a {@link Map} representation for this Op parameter that could be
	 * used in YAML generation.
	 *
	 * @return a {@link Map} containing this parameter's data.
	 */
	public Map<String, Object> data() {
		Map<String, Object> map = new HashMap<>();
		map.put("name", name);
		map.put("parameter type", ioType.toString());
		map.put("description", desc);
		if (type != null) {
			map.put("type", type);
		}
		return map;
	}
}
