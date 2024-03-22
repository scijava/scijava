/*-
 * #%L
 * SciJava Ops Indexer: An annotation processor for indexing Ops
 * %%
 * Copyright (C) 2021 - 2023 SciJava developers.
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

import static javax.lang.model.element.ElementKind.METHOD;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * A set of static utilities useful for processing Ops
 *
 * @author Gabriel Selzer
 */
public final class ProcessingUtils {

	/**
	 * Given a Javadoc comment, separates the comment by the Javadoc tags
	 * (e.g. @author, @param, etc.). Therefore, each string returned by
	 * blockSeparator.split(String) will be:
	 * <ul>
	 * <li>A string beginning with and including a javadoc tag
	 * (e.g. @author, @param, etc.)</li>
	 * <li>The description of the code block - this happens when there's a
	 * description before the tags start</li>
	 * </ul>
	 */
	public static final Pattern blockSeparator = Pattern.compile("^\\s*(?=@\\S)",
		Pattern.MULTILINE);
	/**
	 * Given a string, splits the String by whitespace UNLESS the whitespace is
	 * inside a set of single quotes. Useful for parsing tags, especially implNote
	 * tags.
	 */
	public static final Pattern tagElementSeparator = Pattern.compile(
		"\\s*[,\\s]+(?=(?:[^']*'[^']*')*[^']*$)");

	private ProcessingUtils() {
		throw new AssertionError("not instantiable");
	}

	/**
	 * Logs a {@link Throwable} parsing an {@link Element}
	 *
	 * @param source the {@link Element} whose parsing was erroneous
	 * @param t the {@link Throwable} thrown during the parsing
	 * @param env the {@link ProcessingEnvironment} able to log the
	 *          {@link Throwable}
	 */
	public static void printProcessingException(Element source, Throwable t,
		ProcessingEnvironment env)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		env.getMessager().printMessage(Diagnostic.Kind.ERROR,
			"Exception parsing source + " + source + ": " + sw);
	}

	/**
	 * Finds the functional method of {@code source}, returning it as an
	 * {@link ExecutableElement}
	 *
	 * @param env the {@link ProcessingEnvironment} with the knowledge to reason
	 *          about {@code source}
	 * @param source the {@link TypeElement} that represents a
	 *          {@link FunctionalInterface}, whose functional method we want to
	 *          find
	 * @return the functional method of {@code source}, as an
	 *         {@link ExecutableElement}
	 */
	public static ExecutableElement findFunctionalMethod(
		ProcessingEnvironment env, TypeElement source)
	{
		// Step 1: Find abstract interface method somewhere in the hierarchy
		ExecutableElement fMethod = findAbstractFunctionalMethod(env, source);
		// Step 2: Find the member of source that matches that abstract interface
		// method
		if (fMethod != null) {
			for (Element e : env.getElementUtils().getAllMembers(source)) {
				if (e.getSimpleName().equals(fMethod.getSimpleName())) {
					return (ExecutableElement) e;
				}
			}
		}
		throw new IllegalArgumentException("Op " + source +
			" does not declare a functional method!");
	}

	private static ExecutableElement findAbstractFunctionalMethod( //
		ProcessingEnvironment env, //
		TypeElement source //
	) {
		// First check source itself for the abstract method
		int abstractMethodCount = 0;
		ExecutableElement firstAbstractMethod = null;
		for (Element e : source.getEnclosedElements()) {
			if (e.getKind() == METHOD && e.getModifiers().contains(
				Modifier.ABSTRACT))
			{
				firstAbstractMethod = (ExecutableElement) e;
				abstractMethodCount++;

			}
		}
		if (abstractMethodCount == 1) {
			return firstAbstractMethod;
		}
		// Otherwise, check up the class hierarchy
		else {
			// First, check the interfaces
			for (TypeMirror e : source.getInterfaces()) {
				Element iFace = env.getTypeUtils().asElement(e);
				if (iFace instanceof TypeElement) {
					ExecutableElement fMethod = findAbstractFunctionalMethod(env,
						(TypeElement) iFace);
					if (fMethod != null) return fMethod;
				}
			}
			// Then, check the superclass
			Element superCls = env.getTypeUtils().asElement(source.getSuperclass());
			if (superCls instanceof TypeElement) {
				return findAbstractFunctionalMethod(env, (TypeElement) superCls);
			}
			return null;
		}
	}
}
