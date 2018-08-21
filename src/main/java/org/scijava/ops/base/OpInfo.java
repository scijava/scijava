/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package org.scijava.ops.base;

import java.util.List;

import org.scijava.ops.Op;
import org.scijava.param.ParameterStructs;
import org.scijava.param.ValidityException;
import org.scijava.plugin.Plugin;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;

/**
 * Metadata about a particular op implementation.
 * 
 * @author Curtis Rueden
 */
public class OpInfo {

	private final Class<? extends Op> opClass;
	private final Struct struct;

	public OpInfo(final Class<? extends Op> opClass) throws ValidityException {
		this.opClass = opClass;
		struct = ParameterStructs.structOf(opClass);
	}

	/** Gets the associated {@link Struct} metadata. */
	public Struct struct() {
		return struct;
	}

	public Class<? extends Op> opClass() {
		return opClass;
	}

	/** Gets the op's input parameters. */
	public List<Member<?>> inputs() {
		return OpUtils.inputs(struct());
	}

	/** Gets the op's output parameters. */
	public List<Member<?>> outputs() {
		return OpUtils.outputs(struct());
	}

	public Plugin getAnnotation() {
		return opClass.getAnnotation(Plugin.class);
	}

	/**
	 * Gets the type of op, as specified via {@code @Plugin(type = <type>)}).
	 */
	public Class<?> getType() {
		Plugin pluginAnnotation = getAnnotation();
		if (pluginAnnotation == null) {
			throw new IllegalStateException("No @Plugin annotation found!");
		}
		return pluginAnnotation.type();
	}

	// -- Object methods --

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof OpInfo))
			return false;
		final OpInfo that = (OpInfo) o;
		return struct().equals(that.struct());
	}

	@Override
	public int hashCode() {
		return struct().hashCode();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(opClass.getSimpleName() + "(\n\t\t");
		boolean first = true;
		for (final Member<?> arg : struct) {
			if (first) {
				first = false;
			} else {
				sb.append(",\n\t\t");
			}
			sb.append(arg.getType().toString());
		}
		sb.append(")\n");
		return sb.toString();
	}
}
