/*
 * #%L
 * SciJava Operations: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2019 SciJava Ops developers.
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

/*
* This is autogenerated source code -- DO NOT EDIT. Instead, edit the
* corresponding template in templates/ and rerun bin/generate.groovy.
*/

package org.scijava.ops.adapt.lift;

import java.util.Iterator;
import java.util.function.Function;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.function.Computers;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Converts {@link Computers} operating on single types to {@link Computers}
 * that operate on {@link Iterable}s of types. N.B. it is the user's
 * responsibility to pass {@link Iterable}s of the same length (otherwise the Op
 * will stop when one of the {@link Iterable}s runs out of {@link Object}s).
 * 
 * @author Gabriel Selzer
 */
@Plugin(type = OpCollection.class)
public class ComputerToIterables<I, I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O> {

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Computers.Arity0<O>, Computers.Arity0<Iterable<O>>> liftComputer0 = 
		(computer) -> {
			return (out) -> {
				Iterator<O> itrout = out.iterator();
				while (itrout.hasNext()) {
					computer.compute(itrout.next());
				}
			};
		};

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Computers.Arity1<I, O>, Computers.Arity1<Iterable<I>, Iterable<O>>> liftComputer1 = 
		(computer) -> {
			return (in, out) -> {
				Iterator<I> itrin = in.iterator();
				Iterator<O> itrout = out.iterator();
				while (itrin.hasNext() && itrout.hasNext()) {
					computer.compute(itrin.next(), itrout.next());
				}
			};
		};

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Computers.Arity2<I1, I2, O>, Computers.Arity2<Iterable<I1>, Iterable<I2>, Iterable<O>>> liftComputer2 = 
		(computer) -> {
			return (in1, in2, out) -> {
				Iterator<I1> itrin1 = in1.iterator();
				Iterator<I2> itrin2 = in2.iterator();
				Iterator<O> itrout = out.iterator();
				while (itrin1.hasNext() && itrin2.hasNext() && itrout.hasNext()) {
					computer.compute(itrin1.next(), itrin2.next(), itrout.next());
				}
			};
		};

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Computers.Arity3<I1, I2, I3, O>, Computers.Arity3<Iterable<I1>, Iterable<I2>, Iterable<I3>, Iterable<O>>> liftComputer3 = 
		(computer) -> {
			return (in1, in2, in3, out) -> {
				Iterator<I1> itrin1 = in1.iterator();
				Iterator<I2> itrin2 = in2.iterator();
				Iterator<I3> itrin3 = in3.iterator();
				Iterator<O> itrout = out.iterator();
				while (itrin1.hasNext() && itrin2.hasNext() && itrin3.hasNext() && itrout.hasNext()) {
					computer.compute(itrin1.next(), itrin2.next(), itrin3.next(), itrout.next());
				}
			};
		};

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Computers.Arity4<I1, I2, I3, I4, O>, Computers.Arity4<Iterable<I1>, Iterable<I2>, Iterable<I3>, Iterable<I4>, Iterable<O>>> liftComputer4 = 
		(computer) -> {
			return (in1, in2, in3, in4, out) -> {
				Iterator<I1> itrin1 = in1.iterator();
				Iterator<I2> itrin2 = in2.iterator();
				Iterator<I3> itrin3 = in3.iterator();
				Iterator<I4> itrin4 = in4.iterator();
				Iterator<O> itrout = out.iterator();
				while (itrin1.hasNext() && itrin2.hasNext() && itrin3.hasNext() && itrin4.hasNext() && itrout.hasNext()) {
					computer.compute(itrin1.next(), itrin2.next(), itrin3.next(), itrin4.next(), itrout.next());
				}
			};
		};

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Computers.Arity5<I1, I2, I3, I4, I5, O>, Computers.Arity5<Iterable<I1>, Iterable<I2>, Iterable<I3>, Iterable<I4>, Iterable<I5>, Iterable<O>>> liftComputer5 = 
		(computer) -> {
			return (in1, in2, in3, in4, in5, out) -> {
				Iterator<I1> itrin1 = in1.iterator();
				Iterator<I2> itrin2 = in2.iterator();
				Iterator<I3> itrin3 = in3.iterator();
				Iterator<I4> itrin4 = in4.iterator();
				Iterator<I5> itrin5 = in5.iterator();
				Iterator<O> itrout = out.iterator();
				while (itrin1.hasNext() && itrin2.hasNext() && itrin3.hasNext() && itrin4.hasNext() && itrin5.hasNext() && itrout.hasNext()) {
					computer.compute(itrin1.next(), itrin2.next(), itrin3.next(), itrin4.next(), itrin5.next(), itrout.next());
				}
			};
		};

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Computers.Arity6<I1, I2, I3, I4, I5, I6, O>, Computers.Arity6<Iterable<I1>, Iterable<I2>, Iterable<I3>, Iterable<I4>, Iterable<I5>, Iterable<I6>, Iterable<O>>> liftComputer6 = 
		(computer) -> {
			return (in1, in2, in3, in4, in5, in6, out) -> {
				Iterator<I1> itrin1 = in1.iterator();
				Iterator<I2> itrin2 = in2.iterator();
				Iterator<I3> itrin3 = in3.iterator();
				Iterator<I4> itrin4 = in4.iterator();
				Iterator<I5> itrin5 = in5.iterator();
				Iterator<I6> itrin6 = in6.iterator();
				Iterator<O> itrout = out.iterator();
				while (itrin1.hasNext() && itrin2.hasNext() && itrin3.hasNext() && itrin4.hasNext() && itrin5.hasNext() && itrin6.hasNext() && itrout.hasNext()) {
					computer.compute(itrin1.next(), itrin2.next(), itrin3.next(), itrin4.next(), itrin5.next(), itrin6.next(), itrout.next());
				}
			};
		};

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Computers.Arity7<I1, I2, I3, I4, I5, I6, I7, O>, Computers.Arity7<Iterable<I1>, Iterable<I2>, Iterable<I3>, Iterable<I4>, Iterable<I5>, Iterable<I6>, Iterable<I7>, Iterable<O>>> liftComputer7 = 
		(computer) -> {
			return (in1, in2, in3, in4, in5, in6, in7, out) -> {
				Iterator<I1> itrin1 = in1.iterator();
				Iterator<I2> itrin2 = in2.iterator();
				Iterator<I3> itrin3 = in3.iterator();
				Iterator<I4> itrin4 = in4.iterator();
				Iterator<I5> itrin5 = in5.iterator();
				Iterator<I6> itrin6 = in6.iterator();
				Iterator<I7> itrin7 = in7.iterator();
				Iterator<O> itrout = out.iterator();
				while (itrin1.hasNext() && itrin2.hasNext() && itrin3.hasNext() && itrin4.hasNext() && itrin5.hasNext() && itrin6.hasNext() && itrin7.hasNext() && itrout.hasNext()) {
					computer.compute(itrin1.next(), itrin2.next(), itrin3.next(), itrin4.next(), itrin5.next(), itrin6.next(), itrin7.next(), itrout.next());
				}
			};
		};

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Computers.Arity8<I1, I2, I3, I4, I5, I6, I7, I8, O>, Computers.Arity8<Iterable<I1>, Iterable<I2>, Iterable<I3>, Iterable<I4>, Iterable<I5>, Iterable<I6>, Iterable<I7>, Iterable<I8>, Iterable<O>>> liftComputer8 = 
		(computer) -> {
			return (in1, in2, in3, in4, in5, in6, in7, in8, out) -> {
				Iterator<I1> itrin1 = in1.iterator();
				Iterator<I2> itrin2 = in2.iterator();
				Iterator<I3> itrin3 = in3.iterator();
				Iterator<I4> itrin4 = in4.iterator();
				Iterator<I5> itrin5 = in5.iterator();
				Iterator<I6> itrin6 = in6.iterator();
				Iterator<I7> itrin7 = in7.iterator();
				Iterator<I8> itrin8 = in8.iterator();
				Iterator<O> itrout = out.iterator();
				while (itrin1.hasNext() && itrin2.hasNext() && itrin3.hasNext() && itrin4.hasNext() && itrin5.hasNext() && itrin6.hasNext() && itrin7.hasNext() && itrin8.hasNext() && itrout.hasNext()) {
					computer.compute(itrin1.next(), itrin2.next(), itrin3.next(), itrin4.next(), itrin5.next(), itrin6.next(), itrin7.next(), itrin8.next(), itrout.next());
				}
			};
		};

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Computers.Arity9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O>, Computers.Arity9<Iterable<I1>, Iterable<I2>, Iterable<I3>, Iterable<I4>, Iterable<I5>, Iterable<I6>, Iterable<I7>, Iterable<I8>, Iterable<I9>, Iterable<O>>> liftComputer9 = 
		(computer) -> {
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, out) -> {
				Iterator<I1> itrin1 = in1.iterator();
				Iterator<I2> itrin2 = in2.iterator();
				Iterator<I3> itrin3 = in3.iterator();
				Iterator<I4> itrin4 = in4.iterator();
				Iterator<I5> itrin5 = in5.iterator();
				Iterator<I6> itrin6 = in6.iterator();
				Iterator<I7> itrin7 = in7.iterator();
				Iterator<I8> itrin8 = in8.iterator();
				Iterator<I9> itrin9 = in9.iterator();
				Iterator<O> itrout = out.iterator();
				while (itrin1.hasNext() && itrin2.hasNext() && itrin3.hasNext() && itrin4.hasNext() && itrin5.hasNext() && itrin6.hasNext() && itrin7.hasNext() && itrin8.hasNext() && itrin9.hasNext() && itrout.hasNext()) {
					computer.compute(itrin1.next(), itrin2.next(), itrin3.next(), itrin4.next(), itrin5.next(), itrin6.next(), itrin7.next(), itrin8.next(), itrin9.next(), itrout.next());
				}
			};
		};

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Computers.Arity10<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O>, Computers.Arity10<Iterable<I1>, Iterable<I2>, Iterable<I3>, Iterable<I4>, Iterable<I5>, Iterable<I6>, Iterable<I7>, Iterable<I8>, Iterable<I9>, Iterable<I10>, Iterable<O>>> liftComputer10 = 
		(computer) -> {
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, out) -> {
				Iterator<I1> itrin1 = in1.iterator();
				Iterator<I2> itrin2 = in2.iterator();
				Iterator<I3> itrin3 = in3.iterator();
				Iterator<I4> itrin4 = in4.iterator();
				Iterator<I5> itrin5 = in5.iterator();
				Iterator<I6> itrin6 = in6.iterator();
				Iterator<I7> itrin7 = in7.iterator();
				Iterator<I8> itrin8 = in8.iterator();
				Iterator<I9> itrin9 = in9.iterator();
				Iterator<I10> itrin10 = in10.iterator();
				Iterator<O> itrout = out.iterator();
				while (itrin1.hasNext() && itrin2.hasNext() && itrin3.hasNext() && itrin4.hasNext() && itrin5.hasNext() && itrin6.hasNext() && itrin7.hasNext() && itrin8.hasNext() && itrin9.hasNext() && itrin10.hasNext() && itrout.hasNext()) {
					computer.compute(itrin1.next(), itrin2.next(), itrin3.next(), itrin4.next(), itrin5.next(), itrin6.next(), itrin7.next(), itrin8.next(), itrin9.next(), itrin10.next(), itrout.next());
				}
			};
		};

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Computers.Arity11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O>, Computers.Arity11<Iterable<I1>, Iterable<I2>, Iterable<I3>, Iterable<I4>, Iterable<I5>, Iterable<I6>, Iterable<I7>, Iterable<I8>, Iterable<I9>, Iterable<I10>, Iterable<I11>, Iterable<O>>> liftComputer11 = 
		(computer) -> {
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, out) -> {
				Iterator<I1> itrin1 = in1.iterator();
				Iterator<I2> itrin2 = in2.iterator();
				Iterator<I3> itrin3 = in3.iterator();
				Iterator<I4> itrin4 = in4.iterator();
				Iterator<I5> itrin5 = in5.iterator();
				Iterator<I6> itrin6 = in6.iterator();
				Iterator<I7> itrin7 = in7.iterator();
				Iterator<I8> itrin8 = in8.iterator();
				Iterator<I9> itrin9 = in9.iterator();
				Iterator<I10> itrin10 = in10.iterator();
				Iterator<I11> itrin11 = in11.iterator();
				Iterator<O> itrout = out.iterator();
				while (itrin1.hasNext() && itrin2.hasNext() && itrin3.hasNext() && itrin4.hasNext() && itrin5.hasNext() && itrin6.hasNext() && itrin7.hasNext() && itrin8.hasNext() && itrin9.hasNext() && itrin10.hasNext() && itrin11.hasNext() && itrout.hasNext()) {
					computer.compute(itrin1.next(), itrin2.next(), itrin3.next(), itrin4.next(), itrin5.next(), itrin6.next(), itrin7.next(), itrin8.next(), itrin9.next(), itrin10.next(), itrin11.next(), itrout.next());
				}
			};
		};

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Computers.Arity12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O>, Computers.Arity12<Iterable<I1>, Iterable<I2>, Iterable<I3>, Iterable<I4>, Iterable<I5>, Iterable<I6>, Iterable<I7>, Iterable<I8>, Iterable<I9>, Iterable<I10>, Iterable<I11>, Iterable<I12>, Iterable<O>>> liftComputer12 = 
		(computer) -> {
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, out) -> {
				Iterator<I1> itrin1 = in1.iterator();
				Iterator<I2> itrin2 = in2.iterator();
				Iterator<I3> itrin3 = in3.iterator();
				Iterator<I4> itrin4 = in4.iterator();
				Iterator<I5> itrin5 = in5.iterator();
				Iterator<I6> itrin6 = in6.iterator();
				Iterator<I7> itrin7 = in7.iterator();
				Iterator<I8> itrin8 = in8.iterator();
				Iterator<I9> itrin9 = in9.iterator();
				Iterator<I10> itrin10 = in10.iterator();
				Iterator<I11> itrin11 = in11.iterator();
				Iterator<I12> itrin12 = in12.iterator();
				Iterator<O> itrout = out.iterator();
				while (itrin1.hasNext() && itrin2.hasNext() && itrin3.hasNext() && itrin4.hasNext() && itrin5.hasNext() && itrin6.hasNext() && itrin7.hasNext() && itrin8.hasNext() && itrin9.hasNext() && itrin10.hasNext() && itrin11.hasNext() && itrin12.hasNext() && itrout.hasNext()) {
					computer.compute(itrin1.next(), itrin2.next(), itrin3.next(), itrin4.next(), itrin5.next(), itrin6.next(), itrin7.next(), itrin8.next(), itrin9.next(), itrin10.next(), itrin11.next(), itrin12.next(), itrout.next());
				}
			};
		};

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Computers.Arity13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O>, Computers.Arity13<Iterable<I1>, Iterable<I2>, Iterable<I3>, Iterable<I4>, Iterable<I5>, Iterable<I6>, Iterable<I7>, Iterable<I8>, Iterable<I9>, Iterable<I10>, Iterable<I11>, Iterable<I12>, Iterable<I13>, Iterable<O>>> liftComputer13 = 
		(computer) -> {
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, out) -> {
				Iterator<I1> itrin1 = in1.iterator();
				Iterator<I2> itrin2 = in2.iterator();
				Iterator<I3> itrin3 = in3.iterator();
				Iterator<I4> itrin4 = in4.iterator();
				Iterator<I5> itrin5 = in5.iterator();
				Iterator<I6> itrin6 = in6.iterator();
				Iterator<I7> itrin7 = in7.iterator();
				Iterator<I8> itrin8 = in8.iterator();
				Iterator<I9> itrin9 = in9.iterator();
				Iterator<I10> itrin10 = in10.iterator();
				Iterator<I11> itrin11 = in11.iterator();
				Iterator<I12> itrin12 = in12.iterator();
				Iterator<I13> itrin13 = in13.iterator();
				Iterator<O> itrout = out.iterator();
				while (itrin1.hasNext() && itrin2.hasNext() && itrin3.hasNext() && itrin4.hasNext() && itrin5.hasNext() && itrin6.hasNext() && itrin7.hasNext() && itrin8.hasNext() && itrin9.hasNext() && itrin10.hasNext() && itrin11.hasNext() && itrin12.hasNext() && itrin13.hasNext() && itrout.hasNext()) {
					computer.compute(itrin1.next(), itrin2.next(), itrin3.next(), itrin4.next(), itrin5.next(), itrin6.next(), itrin7.next(), itrin8.next(), itrin9.next(), itrin10.next(), itrin11.next(), itrin12.next(), itrin13.next(), itrout.next());
				}
			};
		};

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Computers.Arity14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O>, Computers.Arity14<Iterable<I1>, Iterable<I2>, Iterable<I3>, Iterable<I4>, Iterable<I5>, Iterable<I6>, Iterable<I7>, Iterable<I8>, Iterable<I9>, Iterable<I10>, Iterable<I11>, Iterable<I12>, Iterable<I13>, Iterable<I14>, Iterable<O>>> liftComputer14 = 
		(computer) -> {
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, out) -> {
				Iterator<I1> itrin1 = in1.iterator();
				Iterator<I2> itrin2 = in2.iterator();
				Iterator<I3> itrin3 = in3.iterator();
				Iterator<I4> itrin4 = in4.iterator();
				Iterator<I5> itrin5 = in5.iterator();
				Iterator<I6> itrin6 = in6.iterator();
				Iterator<I7> itrin7 = in7.iterator();
				Iterator<I8> itrin8 = in8.iterator();
				Iterator<I9> itrin9 = in9.iterator();
				Iterator<I10> itrin10 = in10.iterator();
				Iterator<I11> itrin11 = in11.iterator();
				Iterator<I12> itrin12 = in12.iterator();
				Iterator<I13> itrin13 = in13.iterator();
				Iterator<I14> itrin14 = in14.iterator();
				Iterator<O> itrout = out.iterator();
				while (itrin1.hasNext() && itrin2.hasNext() && itrin3.hasNext() && itrin4.hasNext() && itrin5.hasNext() && itrin6.hasNext() && itrin7.hasNext() && itrin8.hasNext() && itrin9.hasNext() && itrin10.hasNext() && itrin11.hasNext() && itrin12.hasNext() && itrin13.hasNext() && itrin14.hasNext() && itrout.hasNext()) {
					computer.compute(itrin1.next(), itrin2.next(), itrin3.next(), itrin4.next(), itrin5.next(), itrin6.next(), itrin7.next(), itrin8.next(), itrin9.next(), itrin10.next(), itrin11.next(), itrin12.next(), itrin13.next(), itrin14.next(), itrout.next());
				}
			};
		};

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Computers.Arity15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O>, Computers.Arity15<Iterable<I1>, Iterable<I2>, Iterable<I3>, Iterable<I4>, Iterable<I5>, Iterable<I6>, Iterable<I7>, Iterable<I8>, Iterable<I9>, Iterable<I10>, Iterable<I11>, Iterable<I12>, Iterable<I13>, Iterable<I14>, Iterable<I15>, Iterable<O>>> liftComputer15 = 
		(computer) -> {
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15, out) -> {
				Iterator<I1> itrin1 = in1.iterator();
				Iterator<I2> itrin2 = in2.iterator();
				Iterator<I3> itrin3 = in3.iterator();
				Iterator<I4> itrin4 = in4.iterator();
				Iterator<I5> itrin5 = in5.iterator();
				Iterator<I6> itrin6 = in6.iterator();
				Iterator<I7> itrin7 = in7.iterator();
				Iterator<I8> itrin8 = in8.iterator();
				Iterator<I9> itrin9 = in9.iterator();
				Iterator<I10> itrin10 = in10.iterator();
				Iterator<I11> itrin11 = in11.iterator();
				Iterator<I12> itrin12 = in12.iterator();
				Iterator<I13> itrin13 = in13.iterator();
				Iterator<I14> itrin14 = in14.iterator();
				Iterator<I15> itrin15 = in15.iterator();
				Iterator<O> itrout = out.iterator();
				while (itrin1.hasNext() && itrin2.hasNext() && itrin3.hasNext() && itrin4.hasNext() && itrin5.hasNext() && itrin6.hasNext() && itrin7.hasNext() && itrin8.hasNext() && itrin9.hasNext() && itrin10.hasNext() && itrin11.hasNext() && itrin12.hasNext() && itrin13.hasNext() && itrin14.hasNext() && itrin15.hasNext() && itrout.hasNext()) {
					computer.compute(itrin1.next(), itrin2.next(), itrin3.next(), itrin4.next(), itrin5.next(), itrin6.next(), itrin7.next(), itrin8.next(), itrin9.next(), itrin10.next(), itrin11.next(), itrin12.next(), itrin13.next(), itrin14.next(), itrin15.next(), itrout.next());
				}
			};
		};

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Computers.Arity16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O>, Computers.Arity16<Iterable<I1>, Iterable<I2>, Iterable<I3>, Iterable<I4>, Iterable<I5>, Iterable<I6>, Iterable<I7>, Iterable<I8>, Iterable<I9>, Iterable<I10>, Iterable<I11>, Iterable<I12>, Iterable<I13>, Iterable<I14>, Iterable<I15>, Iterable<I16>, Iterable<O>>> liftComputer16 = 
		(computer) -> {
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15, in16, out) -> {
				Iterator<I1> itrin1 = in1.iterator();
				Iterator<I2> itrin2 = in2.iterator();
				Iterator<I3> itrin3 = in3.iterator();
				Iterator<I4> itrin4 = in4.iterator();
				Iterator<I5> itrin5 = in5.iterator();
				Iterator<I6> itrin6 = in6.iterator();
				Iterator<I7> itrin7 = in7.iterator();
				Iterator<I8> itrin8 = in8.iterator();
				Iterator<I9> itrin9 = in9.iterator();
				Iterator<I10> itrin10 = in10.iterator();
				Iterator<I11> itrin11 = in11.iterator();
				Iterator<I12> itrin12 = in12.iterator();
				Iterator<I13> itrin13 = in13.iterator();
				Iterator<I14> itrin14 = in14.iterator();
				Iterator<I15> itrin15 = in15.iterator();
				Iterator<I16> itrin16 = in16.iterator();
				Iterator<O> itrout = out.iterator();
				while (itrin1.hasNext() && itrin2.hasNext() && itrin3.hasNext() && itrin4.hasNext() && itrin5.hasNext() && itrin6.hasNext() && itrin7.hasNext() && itrin8.hasNext() && itrin9.hasNext() && itrin10.hasNext() && itrin11.hasNext() && itrin12.hasNext() && itrin13.hasNext() && itrin14.hasNext() && itrin15.hasNext() && itrin16.hasNext() && itrout.hasNext()) {
					computer.compute(itrin1.next(), itrin2.next(), itrin3.next(), itrin4.next(), itrin5.next(), itrin6.next(), itrin7.next(), itrin8.next(), itrin9.next(), itrin10.next(), itrin11.next(), itrin12.next(), itrin13.next(), itrin14.next(), itrin15.next(), itrin16.next(), itrout.next());
				}
			};
		};

}

