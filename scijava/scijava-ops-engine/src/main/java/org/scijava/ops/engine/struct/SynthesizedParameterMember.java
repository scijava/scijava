/*-
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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

package org.scijava.ops.engine.struct;

import java.lang.reflect.Type;

import org.scijava.function.Producer;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;

/**
 * {@link Member} synthesized using constructor arguments
 *
 * @author Gabriel Selzer
 * @param <T>
 */
public class SynthesizedParameterMember<T> implements Member<T> {

	/** {@link FunctionalMethodType} describing this Member */
	private final FunctionalMethodType fmt;

	/** Producer able to generate the parameter name */
	private final Producer<String> nameGenerator;

	/** Name of the parameter */
	private String name = null;

	/** Producer able to generate the parameter descriptor */
	private final Producer<String> descriptionGenerator;

	private String description = null;

	private boolean isRequired;

	public SynthesizedParameterMember(final FunctionalMethodType fmt, final Producer<MethodParamInfo> synthesizerGenerator)
	{
		this(fmt, synthesizerGenerator, true);
	}

	public SynthesizedParameterMember(final FunctionalMethodType fmt, final Producer<MethodParamInfo> synthesizerGenerator, boolean isRequired)
	{
		this.fmt = fmt;
		this.nameGenerator = () -> synthesizerGenerator.create().name(fmt);
		this.descriptionGenerator = () -> synthesizerGenerator.create().description(fmt);
		this.isRequired = !synthesizerGenerator.create().optionality(fmt);
	}



	// -- Member methods --

	@Override
	public String getKey() {
		if (name == null) generateName();
		return name;
	}

	private synchronized void generateName() {
		if (name != null) return;
		String temp = nameGenerator.create();
		name = temp;
	}

	@Override
	public String getDescription() {
		if (description == null) generateDescription();
		return description;
	}

	private synchronized void generateDescription() {
		if (description != null) return;
		String temp = descriptionGenerator.create();
		description = temp;
	}

	@Override
	public Type getType() {
		return fmt.type();
	}

	@Override
	public ItemIO getIOType() {
		return fmt.itemIO();
	}

	@Override
	public boolean isStruct() {
		return false;
	}

	@Override
	public boolean isRequired() {return isRequired;}
}
