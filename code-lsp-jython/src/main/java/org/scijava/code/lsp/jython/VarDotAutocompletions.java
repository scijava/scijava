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
import java.util.stream.Stream;


public class VarDotAutocompletions implements DotAutocompletions {
	String className;
	public VarDotAutocompletions(final String className) {
		this.className = className;
	}
	@Override
	public String getClassname() {
		return this.className;
	}
	@Override
	public List<CompletionText> get() {
		final List<CompletionText> ac = new ArrayList<>();
		if (null != className) {
			try {
				final Class<?> c = Class.forName(className);
				DotAutocompletions.fieldsAndMethodsInto(c, ac);
			} catch (final Exception e) {
				JythonDev.print("Could not load class " + className, e);
			}
		}
		return ac;
	}
	
	@Override
	public Stream<CompletionText> getStream() {
		try {
			final Class<?> c = Class.forName(this.className);
			return Stream.concat(
					Arrays.stream(c.getFields()).map(f -> new CompletionText(f.getName(), c, f)),
					Arrays.stream(c.getMethods()).map(m -> new CompletionText(m.getName(), c, m)));
		} catch (final Exception e) {
			JythonDev.print("Could not load class " + className, e);
		}
		return Stream.empty();
	}

	@Override
	public String toString() {
		return "VarDotAutocompletions: " + this.className;
	}
}
