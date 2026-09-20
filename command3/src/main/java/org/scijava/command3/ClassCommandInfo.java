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

import java.util.Map;
import java.util.Optional;

import org.scijava.context.Access;
import org.scijava.discovery.Discovery;
import org.scijava.execute.Executable;
import org.scijava.execute.ExecutableInstance;
import org.scijava.execute.Executables;
import org.scijava.struct.Struct;

/**
 * A command as the application knows it before running one: its identity, its
 * parameters, and how it should appear in a menu.
 * <p>
 * The presentation metadata is read from the annotation index, so listing
 * every command and building a menu from them loads no command classes at all.
 * {@link #struct()} and {@link #create()} are the points at which the class is
 * finally needed.
 * </p>
 * <p>
 * NB: SciJava Common called this {@code ModuleInfo}, and its
 * {@code ClassCommandInfo} was one implementation. The names have shifted by one:
 * "module" is now a JPMS module, so the general description is
 * {@link Executable}, and this is the command-shaped view of it.
 * </p>
 * <p>
 * NB: a command whose implementation is a <em>class</em> - which is not the
 * same as a Java class, Kotlin and Groovy and Scala classes being just as
 * welcome. The other kind is a script, which reaches the same menus as a
 * {@link CommandInfo} built by {@link CommandInfo#of}.
 * </p>
 *
 * @author Curtis Rueden
 */
public class ClassCommandInfo implements CommandInfo {

	private final Discovery<Command> discovery;
	private final Map<String, String> menu;
	private Executable delegate;

	ClassCommandInfo(final Discovery<Command> discovery,
		final Map<String, String> menu)
	{
		this.discovery = discovery;
		this.menu = menu;
	}

	// -- Metadata, readable without loading the class --

	/** Gets the command's implementation class name. */
	public String className() {
		return discovery.implClassName();
	}

	/** Gets where this command sits in the menus, if anywhere. */
	@Override
	public Optional<String> menuPath() {
		return attr("path");
	}

	/** Gets the label to display, defaulting to the last menu path element. */
	@Override
	public String label() {
		return menuPath() //
			.map(path -> path.substring(path.lastIndexOf('>') + 1)) //
			.orElseGet(this::name);
	}

	/** Gets this command's keyboard shortcut, if it has one. */
	@Override
	public Optional<String> accelerator() {
		return attr("accelerator");
	}

	/** Gets this command's icon resource, if it has one. */
	@Override
	public Optional<String> iconPath() {
		return attr("iconPath");
	}

	/** Gets how this command sorts among its menu siblings. */
	@Override
	public double weight() {
		return attr("weight").map(Double::parseDouble) //
			.orElse(Double.POSITIVE_INFINITY);
	}

	/** Gets whether this command should appear in menus. */
	@Override
	public boolean isVisible() {
		return !"false".equals(menu.get("visible"));
	}

	/** Gets the priority with which this command was declared. */
	public double priority() {
		return discovery.priority();
	}

	/** Gets a menu attribute by name. */
	public Optional<String> attr(final String key) {
		final String value = menu.get(key);
		return value == null || value.isEmpty() ? Optional.empty() //
			: Optional.of(value);
	}

	// -- Executable methods --

	@Override
	public String name() {
		return className();
	}

	@Override
	public Struct struct() {
		return delegate().struct();
	}

	@Override
	public ExecutableInstance create() {
		return delegate().create();
	}

	@Override
	public String toString() {
		return className() + menuPath().map(p -> " [" + p + "]").orElse("");
	}

	/**
	 * Gets the executable behind this command, loading the class on first use.
	 * <p>
	 * NB: everything above this line answers from the index alone. This is
	 * where a menu stops being free.
	 * </p>
	 */
	private synchronized Executable delegate() {
		if (delegate == null) {
			final Class<? extends Command> type = discovery.type();
			// NB: the lookup comes from the context, so a command's package need
			// only be opened to org.scijava.context -- not additionally to the
			// execution layer that reads its @Parameter fields.
			delegate = Executables.of(type, Access.lookupIn(type));
		}
		return delegate;
	}
}
