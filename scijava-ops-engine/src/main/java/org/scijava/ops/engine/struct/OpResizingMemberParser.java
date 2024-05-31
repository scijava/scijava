/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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
import java.util.ArrayList;
import java.util.List;

import org.scijava.ops.engine.matcher.convert.ConvertedOpInfo;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;
import org.scijava.struct.MemberParser;
import org.scijava.struct.Struct;

public class OpResizingMemberParser implements
	MemberParser<RetypingRequest, Member<?>>
{

	/**
	 * Generates a new {@link List} with retyped {@link Member}s. Using
	 * {@code originalStruct} as a template, this method retypes the inputs of
	 * {@code originalStruct} using {@code inputs}, and the output using
	 * {@code output}.
	 * <p>
	 * This method makes a couple of assumptions:
	 * <ol>
	 * <li>That {@code srcStruct} is valid
	 * <li>That there are {@code inputs.length} input {@link Member}s in
	 * {@code srcStruct}
	 * <li>That there is <b>one</b> output {@link Member} in {@code srcStruct}
	 * </ol>
	 * We should consider adding the evaluation of these assumptions
	 *
	 * @param source the {@link RetypingRequest} from which we create the new
	 *          {@link List} of {@link Member}s
	 * @return a new {@link List} reflecting the reduced number of Op inputs.
	 */
	@Override
	public List<Member<?>> parse(RetypingRequest source, Type structType) {
		List<FunctionalMethodType> newFmts = source.newFmts();
		long numInputs = newFmts.stream() //
			.filter(fmt -> fmt.itemIO() == ItemIO.INPUT) //
			.count();
		List<Member<?>> newMembers = new ArrayList<>();
		long inputsAdded = 0;
		for (Member<?> m : source.struct().members()) {
			if (m.getIOType() == ItemIO.INPUT) {
				inputsAdded++;
				if (inputsAdded > numInputs) continue;
			}
			newMembers.add(m);
		}
		return newMembers;
	}

	public List<Member<?>> parse(Struct s, List<FunctionalMethodType> newFmts,
		Type structType)
	{
		return parse(new RetypingRequest(s, newFmts), structType);
	}

}
