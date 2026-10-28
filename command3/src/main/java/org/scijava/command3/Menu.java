/*
 * #%L
 * Discoverable commands, with the metadata a menu is built from.
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

package org.scijava.command3;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.scijava.index.Indexable;

/**
 * Where a command appears in the menus, and how it looks there.
 * <p>
 * This stacks onto a plugin declaration rather than living inside it:
 * </p>
 *
 * <pre>
 * &#64;Plugin(type = Command.class)
 * &#64;Menu(path = "Image&gt;Adjust&gt;Brightness/Contrast...", weight = 12,
 *       accelerator = "^C", iconPath = "/icons/bc.png")
 * public class BrightnessContrast implements Command { ... }
 * </pre>
 * <p>
 * Keeping it separate is what stops {@code @Plugin} from accumulating
 * attributes that make no sense for most plugin types - a converter has no
 * menu path - while leaving {@code @Plugin} the single, uniform way to declare
 * a plugin.
 * </p>
 * <p>
 * NB: this is {@link Indexable} in its own right, so the annotation processor
 * writes it to its own index file. A menu is therefore built by reading two
 * indexes and joining them on the class name, with <strong>no class
 * loading</strong> - which is what keeps startup cheap when an application has
 * hundreds of commands. An {@code @Menu} without a {@code @Plugin} is
 * harmless but inert: nothing discovers it.
 * </p>
 *
 * @author Curtis Rueden
 */
@Indexable
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Menu {

	/**
	 * Where this command sits, as a {@code >}-separated path whose last element
	 * is the item itself - for example
	 * {@code "Image>Adjust>Brightness/Contrast..."}.
	 */
	String path();

	/** Sorts items within their menu; lower comes first. */
	double weight() default Double.POSITIVE_INFINITY;

	/**
	 * A keyboard shortcut, in the SciJava Common notation: {@code ^} for the
	 * platform's command key, {@code !} for shift, {@code @} for alt.
	 */
	String accelerator() default "";

	/** A classpath resource for the item's icon. */
	String iconPath() default "";

	/** Whether this item is a toggle. */
	boolean selectable() default false;

	/** Groups mutually exclusive toggles; implies {@link #selectable()}. */
	String selectionGroup() default "";

	/** Whether this item appears at all. */
	boolean visible() default true;
}
