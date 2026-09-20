/*
 * #%L
 * An application container: services, discovered and wired.
 * %%
 * Copyright (C) 2026 SciJava developers.
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

package org.scijava.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field to be filled in by the {@link Context}.
 *
 * <pre>
 * public class MyService implements Service {
 *
 * 	&#64;Dependency
 * 	private LogService log;
 * }
 * </pre>
 * <p>
 * The context injects services it owns, plus the {@link Context} itself and
 * its {@link org.scijava.events.EventBus}. Injection happens after
 * construction and before {@link Service#initialize(Context)}, so a dependency
 * is ready to use by the time initialization runs.
 * </p>
 * <p>
 * NB: this is deliberately not called {@code @Parameter}, as it was in SciJava
 * Common. There it meant two unrelated things - a dependency to inject, and an
 * input to a module - and the second is now the business of
 * {@code org.scijava.struct}.
 * </p>
 * <h2>JPMS</h2>
 * <p>
 * Injecting a private field is deep reflection, so a modular project must open
 * the package to the container:
 * </p>
 *
 * <pre>
 * opens com.example.services to org.scijava.context;
 * </pre>
 * <p>
 * A package that is opened but not exported stays invisible to ordinary
 * callers: they cannot import these classes or cast to them. Only the
 * container gets in.
 * </p>
 *
 * @author Curtis Rueden
 * @see Context#inject(Object)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Dependency {

	/**
	 * Whether the context must have such a dependency. When false, a missing
	 * one leaves the field null rather than failing.
	 */
	boolean required() default true;
}
