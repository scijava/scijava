/*-
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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

package org.scijava.ops.engine.yaml;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.scijava.ops.api.OpInfo;
import org.scijava.priority.Priority;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;
import org.scijava.struct.MemberInstance;
import org.scijava.struct.Struct;

/**
 * An abstract base class for parsing the YAML into values common to
 * {@link OpInfo}s.
 *
 * @author Gabriel Selzer
 */
public abstract class AbstractYAMLOpInfoCreator implements YAMLOpInfoCreator {

	static final Set<String> outputKeys = new HashSet<>(List.of(
		"OUTPUT, CONTAINER, MUTABLE"));

	@Override
	public OpInfo create(final URI identifier, final Map<String, Object> yaml) {
		// Parse source - start after the leading slash
		final String srcString = identifier.getPath().substring(1);
		// Parse version
		final String version = yaml.get("version").toString();
		// Parse names
		final String[] names = parseNames(yaml, identifier);
		// Create the OpInfo
		OpInfo info;
		try {
			info = create(srcString, names, parsePriority(yaml), version, yaml);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		// If we have parameter information, bake it in.
		if (yaml.containsKey("parameters")) {
			List<Map<String, Object>> params = (List<Map<String, Object>>) yaml.get(
				"parameters");
			Iterator<Map<String, Object>> paramItr = params.iterator();

			List<Member<?>> members = info.struct().members();
			for (int i = 0; i < members.size(); i++) {
				Member<?> m = members.get(i);
				if (m.isInput() || m.isOutput()) {
					if (!paramItr.hasNext()) break;
					Map<String, Object> paramMap = paramItr.next();
					members.set(i, wrapMember(m, paramMap));
				}
			}
		}

		return info;
	}

	/**
	 * Parses the names out of the YAML
	 *
	 * @param yaml the YAML, stored in a {@link Map}
	 * @param identifier the {@link URI} identifying the source code for the Op
	 * @return the names stored in the YAML
	 * @throws IllegalArgumentException if there are no names in the YAML, or if
	 *           the names element is not a (collection of) String.
	 */
	private String[] parseNames(Map<String, Object> yaml, URI identifier) {
		final String[] names;
		// Construct names
		if (yaml.containsKey("name")) {
			names = new String[] { (String) yaml.get("name") };
		}
		else if (yaml.containsKey("names")) {
			var tmp = yaml.get("names");
			if (tmp instanceof List) {
				names = ((List<String>) tmp).toArray(String[]::new);
			}
			else if (tmp instanceof String) {
				names = new String[] { (String) tmp };
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
		for (int i = 0; i < names.length; i++) {
			names[i] = names[i].trim();
		}
		// Return names
		return names;
	}

	/**
	 * Parses the priority out of the YAML
	 *
	 * @param yaml the YAML, stored in a {@link Map}
	 * @return the priority stored in the YAML, or otherwise
	 *         {@link Priority#NORMAL}
	 */
	private double parsePriority(Map<String, Object> yaml) {
		// Parse priority
		if (yaml.containsKey("priority")) {
			Object p = yaml.get("priority");
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

	private Member<?> wrapMember(final Member<?> member,
		final Map<String, Object> map)
	{
		String name = member.getKey();
		if (member.isInput() && !member.isOutput()) {
			var newName = (String) map.get("name");
			if (newName != null) {
				name = newName;
			}
		}
		else {
			for (String key : outputKeys) {
				if (map.containsKey(key)) {
					name = (String) map.get(key);
					break;
				}
			}
		}
		String desc = ((String) map.getOrDefault("description", "")).trim();
		return new RenamedMember<>(member, name, desc);
	}

	protected abstract OpInfo create(final String identifier,
		final String[] names, final double priority, final String version,
		Map<String, Object> yaml) throws Exception;

	private static class RenamedMember<T> implements Member<T> {

		private final Member<T> src;
		private final String name;
		private final String desc;

		public RenamedMember(final Member<T> src, final String name,
			final String desc)
		{
			this.src = src;
			this.name = name;
			this.desc = desc;
		}

		@Override
		public String getKey() {
			return this.name;
		}

		@Override
		public String getDescription() {
			return this.desc;
		}

		@Override
		public Type getType() {
			return src.getType();
		}

		@Override
		public Class<T> getRawType() {
			return src.getRawType();
		}

		@Override
		public ItemIO getIOType() {
			return src.getIOType();
		}

		@Override
		public boolean isInput() {
			return src.isInput();
		}

		@Override
		public boolean isOutput() {
			return src.isOutput();
		}

		@Override
		public boolean isStruct() {
			return src.isStruct();
		}

		@Override
		public boolean isRequired() {
			return src.isRequired();
		}

		@Override
		public Struct childStruct() {
			return src.childStruct();
		}

		@Override
		public MemberInstance<T> createInstance(Object o) {
			return src.createInstance(o);
		}
	}
}
