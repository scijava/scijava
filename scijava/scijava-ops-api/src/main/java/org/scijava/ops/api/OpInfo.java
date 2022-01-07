
package org.scijava.ops.api;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;

import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.struct.ValidityException;

/**
 * Metadata about an Op implementation.
 * 
 * @author Curtis Rueden
 * @author David Kolb
 */
public interface OpInfo extends Comparable<OpInfo> {

	static final String IMPL_DECLARATION = "|Vanilla:";

	/** name(s) of the op. */
	List<String> names();

	/** Generic type of the op. This will be the parameterized type of the concrete class */
	Type opType();

	/** Gets the associated {@link Struct} metadata. */
	Struct struct();

	/** Gets the hints declared in the {@link OpHints} annotation */
	Hints declaredHints();

	/** Gets the op's input parameters. */
	default List<Member<?>> inputs() {
		return OpUtils.inputs(struct());
	}

	/** Gets the op's output parameters. */
	default Member<?> output() {
		List<Member<?>> outputs = OpUtils.outputs(struct());

		if (outputs.size() == 0) throw new IllegalStateException(
			"No outputs in Struct " + struct());
		if (outputs.size() == 1) return outputs.get(0);
		throw new IllegalStateException(
			"Multiple outputs in Struct " + struct());
	}

	/** Gets the op's dependencies on other ops. */
	default List<OpDependencyMember<?>> dependencies() {
		return OpUtils.dependencies(struct());
	}
	
	default OpCandidate createCandidate(OpEnvironment env, OpRef ref, Map<TypeVariable<?>, Type> typeVarAssigns) {
		return new OpCandidate(env, ref, this, typeVarAssigns);
	}

	/** The op's priority. */
	double priority();

	/** A fully qualified, unambiguous name for this specific op implementation. */
	String implementationName();

	/** Create a StructInstance using the Struct metadata backed by an object of the op itself. */
	StructInstance<?> createOpInstance(List<?> dependencies);

	// TODO Consider if we really want to keep the following methods.
	boolean isValid();
	ValidityException getValidityException();
	
	AnnotatedElement getAnnotationBearer();

	@Override
	default int compareTo(final OpInfo that) {
		if (this.priority() < that.priority()) return 1;
		if (this.priority() > that.priority()) return -1;
		return this.implementationName().compareTo(that.implementationName());
	}

	/** The version of the Op */
	String version();

	/** A unique identifier for an Op */
	String id();
}
