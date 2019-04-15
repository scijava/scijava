package org.scijava.ops.util;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.computer.Computer3;
import org.scijava.ops.core.computer.Computer4;
import org.scijava.ops.core.function.Function3;
import org.scijava.ops.core.function.Function4;
import org.scijava.ops.transform.OpRunner;
import org.scijava.ops.types.Nil;

// 1. Implement all of the transformers for all types that need to be runnable
// 2. Improve the matcher to respect the ops that implement KnowsTypes
public class OpRunners {
	public static class Functions {

		public static <I, O> OpRunner<O> toRunner(Function<I, O> function) {
			return new OpRunner<O>() {

				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] { new Nil<I>() {
					} };
				}

				@SuppressWarnings("unchecked")
				@Override
				public O run(Object[] args) {
					return function.apply((I) args[0]);
				}

			};
		}

		public static <I1, I2, O> OpRunner<O> toRunner(BiFunction<I1, I2, O> function) {
			return new OpRunner<O>() {

				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] { new Nil<I1>() {
					}, new Nil<I2>() {
					} };
				}

				@SuppressWarnings("unchecked")
				@Override
				public O run(Object[] args) {
					return function.apply((I1) args[0], (I2) args[1]);
				}

			};
		}

		public static <I1, I2, I3, O> OpRunner<O> toRunner(Function3<I1, I2, I3, O> function) {
			return new OpRunner<O>() {

				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] { new Nil<I1>() {
					}, new Nil<I2>() {
					}, new Nil<I3>() {
					} };
				}

				@SuppressWarnings("unchecked")
				@Override
				public O run(Object[] args) {
					return function.apply((I1) args[0], (I2) args[1], (I3) args[2]);
				}

			};
		}

		public static <I1, I2, I3, I4, O> OpRunner<O> toRunner(Function4<I1, I2, I3, I4, O> function) {
			return new OpRunner<O>() {

				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] { new Nil<I1>() {
					}, new Nil<I2>() {
					}, new Nil<I3>() {
					}, new Nil<I4>() {
					} };
				}

				@SuppressWarnings("unchecked")
				@Override
				public O run(Object[] args) {
					return function.apply((I1) args[0], (I2) args[1], (I3) args[2], (I4) args[3]);
				}

			};
		}
	}

	public static class Computers {
		public static <I, O> OpRunner<O> toRunner(Computer<I, O> computer, Function<I, O> inputAwareSource){
			return new OpRunner<O>() {

				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] { new Nil<I>() {}};
				}

				@SuppressWarnings("unchecked")
				@Override
				public O run(Object[] args) {
					return Adapt.Computers.asFunction(computer, inputAwareSource).apply((I) args[0]);
				}

			};	
		}
		
		public static <I1, I2, O> OpRunner<O> toRunner(BiComputer<I1, I2, O> computer, BiFunction<I1, I2, O> inputAwareSource){
			return new OpRunner<O>() {

				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] { new Nil<I1>() {}, new Nil<I2>() {}};
				}

				@SuppressWarnings("unchecked")
				@Override
				public O run(Object[] args) {
					return Adapt.Computers.asBiFunction(computer, inputAwareSource).apply((I1) args[0], (I2) args[1]);
				}

			};	
		}
		
		public static <I1, I2, I3, O> OpRunner<O> toRunner(Computer3<I1, I2, I3, O> computer, Function3<I1, I2, I3, O> inputAwareSource){
			return new OpRunner<O>() {

				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] { new Nil<I1>() {}, new Nil<I2>() {}, new Nil<I3>() {}};
				}

				@SuppressWarnings("unchecked")
				@Override
				public O run(Object[] args) {
					return Adapt.Computers.asFunction3(computer, inputAwareSource).apply((I1) args[0], (I2) args[1], (I3) args[2]);
				}

			};	
		}
		
		public static <I1, I2, I3, I4, O> OpRunner<O> toRunner(Computer4<I1, I2, I3, I4, O> computer, Function4<I1, I2, I3, I4, O> inputAwareSource){
			return new OpRunner<O>() {

				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] { new Nil<I1>() {}, new Nil<I2>() {}, new Nil<I3>() {}, new Nil<I4>() {}};
				}

				@SuppressWarnings("unchecked")
				@Override
				public O run(Object[] args) {
					return Adapt.Computers.asFunction4(computer, inputAwareSource).apply((I1) args[0], (I2) args[1], (I3) args[2], (I4) args[3]);
				}

			};	
		}
	}

}
