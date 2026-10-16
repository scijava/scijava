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

public class JythonDev
{
	static public int SILENT = 0,
					  MESSAGES = 1,
					  ERRORS = 2;
	
	/** Adjust value to see debugging information, including:
	 * SILENT: nothing is printed at all.
	 * MESSAGES: prints light messages indicating some internals and the kinds of errors.
	 * ERRORS: prints everything including exceptions.
	 */
	static public int debug = MESSAGES;
	
	static public final void print(Object s) {
		if (debug >= MESSAGES) System.out.println(s);
	}
	
	static public final void printError(Throwable e) {
		if (debug >= ERRORS) e.printStackTrace();
	}
	
	static public final void printTrace(Object s) {
		if (debug >= ERRORS) System.out.println(s);
	}
	
	static public final void print(Object s, Throwable e) {
		if (debug >= ERRORS) {
			System.out.println(s);
			e.printStackTrace();
		} else if (debug >= MESSAGES) {
			System.out.println(s.toString() + " :: " + e.getMessage());
		}
	}

}
