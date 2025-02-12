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

import java.util.function.BiFunction;

import org.scijava.ops.api.OpEnvironment;

/**
 * An example Op, registered by YAML into the {@link OpEnvironment}
 *
 * @author Gabriel Selzer
 * @implNote op names=example.add, priority=100.0
 */
public class YAMLClassOp implements BiFunction<Double, Double, Double> {

	/**
	 * @param aDouble the first input
	 * @param aDouble2 the second input
	 * @return the result
	 */
	@Override
	public Double apply(Double aDouble, Double aDouble2) {
		return aDouble + aDouble2;
	}

	/**
	 * An example inner class Op, registered by YAML into the
	 * {@link OpEnvironment}
	 *
	 * @implNote op names=example.div
	 * @author Gabriel Selzer
	 */
	public static class YAMLInnerClassOp implements
		BiFunction<Double, Double, Double>
	{

		/**
		 * @param aDouble the first input
		 * @param aDouble2 the second input
		 * @return the result
		 */
		@Override
		public Double apply(Double aDouble, Double aDouble2) {
			return aDouble / aDouble2;
		}
	}
}
