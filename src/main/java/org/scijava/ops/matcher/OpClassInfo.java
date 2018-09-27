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

package org.scijava.ops.matcher;

import java.lang.reflect.Type;
import java.util.List;

import org.scijava.core.Priority;
import org.scijava.ops.OpUtils;
import org.scijava.ops.core.Op;
import org.scijava.param.ParameterStructs;
import org.scijava.param.ValidityException;
import org.scijava.plugin.Plugin;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.util.Types;

/**
 * Metadata about an op implementation defined as a class.
 * 
 * @author Curtis Rueden
 * @author David Kolb
 */
public class OpClassInfo implements OpInfo {

	private final Class<? extends Op> opClass;
	private Struct struct;
	private ValidityException validityException;

	public OpClassInfo(final Class<? extends Op> opClass) {
		this.opClass = opClass;
		try {
			struct = ParameterStructs.structOf(opClass);
		} catch (ValidityException e) {
			validityException = e;
		} 
	}

	// -- OpInfo methods --

	@Override
	public Type opType() {
		// TODO: Check whether this is correct!
		return Types.parameterizeRaw(opClass);
		//return opClass;
	}

	@Override
	public Struct struct() {
		return struct;
	}

	@Override
	public List<Member<?>> inputs() {
		return OpUtils.inputs(struct());
	}

	@Override
	public List<Member<?>> outputs() {
		return OpUtils.outputs(struct());
	}

	@Override
	public double priority() {
		final Plugin opAnnotation = opClass.getAnnotation(Plugin.class);
		return opAnnotation == null ? Priority.NORMAL : opAnnotation.priority();
	}

	@Override
	public String implementationName() {
		return opClass.getName();
	}

	@Override
	public StructInstance<?> createOp() {
		final Object object;
		try {
			// TODO: Consider whether this is really the best way to
			// instantiate the op class here. No framework usage?
			// E.g., what about pluginService.createInstance?
			object = opClass.newInstance();
		} catch (final InstantiationException | IllegalAccessException e) {
			// TODO: Think about whether exception handling here should be
			// different.
			throw new IllegalStateException("Unable to instantiate op: '" + opClass.getName()
					+ "' Each op must have a no-args constructor.", e);
		}
		return struct().createInstance(object);
	}

	@Override
	public ValidityException getValidityException() {
		return validityException;
	}
	
	@Override
	public boolean isValid() {
		return validityException == null;
	}
	
	// -- Object methods --

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof OpClassInfo))
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
		return OpUtils.opString(this);
	}
}
