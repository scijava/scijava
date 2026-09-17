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

import org.scijava.index.Indexable;
import org.scijava.priority.Priority;

/**
 * Declares a class as an extension of some type, discoverable by that type.
 * <p>
 * Annotate a class and it is discoverable - no configuration file to write, no
 * {@code module-info} clause to remember:
 * </p>
 *
 * <pre>
 * &#64;Plugin(type = Converter.class, priority = Priority.HIGH)
 * public class MyConverter implements Converter { ... }
 * </pre>
 * <p>
 * An annotation processor records these at build time, so the container can
 * list what is available, and read each one's metadata, <em>without loading a
 * single plugin class</em>. That is what keeps a menu of a thousand commands
 * cheap to build.
 * </p>
 * <h2>When to use this, and when to use a service</h2>
 * <p>
 * This is for the long tail: commands, converters, widgets, formats, tools -
 * many implementations, contributed widely, of which a given session uses few.
 * {@link Service}s are different: a small, framework-level set, each the one
 * implementation of its interface, declared with {@code provides} in a
 * {@code module-info}. A service is not a plugin.
 * </p>
 * <h2>JPMS</h2>
 * <p>
 * The container constructs plugins reflectively, so a modular project must
 * open the package containing them - to the container only:
 * </p>
 *
 * <pre>
 * opens com.example.plugins to org.scijava.context;
 * </pre>
 * <p>
 * NB: {@code opens} is not {@code exports}. The package stays invisible to
 * ordinary callers at compile time - they cannot import the class, cast to it,
 * or call anything beyond the type it provides - while the container gets the
 * reflective access it needs.
 * </p>
 *
 * @author Curtis Rueden
 * @see Context#plugins(Class)
 */
@Indexable
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Plugin {

	/** The type of extension this plugin provides. */
	Class<?> type();

	/** An identifier for this plugin, unique among those of its type. */
	String name() default "";

	/** A human-readable label, for display. */
	String label() default "";

	/** A longer human-readable description. */
	String description() default "";

	/**
	 * Sorts plugins of the same type, highest first. See {@link Priority}.
	 */
	double priority() default Priority.NORMAL;

	/** Arbitrary additional metadata. */
	Attr[] attrs() default {};
}
