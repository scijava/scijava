/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2026 SciJava developers.
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

package org.scijava.ops.image.image.equation;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.parsington.ExpressionParser;
import org.scijava.parsington.SyntaxTree;

/**
 * Computes an image from an equation evaluated at each pixel position.
 * <p>
 * The expression is parsed by the
 * <a href="https://github.com/scijava/parsington">Parsington</a> library; see
 * {@link EquationEvaluator} for the set of supported variables, constants, and
 * functions. Common examples:
 * </p>
 * <pre>
 * cos(0.1*p[0]) + sin(0.1*p[1])
 * sqrt(x*x + y*y)
 * 128 + 127*sin(0.05*x + 0.05*y)
 * </pre>
 * <p>
 * Because evaluation is purely a function of the input expression and the
 * pixel position, results are reproducible: re-running the same expression
 * over an equally sized output always yields identical pixel values. Even
 * {@code random()} is deterministic — it returns a value in {@code [0, 1)}
 * derived by hashing the pixel position together with a fixed seed.
 * </p>
 * <p>
 * Note that this op is rather slow; it is intended primarily for
 * demonstration purposes, and for easily generating small synthetic images
 * for testing other ops.
 * </p>
 *
 * @author Curtis Rueden
 */
public final class Equation {

	private Equation() {
		// NB: utility class.
	}

	/**
	 * Fills {@code output} by evaluating {@code expression} at each pixel
	 * position.
	 *
	 * @param expression a math expression in the syntax supported by
	 *          {@link EquationEvaluator}.
	 * @param output the image buffer to fill.
	 * @implNote op names='image.equation', type=Computer
	 */
	public static <T extends RealType<T>> void equation(final String expression,
		final RandomAccessibleInterval<T> output)
	{
		final SyntaxTree tree = new ExpressionParser().parseTree(expression);
		final EquationEvaluator evaluator = new EquationEvaluator(0L);
		final int n = output.numDimensions();
		final long[] pos = new long[n];
		evaluator.position(pos);

		final var cursor = Views.iterable(output).localizingCursor();
		while (cursor.hasNext()) {
			cursor.fwd();
			cursor.localize(pos);
			double value;
			try {
				final Object result = evaluator.evaluate(tree);
				value = (result instanceof Number) ? ((Number) result).doubleValue()
					: Double.NaN;
			}
			catch (final RuntimeException exc) {
				value = Double.NaN;
			}
			cursor.get().setReal(value);
		}
	}
}
