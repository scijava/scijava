/*-
 * #%L
 * ImageJ1 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2013 - 2023 ImageJ2 developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 0. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 1. Redistributions in binary form must reproduce the above copyright notice,
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

package org.scijava.ops.tutorial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.priority.Priority;

/**
 * A tutorial showing how you can use Op priorities to implement Ops that take
 * precedence over others in certain scenarios
 *
 * @author Gabriel Selzer
 */
public class OpPriorities implements OpCollection {

	@OpField(names = "tutorial.priority")
	public final Function<Iterable<Integer>, String> iterableFunc = //
		n -> {
			int max = Integer.MIN_VALUE;
			for (Integer object : n) {
				if (object > max) max = object;
			}
			return "This maximum (Iterable Op): " + max;
		};

	@OpField(names = "tutorial.priority", priority = Priority.HIGH)
	public final Function<SortedSet<Integer>, String> listFunc = //
		n -> "This maximum (SortedSet Op): " + n.last();

	/**
	 * Many algorithms are able to achieve performance improvements on a
	 * particular task by making some assumptions. For example, we might write a
	 * baseline "maximum" Op that works on java.lang.Iterable, which covers a lot
	 * of types, but we could do a lot better a TreeSet by using the
	 * SortedSet.last() method. Thus, we can write two different Ops, and get
	 * different outputs depending on whether the input is a SortedSet or
	 * something else.
	 */
	public static void main(String... args) {
		OpEnvironment ops = OpEnvironment.build();
		// Say we have a Collection of numbers
		var ourNumbers = Arrays.asList(4, 8, 2, 3);

		// ArrayList is not a SortedSet, so it will use our Iterable Op
		List<Integer> list = new ArrayList<>(ourNumbers);
		String resultForAllIterables = ops.unary("tutorial.priority") //
			.input(list) //
			.outType(String.class) //
			.apply();
		System.out.println(resultForAllIterables);

		// TreeSet is a SortedSet, so it could use both our Iterable Op and our
		// SortedSet Op. The SortedSet Op doesn't have to loop through the data, so
		// we want to use that one for performance. To do that, we set the priority
		// of that Op higher!
		TreeSet<Integer> set = new TreeSet<>(ourNumbers);
		String resultForATreeSet = ops.unary("tutorial.priority") //
			.input(set) //
			.outType(String.class) //
			.apply();
		System.out.println(resultForATreeSet);
	}
}
