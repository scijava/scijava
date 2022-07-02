
package org.scijava.ops.api;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.struct.ValidityException;

/**
 * Metadata about an Op implementation.
 * 
 * @author Curtis Rueden
 * @author David Kolb
 * @author Gabriel Selzer
 */
public interface OpInfo extends Comparable<OpInfo> {

	/** Identifier for an unaltered OpInfo in an Op signature **/
	String IMPL_DECLARATION = "|Info:";

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
		return struct().members().stream() //
			.filter(Member::isInput) //
			.collect(Collectors.toList());
	}

	/** Gets the types of the op's input parameters. */
	default List<Type> inputTypes() {
		return inputs().stream() //
			.map(Member::getType) //
			.collect(Collectors.toList());
	}

	/** Gets the op's output parameters. */
	default List<Member<?>> outputs() {
		return struct().members().stream() //
				.filter(Member::isOutput) //
				.collect(Collectors.toList());
	}

	/** Gets the op's output parameter, if there is <b>exactly</b> one. */
	default Member<?> output() {
		List<Member<?>> outputs = outputs();

		if (outputs.size() == 0) throw new IllegalStateException(
			"No outputs in Struct " + struct());
		if (outputs.size() == 1) return outputs.get(0);
		throw new IllegalStateException(
			"Multiple outputs in Struct " + struct());
	}

	/** Gets the op's output parameter, if there is <b>exactly</b> one. */
	default Type outputType() {
		return output().getType();
	}

	/** Gets the op's dependencies on other ops. */
	default List<OpDependencyMember<?>> dependencies() {
		return struct().members().stream() //
			.filter(m -> m instanceof OpDependencyMember) //
			.map(m -> (OpDependencyMember<?>) m) //
			.collect(Collectors.toList());
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

	/**
	 * Writes a basic {@link String} describing this {@link OpInfo}
	 * 
	 * @return a descriptor for this {@link OpInfo}
	 */
	default String opString() {
		return opString(null);
	}

	/**
	 * Writes a basic {@link String} describing this {@link OpInfo} <b>with a
	 * particular {@link Member} highlighted</b>.
	 *
	 * @param special the {@link Member} to highlight
	 * @return a descriptor for this {@link OpInfo}
	 */
	default String opString(final Member<?> special) {
		final StringBuilder sb = new StringBuilder();
		sb.append(implementationName()).append("(\n\t Inputs:\n");
		for (final Member<?> arg : inputs()) {
			appendParam(sb, arg, special);
		}
		sb.append("\t Outputs:\n");
		appendParam(sb, output(), special);
		sb.append(")\n");
		return sb.toString();
	}

	/**
	 * Appends a {@link Member} to the {@link StringBuilder} writing the Op
	 * string.
	 * 
	 * @param sb the {@link StringBuilder}
	 * @param arg the {@link Member} being appended to {@code sb}
	 * @param special the {@link Member} to highlight
	 */
	private void appendParam(final StringBuilder sb, final Member<?> arg,
		final Member<?> special)
	{
		if (arg == special) sb.append("==> \t"); // highlight special item
		else sb.append("\t\t");
		sb.append(arg.getType().getTypeName());
		sb.append(" ");
		sb.append(arg.getKey());
		if (!arg.getDescription().isEmpty()) {
			sb.append(" -> ");
			sb.append(arg.getDescription());
		}
		sb.append("\n");
	}

}
