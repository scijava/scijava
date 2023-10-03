
package org.scijava.ops.indexer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Record class wrangling data pertaining to an Op parameter
 * 
 * @author Gabriel Selzer
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
	 * Creates a valid YAML representation for this Op parameter
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
