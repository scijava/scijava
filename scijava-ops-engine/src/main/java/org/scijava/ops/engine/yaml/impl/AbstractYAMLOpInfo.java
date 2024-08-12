/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

package org.scijava.ops.engine.yaml.impl;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.util.Infos;
import org.scijava.priority.Priority;
import org.scijava.struct.Struct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractYAMLOpInfo implements OpInfo {

	protected final Map<String, Object> yaml;
	protected final String identifier;
	protected final List<String> names;
	protected final String description;
	protected final String version;
	protected final Double priority;
	protected final Hints hints;

	public AbstractYAMLOpInfo(final Map<String, Object> yaml,
		final String identifier)
	{
		this.yaml = yaml;
		this.identifier = identifier;
		this.names = parseNames();
		this.priority = parsePriority();
		this.description = yaml.getOrDefault("description", "").toString();
		this.version = (String) yaml.get("version");
		this.hints = new Hints((List<String>) yaml.getOrDefault( //
				"hints", //
				Collections.emptyList() //
		));
	}

	/**
	 * Parses the names out of the YAML
	 *
	 * @return the names stored in the YAML
	 * @throws IllegalArgumentException if there are no names in the YAML, or if
	 *           the names element is not a (collection of) String.
	 */
	protected List<String> parseNames() {
		List<String> names = new ArrayList<>();
		// Construct names
		if (yaml.containsKey("name")) {
			names.add((String) yaml.get("name"));
		}
		else if (yaml.containsKey("names")) {
			var tmp = yaml.get("names");
			if (tmp instanceof List) {
				names = (List<String>) tmp;
			}
			else if (tmp instanceof String) {
				names.add((String) tmp);
			}
			else {
				throw new IllegalArgumentException("Cannot convert" + tmp +
					"to a String[]!");
			}
		}
		else {
			throw new IllegalArgumentException("Op " + identifier +
				" declares no names!");
		}
		// Trim names
		for (var i = 0; i < names.size(); i++) {
			names.set(i, names.get(i).trim());
		}
		// Return names
		return names;
	}

	/**
	 * Parses the priority out of the YAML
	 *
	 * @return the priority stored in the YAML, or otherwise
	 *         {@link Priority#NORMAL}
	 */
	protected double parsePriority() {
		// Parse priority
		if (yaml.containsKey("priority")) {
            var p = yaml.get("priority");
			if (p instanceof Number) return ((Number) p).doubleValue();
			else if (p instanceof String) {
				return Double.parseDouble((String) p);
			}
			else {
				throw new IllegalArgumentException("Op priority " + p +
					" not parsable");
			}
		}
		// Return default priority
		return Priority.NORMAL;
	}

	@Override
	public List<String> names() {
		return names;
	}

	@Override
	public String description() {
		return description;
	}

	@Override
	public Hints declaredHints() {
		return hints;
	}

	@Override
	public double priority() {
		return priority;
	}

	@Override
	public String version() {
		return version;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof YAMLOpMethodInfo)) return false;
		final var that = (OpInfo) o;
		return struct().equals(that.struct());
	}

	@Override
	public abstract Struct struct();

	@Override
	public int hashCode() {
		return struct().hashCode();
	}

	@Override
	public String toString() {
		return Infos.describe(this);
	}
}
