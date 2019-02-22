package org.scijava.ops.util;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.core.Source;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.computer.Computer3;
import org.scijava.ops.core.computer.Computer4;
import org.scijava.ops.core.computer.Computer5;
import org.scijava.ops.core.function.Function3;
import org.scijava.ops.core.function.Function4;
import org.scijava.ops.core.function.Function5;
import org.scijava.ops.core.inplace.BiInplaceFirst;
import org.scijava.ops.core.inplace.BiInplaceSecond;
import org.scijava.ops.core.inplace.Inplace;
import org.scijava.ops.core.inplace.Inplace3First;
import org.scijava.ops.core.inplace.Inplace4First;
import org.scijava.ops.core.inplace.Inplace5First;
import org.scijava.ops.transform.OpRunner;
import org.scijava.ops.types.Nil;

// 1. Implement all of the transformers for all types that need to be runnable
// 2. Improve the matcher to respect the ops that implement KnowsTypes
public class OpRunners {
	public static class Functions {

		public static <I, O> OpRunner<O> toRunner(Function<I, O> function) {
			return new OpRunner<O>() {
				
				@Override
				public Object getAdaptedOp() {
					return function;
				}

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
				public Object getAdaptedOp() {
					return function;
				}
				
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
				public Object getAdaptedOp() {
					return function;
				}
				
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
				public Object getAdaptedOp() {
					return function;
				}
				
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
		
		public static <I1, I2, I3, I4, I5, O> OpRunner<O> toRunner(Function5<I1, I2, I3, I4, I5, O> function) {
			return new OpRunner<O>() {

				@Override
				public Object getAdaptedOp() {
					return function;
				}
				
				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] { new Nil<I1>() {
					}, new Nil<I2>() {
					}, new Nil<I3>() {
					}, new Nil<I4>() {
					}, new Nil<I5>() {
					} };
				}

				@SuppressWarnings("unchecked")
				@Override
				public O run(Object[] args) {
					return function.apply((I1) args[0], (I2) args[1], (I3) args[2], (I4) args[3], (I5) args[4]);
				}

			};
		}
	}

	public static class Computers {
		public static <I, O> OpRunner<O> toRunner(Computer<I, O> computer) {
			return new OpRunner<O>() {

				@Override
				public Object getAdaptedOp() {
					return computer;
				}
				
				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] { new Nil<I>() {
					} };
				}

				@SuppressWarnings("unchecked")
				@Override
				public O run(Object[] args) {
					Source<O> source = () -> (O) args[1];
					return Adapt.Computers.asFunction(computer, source).apply((I) args[0]);
				}

			};
		}

		public static <I1, I2, O> OpRunner<O> toRunner(BiComputer<I1, I2, O> computer) {
			return new OpRunner<O>() {

				@Override
				public Object getAdaptedOp() {
					return computer;
				}
				
				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] { new Nil<I1>() {
					}, new Nil<I2>() {
					} };
				}

				@SuppressWarnings("unchecked")
				@Override
				public O run(Object[] args) {
					Source<O> source = () -> (O) args[2];
					return Adapt.Computers.asBiFunction(computer, source).apply((I1) args[0], (I2) args[1]);
				}

			};
		}

		public static <I1, I2, I3, O> OpRunner<O> toRunner(Computer3<I1, I2, I3, O> computer) {
			return new OpRunner<O>() {

				@Override
				public Object getAdaptedOp() {
					return computer;
				}
				
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
					Source<O> source = () -> (O) args[3];
					return Adapt.Computers.asFunction3(computer, source).apply((I1) args[0], (I2) args[1],
							(I3) args[2]);
				}

			};
		}

		public static <I1, I2, I3, I4, O> OpRunner<O> toRunner(Computer4<I1, I2, I3, I4, O> computer) {
			return new OpRunner<O>() {

				@Override
				public Object getAdaptedOp() {
					return computer;
				}
				
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
					Source<O> source = () -> (O) args[4];
					return Adapt.Computers.asFunction4(computer, source).apply((I1) args[0], (I2) args[1],
							(I3) args[2], (I4) args[3]);
				}

			};
		}
		
		public static <I1, I2, I3, I4, I5, O> OpRunner<O> toRunner(Computer5<I1, I2, I3, I4, I5, O> computer) {
			return new OpRunner<O>() {

				@Override
				public Object getAdaptedOp() {
					return computer;
				}
				
				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] { new Nil<I1>() {
					}, new Nil<I2>() {
					}, new Nil<I3>() {
					}, new Nil<I4>() {
					}, new Nil<I5>() {
					} };
				}

				@SuppressWarnings("unchecked")
				@Override
				public O run(Object[] args) {
					Source<O> source = () -> (O) args[5];
					return Adapt.Computers.asFunction5(computer, source).apply((I1) args[0], (I2) args[1],
							(I3) args[2], (I4) args[3], (I5) args[4]);
				}

			};
		}
	}

	// INPLACES

	public static class Inplaces {
		public static <IO> OpRunner<IO> toRunner(Inplace<IO> inplace) {
			return new OpRunner<IO>() {
				
				@Override
				public Object getAdaptedOp() {
					return inplace;
				}
				
				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] { new Nil<IO>() {
					} };
				}

				@Override
				public IO run(Object[] args) {
					return Adapt.Inplaces.asFunction(inplace).apply((IO) args[0]);
				}

			};
		}

		public static <IO, I2> OpRunner<IO> toRunner(BiInplaceFirst<IO, I2> inplace) {
			return new OpRunner<IO>() {
				
				@Override
				public Object getAdaptedOp() {
					return inplace;
				}
				
				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] { new Nil<IO>() {
					}, new Nil<I2>() {
					} };
				}

				@Override
				public IO run(Object[] args) {
					return Adapt.Inplaces.asBiFunction(inplace).apply((IO) args[0], (I2) args[1]);
				}

			};
		}

		public static <I1, IO> OpRunner<IO> toRunner(BiInplaceSecond<I1, IO> inplace) {
			return new OpRunner<IO>() {
				
				@Override
				public Object getAdaptedOp() {
					return inplace;
				}
				
				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] { new Nil<I1>() {
					}, new Nil<IO>() {
					} };
				}

				@Override
				public IO run(Object[] args) {
					return Adapt.Inplaces.asBiFunction(inplace).apply((I1) args[0], (IO) args[1]);
				}

			};
		}
		
		public static <IO, I2, I3> OpRunner<IO> toRunner(Inplace3First<IO, I2, I3> inplace) {
			return new OpRunner<IO>() {
				
				@Override
				public Object getAdaptedOp() {
					return inplace;
				}
				
				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] { new Nil<IO>() {
					}, new Nil<I2>() {
					}, new Nil<I3>() {
					} };
				}

				@Override
				public IO run(Object[] args) {
					return Adapt.Inplaces.asFunction3(inplace).apply((IO) args[0], (I2) args[1], (I3) args[2]);
				}

			};
		}

		public static <IO, I2, I3, I4> OpRunner<IO> toRunner(Inplace4First<IO, I2, I3, I4> inplace) {
			return new OpRunner<IO>() {
				
				@Override
				public Object getAdaptedOp() {
					return inplace;
				}
				
				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] { new Nil<IO>() {
					}, new Nil<I2>() {
					}, new Nil<I3>() {
					}, new Nil<I4>() {
					} };
				}

				@Override
				public IO run(Object[] args) {
					return Adapt.Inplaces.asFunction4(inplace).apply((IO) args[0], (I2) args[1], (I3) args[2], (I4) args[3]);
				}

			};
		}

		public static <IO, I2, I3, I4, I5> OpRunner<IO> toRunner(Inplace5First<IO, I2, I3, I4, I5> inplace) {
			return new OpRunner<IO>() {
				
				@Override
				public Object getAdaptedOp() {
					return inplace;
				}
				
				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] { new Nil<IO>() {
					}, new Nil<I2>() {
					}, new Nil<I3>() {
					}, new Nil<I4>() {
					}, new Nil<I5>() {
					} };
				}

				@Override
				public IO run(Object[] args) {
					return Adapt.Inplaces.asFunction5(inplace).apply((IO) args[0], (I2) args[1], (I3) args[2], (I4) args[3], (I5) args[4]);
				}

			};
		}
	}
}
