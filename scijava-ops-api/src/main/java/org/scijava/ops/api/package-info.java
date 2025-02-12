/*-
 * #%L
 * The public API of SciJava Ops.
 * %%
 * Copyright (C) 2021 - 2025 SciJava developers.
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
/**
 * This module contains the API necessary to retrieve, execute, and reason about
 * Ops.
 * <h2>What is the SciJava Ops Library?</h2>
 * <p>
 * SciJava Ops arose from a desire for simplicity - there are lots of different
 * algorithmic libraries, each with their own data structures, algorithm syntax,
 * and other requirements. With all of these differences, library
 * interoperability can be a daunting task. This can lead to algorithm
 * reimplementations, unmaintained code, and a lack of cooperation between
 * open-source communities. SciJava Ops seeks to unify different libraries under
 * a single framework, separating the development of new algorithms from the way
 * they are executed. Put more succinctly, the goals of SciJava Ops are the
 * following:
 * </p>
 * <ul>
 * <li><b>Usability.</b> All Ops should be called in the same way, and
 * implementation details should be hidden - this means that the user should
 * call an Op the same way, whether the algorithm is written in Java, Python, or
 * as a CUDA kernel.</li>
 * <li><b>Extensibility.</b> New Ops should be accommodated in a variety of
 * programming languages, and each language should provide minimally-invasive
 * methods for declaring code blocks as Ops. This also means that Ops should
 * accommodate writing Ops in one language, and then calling Ops using data
 * structures from a different language.</li>
 * <li><b>Generality.</b> Ops should allow any number of typed inputs, without
 * restriction on input types. With no restrictions on input types, the library
 * can seamlessly adapt new data structures tailored to performance or
 * expressiveness as they are developed.</li>
 * <li><b>Speed.</b> Calling Ops should be approximately as performant as
 * invocation in their natural setting (such as Python, C++, Java, or another
 * language entirely).</li>
 * </ul>
 * <h2>What is an Op?</h2>
 * <p>
 * An Op is an algorithm adhering to the following traits:
 * </p>
 * <ol>
 * <li>Ops are named - this name conveys an Op's purpose, and allows us to find
 * all Ops implementing a particular operation</li>
 * <li>Ops have a number of input and output parameters, each defined by a
 * type</li>
 * <li>Ops adhere to a {@link java.lang.FunctionalInterface}, defining how it
 * operates</li>
 * </ol>
 * <p>
 * Using the name and the combination of input and output parameters, we can
 * retrieve, or "match", any Op from within an {@link OpEnvironment}. Op calls
 * with the same name and specified inputs/outputs will be reproducible within a
 * particular Op environment.
 * </p>
 * <h2>Op Equivalence</h2>
 * <p>
 * To support the Op matching paradigm, we establish three types of equivalence:
 * </p>
 * <ol>
 * <li><b>Form Equivalence</b> implies that two objects (which could be Ops, or
 * Op parameters) theoretically draw from the same shared idea (such as an
 * addition operation, or an image, etc.)</li>
 * <li><b>Structural Equivalence</b> - Structural Equivalence means that two
 * Ops:
 * <ol>
 * <li>Are Form Equivalent</li>
 * <li>Have the same number of inputs and the same number of outputs</li>
 * <li>For each input position, accept form-equivalent inputs</li>
 * <li>Return form-equivalent outputs</li>
 * </ol>
 * </li>
 * <li><b>Result Equivalence</b> means that {@code o1.equals(o2)} for two
 * outputs {@code o1} and {@code o2} from two Ops.</li>
 * </ol>
 * <p>
 * Within the Ops API, each type of equivalence is utilized in the following
 * ways:
 * </p>
 * <ol>
 * <li><b>Form Equivalence.</b> If two Ops are form-equivalent, they are defined
 * under the same name.</li>
 * <li><b>Structural Equivalence.</b> If two Ops are structural-equivalent, they
 * share a common form-reduced description, searchable using
 * {@link OpEnvironment#help(String)}. For example, an Op "math.add" that
 * produces an ImgLib2 Img from Img operands, and another Op "math.add" that
 * produces a NumPy ndarray from ndarray operands, will reduce to a single
 * description "math.add" that produces an image from image operands.</li>
 * <li><b>Result Equivalence.</b> If two Ops are result-equivalent, they produce
 * equivalent values, using the primary language-specific definition of equality
 * (e.g. `Object.equals`, for Java usage).</li>
 * </ol>
 * <p>
 * For example, consider three Ops:
 * </p>
 * <ol>
 * <li>filter.gauss(net.imglib2.img.Img,
 * net.imglib2.type.numeric.real.FloatType) &rarr; net.imglib2.img.Img</li>
 * <li>filter.gauss(ij.ImagePlus, java.lang.Double) &rarr; ij.ImagePlus</li>
 * <li>filter.gauss(net.imglib2.img.Img,
 * net.imglib2.algorithm.neighborhood.Shape) &rarr; net.imglib2.img.Img</li>
 * </ol>
 * <p>
 * Ops 1 and 2 are considered form-equivalent, as they have the same name, and
 * structural-equivalent, as they both take in an image data structure and a
 * floating point number and return an image data structure, but may not be
 * result-equivalent due to implementation differences between the two Ops, or
 * precision loss of the data structures.
 * </p>
 * <p>
 * Ops 1 and 3 are also form-equivalent, as they have the same name, but are not
 * structural-equivalent, as one has an implicit Shape over which to perform a
 * gaussian blur, while the other uses an explicitly specified shape.
 * </p>
 * <p>
 * These definitions of equivalence provide clarity when articulating the Ops
 * framework's benefits:
 * </p>
 * <ol>
 * <li>Form-equivalence in Ops allows a comprehensive search of the available
 * algorithms for accomplishing a particular algorithm, by searching its
 * name</li>
 * <li>Form-equivalence in data inputs allows us to consider algorithm inputs in
 * a library-agnostic way, making it easier to understand and use
 * algorithms</li>
 * <li>Structural-equivalence in Ops allows us to consider similar Ops in
 * different languages as one, and to delegate to the proper Op using user
 * inputs. In other words, you can call structural-equivalent Ops identically,
 * and SciJava Ops will take care to call the correct Op based on the concrete
 * inputs provided.</li>
 * <li>Result-equivalence, and therefore reproducibility, in Ops, is guaranteed
 * within an OpEnvironment and a set of input objects, when <b>deterministic</b>
 * algorithms are used. Cognizance of algorithm determinism allows users to
 * create reproducible pipelines, however determinism is <b>not</b> a
 * requirement for Ops, as it would preclude many valuable algorithms from
 * becoming an Op.</li>
 * </ol>
 */

package org.scijava.ops.api;
