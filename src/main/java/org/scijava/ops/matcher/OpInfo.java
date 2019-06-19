
package org.scijava.ops.matcher;

import java.lang.reflect.Type;
import java.util.List;

import org.scijava.ops.OpUtils;
import org.scijava.param.ValidityException;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;

/**
 * Metadata about an op implementation defined as a class.
 * 
 * @author Curtis Rueden
 * @author David Kolb
 */
public interface OpInfo {

	/** Generic type of the op. */
	Type opType();

	/** Gets the associated {@link Struct} metadata. */
	Struct struct();

	/** Gets the op's input parameters. */
	default List<Member<?>> inputs() {
		return OpUtils.inputs(struct());
	}

	/** Gets the op's output parameters. */
	default Member<?> output() {
		return OpUtils.outputs(struct()).get(0);
	}

	/** The op's priority. */
	double priority();

	/** A fully qualified, unambiguous name for this specific op implementation. */
	String implementationName();

	/** Create a StructInstance using the Struct metadata backed by an object of the op itself.  */
	StructInstance<?> createOpInstance();

	// TODO Consider if we really want to keep the following methods.
	boolean isValid();
	ValidityException getValidityException();
}
