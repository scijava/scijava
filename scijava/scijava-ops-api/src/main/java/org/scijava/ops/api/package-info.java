/**
 * This module contains the API necessary to retrieve, execute, and reason about Ops.
 * <h1>What is an Op?</h1>
 *
 * An Op is an algorithm adhering to the following traits:
 * <ol>
 * <li>Ops are stateless - with no internal state, calling an Op two times on the same inputs will produce the same output.</li>
 * <li>Ops are limited to one output - by limiting Ops to one output, we ensure that the algorithms we write are reusable building blocks. Of course, algorithms always need inputs, and there are no limitations on the number of inputs.</li>
 * <li>Ops have at least one name - this name conveys an Op's purpose</li>
 * </ol>
 * Using the name and the combination of input and output parameters, we can retrieve, or "match", any Op from within an {@link org.scijava.ops.api.OpEnvironment}. Op calls with the same name and specified inputs/outputs will be reproducible within a particular Op environment.
 *
 * <h1>Op Equivalence</h1>
 * To support the Op matching paradigm, we establish three types of equivalence:
 * <ol>
 *   <li><b>Form Equivalence</b></li> Drawn from <a href="https://en.wikipedia.org/wiki/Theory_of_forms">Plato's Theory of Forms</a>, Form Equivalence implies that two objects (which could be Ops, or Op parameters) theoretically draw from the same shared idea (such as an addition operation, or an image, etc.)
 *   <li><b>Op Equivalence</b></li> Algorithmic Equivalence means that two Ops:
 *   <ol>
 *     <li>Are Form Equivalent</li>
 *     <li>Have the same number of inputs and the same number of outputs</li>
 *     <li>For each input position, accept form equivalent types</li>
 *     <li>Return form equivalent output types</li>
 *   </ol>
 *   <li><b>Numerical Equivalence</b></li> Numerical Equivalence means that {@code o1.equals(o2)} for two outputs {@code o1} and {@code o2} from two Ops.
 * </ol>
 * Within the Ops API, each type of equivalence is utilized in the following ways:
 * <ol>
 *   <li><b>Form Equivalence</b></li> If two Ops are "Form Equivalent", they are defined under the same name.
 *   <li><b>Op Equivalence</b></li> If two Ops are "Op Equivalent", then they are considered to implement the same computational algorithm.
 *   <li><b>Numerical Equivalence</b></li> If two Ops are "Numerically Equivalent", then they are considered to output the same values, neglecting trivial differences such as floating-point errors.
 * </ol>
 *
 * For example, consider three Ops:
 * <ol>
 *   <li> filter.gauss(net.imglib2.img.Img, net.imglib2.type.numeric.real.FloatType) -> net.imglib2.img.Img</li>
 *   <li> filter.gauss(ij.ImagePlus, java.lang.Double) -> ij.ImagePlus</li>
 *   <li> filter.gauss(net.imglib2.img.Img, net.imglib2.algorithm.neighborhood.Shape) -> net.imglib2.img.Img</li>
 * </ol>
 * Ops 1 and 2 should be considered form equivalent, as they have the same name, and Op equivalent, as they both take in an image data structure and a floating point number and return an image data structure, but they are likely not numerically equivalent due to the underlying data structures.
 * <p>
 * Ops 1 and 3 should also be considered form equivalent, as they have the same name, but are not form equivalent, as one computes its own Shape over which to perform a gaussian blur, while the other takes a predefined shape.
 * <p>
 * These definitions of equivalence provide a level of flexibility impossible without the Ops API; form equivalence allows us to, for example, define equivalent Ops across programming languages and libraries, and then to create scripts that can run unaltered on data types from each of those languages and libraries. Within each form implementation, however, we ensure numeric equivalence and therefore reproducability
 *
 *
 */
package org.scijava.ops.api;
