package org.scijava.ops.util;

import com.google.common.collect.Streams;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.scijava.functions.Computers;

public class Maps {

	private Maps() {
		// NB: Prevent instantiation of utility class.
	}
		
	public interface FunctionMaps {
		
		public interface Iterables {
			
			public static <I, O> Function<Iterable<I>, Iterable<O>> liftBoth(final Function<I, O> function) {
				return iter -> Streams.stream(iter).map(function).collect(Collectors.toList());
			}

			public static <I, O> Function<Iterable<I>, Iterable<O>> bothFlat(final Function<I, Iterable<O>> function) {
				return iter -> Streams.stream(iter).flatMap(i -> Streams.stream(function.apply(i))).collect(Collectors.toList());
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

	public interface ComputerMaps {
		
		public interface Iterables {
			
			public static <I, O> Computers.Arity1<Iterable<I>, Iterable<O>> liftBoth(final Computers.Arity1<I, O> computer) {
				return (iter1, iter2) -> {
					Iterator<I> i1 = iter1.iterator();
					Iterator<O> i2 = iter2.iterator();
					while(i1.hasNext() && i2.hasNext()) {
						computer.compute(i1.next(), i2.next());
					}
				};
			}
		}
	}
}
