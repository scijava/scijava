/*
 * #%L
 * The model behind a parameter dialog: groups, dependencies, validation.
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

package org.scijava.harvest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a group of parameters: a box in a dialog, possibly collapsible,
 * possibly conditional, possibly generated.
 *
 * <pre>
 * &#64;Group(name = "Advanced", collapsible = true, collapsed = true)
 * &#64;Group(name = "Dimensions", membersFrom = "dimensionMembers", after = "numDims")
 * public class MyCommand implements Command { ... }
 * </pre>
 *
 * @author Curtis Rueden
 */
@Repeatable(Groups.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Group {

	/** The group's name, as parameters refer to it. */
	String name();

	/** A human-readable label, defaulting to the name. */
	String label() default "";

	/** Whether the user can fold this group away. */
	boolean collapsible() default false;

	/** Whether it starts folded. */
	boolean collapsed() default false;

	/** A behavior deciding whether this group is currently shown. */
	String visibleWhen() default "";

	/**
	 * A behavior producing this group's parameters, for a group whose very
	 * shape depends on other values - three dimension labels or seven,
	 * according to a count entered above.
	 */
	String membersFrom() default "";

	/** Which parameter this group follows, for a generated group. */
	String after() default "";
}
