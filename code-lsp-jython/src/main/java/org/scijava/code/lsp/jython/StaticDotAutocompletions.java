/*-
 * #%L
 * Jython language support for SciJava Script Editor.
 * %%
 * Copyright (C) 2020 - 2024 SciJava developers.
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
package org.scijava.code.lsp.jython;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.python.indexer.types.NModuleType;

public class StaticDotAutocompletions implements DotAutocompletions
{	
	final String className;
	public StaticDotAutocompletions(final String className) {
		this.className = className;
	}
	@Override
	public String getClassname() {
		return this.className;
	}
	@Override
	public List<CompletionText> get() {
		final List<CompletionText> ac = new ArrayList<>();
		if (null != this.className) {
			String msg = "";
			try {
				// Check first if it's a python module
				final NModuleType module = Scope.loadPythonModule(this.className); // Scope.indexer.getBuiltinModule(this.className);
				JythonDev.printTrace("module is: " + module);
				if (null != module) {
					module.getTable().keySet().forEach( m -> ac.add(new CompletionText(m)));
					// Not need to remove: a file system watcher will do so when the module file is updated or deleted.
					// Scope.indexer.moduleTable.remove(this.className);
					return ac;
				} else {
					msg += "Not a python module: " + this.className;
				}
				// Or a java class:
				try {
					DotAutocompletions.staticFieldsAndStaticMethodsInto(Class.forName(this.className), ac);
					return ac;
				} catch (ClassNotFoundException cnfe) {
					msg += "\nCannot find java class " + this.className;
				}
				// Or a static method of a java class
				try {
					final int idot = this.className.lastIndexOf('.');
					final String name = this.className.substring(idot + 1);
					final Class<?> c = Class.forName(this.className.substring(0, idot));
					// There could be more than one method, with more than one return type
					for (final Class<?> r: Arrays.asList(c.getMethods()).stream()
							.filter(m -> m.getName().equals(name))
							.map(m -> m.getReturnType())
							.distinct()
							.collect(Collectors.toList())) {
						DotAutocompletions.fieldsAndMethodsInto(r, ac);
					}
					return ac;
				} catch (ClassNotFoundException cnfe) {
					msg += "\nCannot derive static method or field from " + this.className;
				}
				
				if (null != msg) {
					JythonDev.print(msg);
				}
			} catch (Exception e) {
				JythonDev.print(msg, e);
			}
		}
		return ac;
	}
	
	@Override
	public String toString() {
		final String completions = get().stream().map(c -> c.getReplacementText()).collect(Collectors.joining(",", "[", "]"));
		return "StaticDotAutocompletions: " + className + " -- " + completions;
	}
}
