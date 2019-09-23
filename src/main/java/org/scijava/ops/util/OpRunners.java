package org.scijava.ops.util;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Functions;
import org.scijava.ops.function.Inplaces;
import org.scijava.ops.function.Producer;
import org.scijava.ops.transform.OpRunner;
import org.scijava.ops.types.Nil;

// 1. Implement all of the transformers for all types that need to be runnable
// 2. Improve the matcher to respect the ops that implement KnowsTypes
public class OpRunners {
	public static class FunctionRunner {
		
		public static <O> OpRunner toRunner(Supplier<O> function) {
			return new OpRunner() {
				
				@Override
				public Object getAdaptedOp() {
					return function;
				}

				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] {};
				}

				@Override
				public O run(Object[] args) {
					return function.get();
				}

			};
		}

		public static <I, O> OpRunner toRunner(Function<I, O> function) {
			return new OpRunner() {
				
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

		public static <I1, I2, O> OpRunner toRunner(BiFunction<I1, I2, O> function) {
			return new OpRunner() {

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

		public static <I1, I2, I3, O> OpRunner toRunner(Functions.Arity3<I1, I2, I3, O> function) {
			return new OpRunner() {

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
	}

	public static class ComputerRunner {
		public static <O> OpRunner toRunner(Computers.Arity0<O> computer) {
			return new OpRunner() {

				@Override
				public Object getAdaptedOp() {
					return computer;
				}
				
				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] {};
				}

				@SuppressWarnings("unchecked")
				@Override
				public O run(Object[] args) {
					Producer<O> source = () -> (O) args[0];
					return Adapt.ComputerAdapt.asFunction(computer, source).get();
				}

			};
		}
		
		public static <I, O> OpRunner toRunner(Computers.Arity1<I, O> computer) {
			return new OpRunner() {

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
					Function<I, O> source = (in) -> (O) args[1];
					return Adapt.ComputerAdapt.asFunction(computer, source).apply((I) args[0]);
				}

			};
		}

		public static <I1, I2, O> OpRunner toRunner(Computers.Arity2<I1, I2, O> computer) {
			return new OpRunner() {

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
					Function<I1, O> source = (in1) -> (O) args[2];
					return Adapt.ComputerAdapt.asBiFunction(computer, source).apply((I1) args[0], (I2) args[1]);
				}

			};
		}

		public static <I1, I2, I3, O> OpRunner toRunner(Computers.Arity3<I1, I2, I3, O> computer) {
			return new OpRunner() {

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
					Function<I1, O> source = (in1) -> (O) args[3];
					return Adapt.ComputerAdapt.asFunction3(computer, source).apply((I1) args[0], (I2) args[1],
							(I3) args[2]);
				}

			};
		}
	}

	// INPLACES

	public static class InplaceRunner {
		public static <IO> OpRunner toRunner(Inplaces.Arity1<IO> inplace) {
			return new OpRunner() {
				
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
					return Adapt.InplaceAdapt.asFunction(inplace).apply((IO) args[0]);
				}

			};
		}

		public static <IO, I2> OpRunner toRunner(Inplaces.Arity2_1<IO, I2> inplace) {
			return new OpRunner() {
				
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
					return Adapt.InplaceAdapt.asBiFunction(inplace).apply((IO) args[0], (I2) args[1]);
				}

			};
		}

		public static <I1, IO> OpRunner toRunner(Inplaces.Arity2_2<I1, IO> inplace) {
			return new OpRunner() {
				
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
					return Adapt.InplaceAdapt.asBiFunction(inplace).apply((I1) args[0], (IO) args[1]);
				}

			};
		}
		
		public static <IO, I2, I3> OpRunner toRunner(Inplaces.Arity3_1<IO, I2, I3> inplace) {
			return new OpRunner() {
				
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
					return Adapt.InplaceAdapt.asFunction3(inplace).apply((IO) args[0], (I2) args[1], (I3) args[2]);
				}

			};
		}
		
		public static <I1, IO, I3> OpRunner toRunner(Inplaces.Arity3_2<I1, IO, I3> inplace) {
			return new OpRunner() {
				
				@Override
				public Object getAdaptedOp() {
					return inplace;
				}
				
				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] { new Nil<I1>() {
					}, new Nil<IO>() {
					}, new Nil<I3>() {
					} };
				}

				@Override
				public IO run(Object[] args) {
					return Adapt.InplaceAdapt.asFunction3(inplace).apply((I1) args[0], (IO) args[1], (I3) args[2]);
				}

			};
		}
		
		public static <I1, I2, IO> OpRunner toRunner(Inplaces.Arity3_3<I1, I2, IO> inplace) {
			return new OpRunner() {
				
				@Override
				public Object getAdaptedOp() {
					return inplace;
				}
				
				@Override
				public Nil<?>[] inTypes() {
					return new Nil<?>[] { new Nil<I1>() {
					}, new Nil<I1>() {
					}, new Nil<IO>() {
					} };
				}

				@Override
				public IO run(Object[] args) {
					return Adapt.InplaceAdapt.asFunction3(inplace).apply((I1) args[0], (I2) args[1], (IO) args[2]);
				}

			};
		}
	}
}
