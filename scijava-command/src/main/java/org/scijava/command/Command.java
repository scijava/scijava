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

package org.scijava.command;

/**
 * Something a user can run: the unit a menu item, a script and a recorded
 * step all refer to.
 * <p>
 * A Java command is a class implementing this interface, declaring its
 * parameters as {@code org.scijava.execute.Parameter} fields, and declaring
 * itself with {@code @Plugin(type = Command.class)}:
 * </p>
 *
 * <pre>
 * &#64;Plugin(type = Command.class)
 * &#64;Menu(path = "Image&gt;Adjust&gt;Brightness/Contrast...", weight = 12)
 * public class BrightnessContrast implements Command {
 *
 * 	&#64;Parameter
 * 	private Dataset image;
 *
 * 	public void run() { ... }
 * }
 * </pre>
 * <h2>On the word</h2>
 * <p>
 * "Command" here means what SciJava Common called a <em>module</em>: anything
 * a user can run, with declared parameters. A script is a command whose
 * implementation is source in some language; a class like the one above is a
 * command whose implementation is a class. Neither is a special case of the
 * other, and both reach the menu, the harvester and the runner by one path.
 * </p>
 * <p>
 * This interface is therefore not the definition of a command - {@link
 * CommandInfo} is - but the way to <em>write</em> one as a class. A script
 * implements nothing at all, and is no less a command for it.
 * </p>
 * <p>
 * The old vocabulary is retired: <em>module</em> now means a JPMS module,
 * <em>macro</em> means the ImageJ 1.x macro language and nothing here, and
 * <em>extension</em> is not a technical term.
 * </p>
 *
 * @author Curtis Rueden
 */
public interface Command extends Runnable {
	// NB: no methods of its own. A command is a Runnable that declares
	// parameters, and the parameters are declared by annotation.
}
