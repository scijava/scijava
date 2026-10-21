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
import java.util.List;


class ClassDotAutocompletions extends DefVarDotAutocompletions {
	final List<String> superclassNames; // List of superclasses
	final List<CompletionText>dotAutocompletions; // List of class methods and fields
	
	/**
	 * 
	 * @param fnName
	 * @param superclassNames
	 * @param argumentNames
	 * @param dotAutocompletions
	 * @param class_scope
	 */
	public ClassDotAutocompletions(final String fnName, List<String> superclassNames,
			final List<String> argumentNames, final List<CompletionText> dotAutocompletions, final Scope class_scope) {
		super(fnName, null, argumentNames, class_scope);
		this.superclassNames = superclassNames;
		this.dotAutocompletions = dotAutocompletions;
	}
	
	@Override
	public List<CompletionText> get() {
		final List<CompletionText> ac = new ArrayList<>(this.dotAutocompletions);
		for (final String className: this.superclassNames) {
			List<CompletionText> fm = DotAutocompletions.getPublicFieldsAndMethods(className);
			// TODO: If none, maybe it's a python class: get them all via the
			// org.python.indexer.Indexer and Builtins classes, see
			// StaticDotAutocompletions
			ac.addAll(fm);
		}
		return ac;
	}
	
	public void put(final CompletionText entry) {
		if (this.dotAutocompletions.contains(entry)) return; // list search OK: very low N
		this.dotAutocompletions.add(entry);
	}
	
	/** Make this be cda plus its own dotAutocompletions.
	 * 
	 * @param cda
	 */
	public void mutateIntoPlus(final ClassDotAutocompletions cda) 
	{
		this.className = cda.className;
		this.fnName = cda.fnName;
		this.superclassNames.clear();
		this.superclassNames.addAll(cda.superclassNames);
		this.dotAutocompletions.addAll(cda.dotAutocompletions);
		this.argumentNames.clear();
		this.argumentNames.addAll(cda.argumentNames);
		this.scope = cda.scope;
	}
	
	@Override
	public String toString() {
		return "ClassDotAutocompletions: " + this.fnName + "(" + String.join(", ", this.argumentNames) + ")" + " -- " + String.join(", ", this.get().toString());
	}
}
