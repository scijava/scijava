/*-
 * #%L
 * The public API of SciJava Ops.
 * %%
 * Copyright (C) 2021 - 2024 SciJava developers.
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

package org.scijava.ops.api;

import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

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

	/** Name(s) of the op. */
	List<String> names();

	/**
	 * Generic type of the op. This will be the parameterized type of the concrete
	 * class
	 */
	Type opType();

	/** The associated {@link Struct} metadata. */
	Struct struct();

	/** The hints declared by the Op */
	Hints declaredHints();

	/** The op's input parameters. */
	default List<Member<?>> inputs() {
		return struct().members().stream() //
			.filter(Member::isInput) //
			.collect(Collectors.toList());
	}

	/** The types of the op's input parameters. */
	default List<Type> inputTypes() {
		return inputs().stream() //
			.map(Member::type) //
			.collect(Collectors.toList());
	}

	/** The op's output parameters. */
	default List<Member<?>> outputs() {
		return struct().members().stream() //
			.filter(Member::isOutput) //
			.collect(Collectors.toList());
	}

	/** The op's output parameter, if there is <b>exactly</b> one. */
	default Member<?> output() {
        var outputs = outputs();

		if (outputs.size() == 0) throw new IllegalStateException(
			"No outputs in Struct " + struct());
		if (outputs.size() == 1) return outputs.get(0);
		throw new IllegalStateException("Multiple outputs in Struct " + struct());
	}

	/** The type of the op's output parameter, if there is <b>exactly</b> one. */
	default Type outputType() {
		return output().type();
	}

	/** The op's priority. */
	double priority();

	/** A fully qualified, unambiguous name for this specific op implementation. */
	String implementationName();

	/** Creates an instance of the op's associated {@link Struct} metadata. */
	StructInstance<?> createOpInstance(List<?> dependencies);

	AnnotatedElement getAnnotationBearer();

	@Override
	default int compareTo(final OpInfo that) {
		if (this.priority() < that.priority()) return 1;
		if (this.priority() > that.priority()) return -1;

		return this.implementationName().compareTo(that.implementationName());
	}

	/** The version of the Op. */
	String version();

	/** A unique identifier for an Op. */
	String id();

	/** A description of the Op's behavior. */
	default String description() {
		return "";
	}
}
