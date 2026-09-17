/*
 * #%L
 * Running things that declare their inputs and outputs.
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

package org.scijava.execute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.scijava.struct.ItemIO;

/**
 * Declares a field as an input or output of the thing that declares it.
 *
 * <pre>
 * public class AddNumbers implements Runnable {
 *
 * 	&#64;Parameter
 * 	private double a;
 *
 * 	&#64;Parameter
 * 	private double b;
 *
 * 	&#64;Parameter(io = ItemIO.OUTPUT)
 * 	private double result;
 *
 * 	public void run() { result = a + b; }
 * }
 * </pre>
 * <p>
 * NB: this is the SciJava Common meaning of {@code @Parameter} - an input to
 * something runnable - and only that meaning. Injecting a service is
 * {@code org.scijava.context.Dependency}, a separate annotation, because
 * conflating the two is what made the original confusing.
 * </p>
 *
 * @author Curtis Rueden
 * @see Executables#struct(Class)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Parameter {

	/** Whether this parameter is an input, an output, or both. */
	ItemIO io() default ItemIO.INPUT;

	/** Whether a value must be supplied. */
	boolean required() default true;

	/** A human-readable label, for display. */
	String label() default "";

	/** A longer human-readable description. */
	String description() default "";

	/**
	 * A behavior to run when this parameter's value changes.
	 * <p>
	 * NB: a name, not a method reference, so that a script can supply one too;
	 * see {@link Behavior}.
	 * </p>
	 */
	String callback() default "";

	/**
	 * A behavior that checks this parameter's value, returning a message
	 * describing the problem, or null or an empty string if there is none.
	 */
	String validator() default "";

	/** A behavior deciding whether this parameter is currently shown. */
	String visibleWhen() default "";

	/** The group this parameter belongs to, if any. */
	String group() default "";

	/**
	 * The values this parameter may take, where they are known in advance.
	 *
	 * @see #choicesFrom()
	 */
	String[] choices() default {};

	/**
	 * A behavior producing the values this parameter may take, where they
	 * depend on runtime state - the axes of the open image, the fields of the
	 * chosen table.
	 * <p>
	 * NB: this is what SciJava Common needed {@code DynamicCommand} for. There,
	 * a command mutated its own {@code ModuleInfo} to call {@code setChoices},
	 * which made the description itself mutable and per-instance: a platform
	 * could not know a command's parameters without constructing it. Declaring
	 * that the choices come from a behavior keeps the parameter statically
	 * visible while its values stay dynamic.
	 * </p>
	 */
	String choicesFrom() default "";

	/** The smallest value this parameter may take, for a numeric parameter. */
	String min() default "";

	/** The largest value this parameter may take, for a numeric parameter. */
	String max() default "";

	/** How far one step moves a numeric parameter, in a spinner or slider. */
	String stepSize() default "";

	/**
	 * A hint about how to display this parameter, for a user interface to
	 * interpret: {@code "slider"}, {@code "password"}, {@code "directory"}.
	 * <p>
	 * Several hints are separated by commas, and one may carry a value after a
	 * colon: {@code "slider,format:0.00"}. A user interface ignores hints it
	 * does not recognize, so a parameter styled for one toolkit still renders
	 * in another.
	 * </p>
	 */
	String style() default "";
}
