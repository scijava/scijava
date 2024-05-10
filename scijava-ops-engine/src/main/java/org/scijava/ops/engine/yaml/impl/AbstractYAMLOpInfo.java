
package org.scijava.ops.engine.yaml.impl;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.util.Infos;
import org.scijava.priority.Priority;
import org.scijava.struct.Struct;

import java.lang.reflect.Type;
import java.util.ArrayList;
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
		this.hints = new Hints();
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
		for (int i = 0; i < names.size(); i++) {
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
		final OpInfo that = (OpInfo) o;
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
