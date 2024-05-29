/*
 * #%L
 * SciJava library for generic type reasoning.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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
 * This package offers type reasoning beyond only what is offered by SciJava
 * Common's {@link org.scijava.common3.Types} class. This package offers the
 * following additional features:
 * <ul>
 * <li>Reason about whether a collection of arguments (object instances, generic
 * types, or a mixture thereof) satisfy a given list of generic types, such as
 * those of a particular method signature.</li>
 * <li>Create {@link org.scijava.types.Nil} objects, which act as "typed null"
 * placeholders, and support generation of proxy instances of their associated
 * generic type, similar to (but less featureful than) how mocking frameworks
 * create mock objects.</li>
 * <li>Recover erased generic type information from object instances at runtime,
 * in an extensible way, via {@link org.scijava.types.extract.TypeExtractor}
 * plugins and the {@link org.scijava.types.extract.TypeReifier#reify} method.
 * E.g., you can learn that an object of class {@link java.util.HashMap} is
 * actually (or at least functionally) a {@code HashMap<String, Integer>}.</li>
 * </ul>
 */

package org.scijava.types;
