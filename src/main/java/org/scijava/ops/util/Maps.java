package org.scijava.ops.util;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.scijava.ops.core.Computer;

import com.google.common.collect.Streams;

public class Maps {

	private Maps() {
		// NB: Prevent instantiation of utility class.
	}

	public static class Lift {
		private Lift() {
		}
		
		public static class Functions {
			private Functions() {
			}
			
			public static <I, O> Function<List<I>, List<O>> list(final Function<I, O> function) {
				return iter -> iter.stream().map(function).collect(Collectors.toList());
			}
			
			public static <I, O> Function<Iterable<I>, Iterable<O>> iterable(final Function<I, O> function) {
				return iter -> () -> Streams.stream(iter).map(function).iterator();
			}

			public static <I, O> Function<Iterable<I>, Iterable<O>> iterableFlat(final Function<I, Iterable<O>> function) {
				return iter -> () -> Streams.stream(iter).flatMap(i -> Streams.stream(function.apply(i))).iterator();
			}
		}

		public static class Computers {
			private Computers() {
			}

			public static <I, O> Computer<Iterable<I>, Iterable<O>> iterable(final Computer<I, O> computer) {
				return (iter1, iter2) -> {
					Iterator<I> i1 = iter1.iterator();
					Iterator<O> i2 = iter2.iterator();
					computer.compute(i1.next(), i2.next());
				};
			}
		}
	}
}
