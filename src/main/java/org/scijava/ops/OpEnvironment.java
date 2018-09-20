/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package org.scijava.ops;

import java.util.Collection;

import org.scijava.ops.matcher.OpInfo;
import org.scijava.ops.matcher.OpRef;

/**
 * An op environment is the top-level entry point into op execution. It provides
 * all the built-in functionality of ops in a single place, including:
 * <ul>
 * <li>The pool of available ops, from which candidates are chosen.</li>
 * <li>Type-safe, built-in method signatures for all op implementations.</li>
 * <li>Selection (a.k.a. "matching") of op implementations from {@link OpRef}
 * descriptors.</li>
 * </ul>
 * <p>
 * Customizing the {@link OpEnvironment} allows customization of any or all of
 * the above. Potential use cases include:
 * <ul>
 * <li>Limiting or extending the pool of available op implementations.</li>
 * <li>Caching op outputs to improve subsequent time performance.</li>
 * <li>Configuration of environment "hints" to improve performance in time or
 * space.</li>
 * </ul>
 * <p>
 * The default&mdash;but not necessarily <em>only</em>&mdash;op environment is
 * the {@link OpService} of the application. The environment can be modified by
 * using a {@link CustomOpEnvironment}, or by implementing this interface
 * directly.
 * </p>
 * 
 * @author Curtis Rueden
 * @see OpService
 */
public interface OpEnvironment {

	/** The available ops for this environment. */
	Collection<OpInfo> infos();
}
