/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2025 SciJava developers.
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

package org.scijava.ops.engine.yaml.impl.ops;

import java.lang.reflect.Method;
import java.util.List;

/**
 * A static {@link Method}, exposed to Ops via YAML
 *
 * @author Gabriel Selzer
 */
public class YAMLMethodOp {

	/**
	 * An example Op, implemented by a {@link Method}
	 *
	 * @implNote op name=example.sub
	 * @param aDouble the first double
	 * @param aDouble2 the second double (nullable)
	 * @return the difference
	 */
	public static <N extends Number> Double subtract(N aDouble, N aDouble2) {
		if (aDouble2 == null) {
			return aDouble.doubleValue();
		}
		return aDouble.doubleValue() - aDouble2.doubleValue();
	}

	/**
	 * Another example Op, implemented by a {@link Method}
	 *
	 * @implNote op name=example.xor, type=Inplace1, hints="Adaptation.FORBIDDEN,Conversion.FORBIDDEN"
	 * @param aList the first integer {@link List}
	 * @param aList2 the second integer {@link List}
	 */
	public static void xor(List<Integer> aList, List<Integer> aList2) {
		for (int i = 0; i < aList.size(); i++) {
			aList.set(i, aList.get(i) ^ aList2.get(i));
		}
	}

	/**
	 * A third example Op, implemented by a {@link Method}
	 *
	 * @implNote op name=example.and, type=Computer
	 * @param aList the first integer {@link List}
	 * @param aList2 the second integer {@link List}
	 * @param out the logical and of the two integer {@link List}s
	 */
	public static void and(List<Integer> aList, List<Integer> aList2,
		List<Integer> out)
	{
		out.clear();
		for (int i = 0; i < aList.size(); i++) {
			out.add(aList.get(i) & aList2.get(i));
		}
	}

	/**
	 * A third example Op, implemented by a {@link Method}
	 *
	 * @implNote op name=example.or, type=Computer2
	 * @param aList the first integer {@link List}
	 * @param out the logical and of the two integer
	 * @param aList2 the second integer {@link List}
	 */
	public static void or(List<Integer> aList, List<Integer> out,
		List<Integer> aList2)
	{
		out.clear();
		for (int i = 0; i < aList.size(); i++) {
			out.add(aList.get(i) | aList2.get(i));
		}
	}

}
