/*-
 * #%L
 * An annotation processor for indexing Ops with javadoc.
 * %%
 * Copyright (C) 2021 - 2024 SciJava developers.
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

package org.scijava.ops.indexer;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.NoType;
import javax.tools.Diagnostic;

import static org.scijava.ops.indexer.ProcessingUtils.*;

/**
 * {@link OpImplData} implementation handling {@link Class}es annotated with
 * "implNote op"
 *
 * @author Gabriel Selzer
 */
class OpClassImplData extends OpImplData {

	/**
	 * @param source the {@link TypeElement} that is an "implNote op"
	 * @param fMethod the functional method of the Op
	 * @param doc the Javadoc on {@code source}
	 * @param fMethodDoc the Javadoc on the functional method {@code fMethod}
	 * @param env the {@link ProcessingEnvironment} used for annotation processing
	 */
	public OpClassImplData(TypeElement source, ExecutableElement fMethod,
		String doc, String fMethodDoc, ProcessingEnvironment env)
	{
		super(source, doc, env);
		parseFunctionalMethod(fMethod, fMethodDoc);
	}

	private void parseFunctionalMethod(ExecutableElement fMethod,
		String fMethodDoc)
	{
		if (fMethodDoc == null || fMethodDoc.isEmpty()) {
			printError(fMethod.getEnclosingElement(),
				" has a functional method without javadoc!");
			return;
		}
		String[] sections = blockSeparator.split(fMethodDoc);
		var paramTypes = fMethod.getParameters().iterator();

		int expNoParams = fMethod.getParameters().size();
		int expNoReturns = fMethod.getReturnType() instanceof NoType ? 0 : 1;
		int noParams = 0, noReturns = 0;

		for (String section : sections) {
			String[] elements = tagElementSeparator.split(section, 2);
			switch (elements[0]) {
				case "@param": {
					noParams++;
					String[] foo = tagElementSeparator.split(elements[1], 2);
					String name = foo[0];
					String description = foo[1];
					if (paramTypes.hasNext()) {
						var pType = paramTypes.next();
						String type = pType.asType().toString();
						params.add(new OpParameter(name, type, ProcessingUtils.ioType(
							description), description, isNullable(pType, description)));
					}
					else {
						throw new IllegalArgumentException("Op " + this.source + " has " +
							noParams + " @param tags, but the functional method only has " +
							expNoParams + " parameters!");
					}
					break;
				}
				case "@return": {
					noReturns++;
					String name = "output";
					String description = elements[1];
					String type = fMethod.getReturnType().toString();
					params.add(new OpParameter(name, type, OpParameter.IO_TYPE.OUTPUT,
						description, false));
					break;
				}
				case "@author":
					addAuthor(tagElementSeparator.split(section, 2)[1]);
					break;
			}
		}
		if (noParams != expNoParams) {
			throw new IllegalArgumentException("Op " + this.source + " has " +
				noParams + " @param tags, but the functional method " + fMethod +
				" only has " + expNoParams + " parameters!");
		}
		if (noReturns != expNoReturns) {
			throw new IllegalArgumentException("Op " + this.source + " has " +
				noReturns + " @return tags, but the functional method only has " +
				expNoReturns + " returns!");
		}
	}

	/**
	 * Parse javadoc tags pertaining exclusively to {@link Class}es
	 *
	 * @param source the {@link Element} representing the {@link Class}.
	 * @param additionalTags the tags that pertain particularly to {@link Class}es
	 */
	@Override
	void parseAdditionalTags(Element source, List<String[]> additionalTags) {}

	protected String formulateSource(Element source) {
		var srcString = source.toString();
		var parent = source.getEnclosingElement();
		// handle inner classes
		while (parent.getKind() == ElementKind.CLASS) {
			int badPeriod = srcString.lastIndexOf('.');
			srcString = srcString.substring(0, badPeriod) + '$' + srcString.substring(
				badPeriod + 1);
			parent = parent.getEnclosingElement();
		}
		return "javaClass:/" + URLEncoder.encode(srcString, StandardCharsets.UTF_8);
	}

	private void printError(Element source, String msg) {
		env.getMessager().printMessage(Diagnostic.Kind.ERROR, source + msg);
	}

}
