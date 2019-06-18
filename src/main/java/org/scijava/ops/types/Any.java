package org.scijava.ops.types;

import java.lang.reflect.Type;

/**
 * This {@link Type} represents a Type that can be assigned to any other Type.
 * 
 * @author Gabe Selzer
 *
 */
public class Any implements Type {
	@Override
	public String toString() {
		return "Any";
	}

}
