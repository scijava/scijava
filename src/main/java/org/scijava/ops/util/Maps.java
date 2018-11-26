package org.scijava.ops.util;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.scijava.ops.core.computer.Computer;

import com.google.common.collect.Streams;

public class Maps {

	private Maps() {
		// NB: Prevent instantiation of utility class.
	}
		
	public interface Functions {
		
		public interface Iterables {
			
			public static <I, O> Function<Iterable<I>, Iterable<O>> liftBoth(final Function<I, O> function) {
				return iter -> () -> Streams.stream(iter).map(function).iterator();
			}

			public static <I, O> Function<Iterable<I>, Iterable<O>> bothFlat(final Function<I, Iterable<O>> function) {
				return iter -> () -> Streams.stream(iter).flatMap(i -> Streams.stream(function.apply(i))).iterator();
			}
		}
		
		public interface Arrays {
			@SuppressWarnings("unchecked")
			public static <I, O> Function<I[], O[]> liftBoth(final Function<I, O> function, Class<O> cls) {
				return is -> java.util.Arrays.stream(is).map(function).toArray(size -> (O[])Array.newInstance(cls, size));
			}
		}
		
		public interface Lists {
			public static <I, O> Function<List<I>, List<O>> liftBoth(final Function<I, O> function) {
				return iter -> iter.stream().map(function).collect(Collectors.toList());
			}
		}
	}

	public interface Computers {
		
		public interface Iterables {
			
			public static <I, O> Computer<Iterable<I>, Iterable<O>> liftBoth(final Computer<I, O> computer) {
				return (iter1, iter2) -> {
					Iterator<I> i1 = iter1.iterator();
					Iterator<O> i2 = iter2.iterator();
					computer.compute(i1.next(), i2.next());
				};
			}
		}
	}
}
