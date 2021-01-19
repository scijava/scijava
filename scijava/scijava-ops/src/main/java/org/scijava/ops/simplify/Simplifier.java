
package org.scijava.ops.simplify;

import java.lang.reflect.Type;

import org.scijava.plugin.SciJavaPlugin;
import org.scijava.types.Types;

/**
 * An object that can convert between a general and precise type.
 * 
 * @author Gabriel Selzer
 * @param <G> - the general type
 * @param <P> - the precise type
 */
public interface Simplifier<G, P> extends SciJavaPlugin {
	
	public G simplify(P p);

	public P focus(G g);

	default Type simpleType() {
		return Types.param(getClass(), Simplifier.class, 0);
	}

	default Type focusedType() {
		return Types.param(getClass(), Simplifier.class, 1);
	}

}
