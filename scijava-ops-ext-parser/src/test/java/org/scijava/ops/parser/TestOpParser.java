/*-
 * #%L
 * SciJava Ops External Parser: A tool for parsing external libraries to ops
 * %%
 * Copyright (C) 2024 SciJava developers.
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

package org.scijava.ops.parser;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Testing for {@link OpParser}
 */
public class TestOpParser {

	/**
	 * Trivial test that we get the expected yaml back for our test file
	 */
	@Test
	public void validateParsing() throws ClassNotFoundException {
		File f = new File(getClass().getResource("/test-ops.yaml").getFile());
		String actual = OpParser.parseOpDocument(f.getAbsolutePath());

		String expected = //
			"- op:" //
				+ "\n    names: [arrays.toStringDeep, fun.stringMaker, test.deepToString]" //
				+ "\n    description: ''" //
				+
				"\n    source: javaMethod:/java.util.Arrays.deepToString%28%5BLjava.lang.Object%3B%29" //
				+ "\n    priority: 0.0" //
				+ "\n    version: '1'" //
				+ "\n    parameters:" //
				+
				"\n    - {parameter type: INPUT, name: arg0, description: '', type: '[Ljava.lang.Object;'}" //
				+ "\n    authors: [Nobody, Everybody]" //
				+ "\n    tags: {}" //
				+ "\n- op:" //
				+ "\n    names: [ext.arraycopy, test.arraycopy]" //
				+ "\n    description: a useful op" //
				+
				"\n    source: javaMethod:/java.lang.System.arraycopy%28java.lang.Object%2Cint%2Cjava.lang.Object%2Cint%2Cint%29" //
				+ "\n    priority: 50.0" //
				+ "\n    version: '1'" //
				+ "\n    parameters:" //
				+
				"\n    - {parameter type: INPUT, name: arg0, description: '', type: java.lang.Object}" //
				+
				"\n    - {parameter type: CONTAINER, name: arg1, description: '', type: int}" //
				+
				"\n    - {parameter type: INPUT, name: arg2, description: '', type: java.lang.Object}" //
				+
				"\n    - {parameter type: INPUT, name: arg3, description: '', type: int}" //
				+
				"\n    - {parameter type: INPUT, name: arg4, description: '', type: int}" //
				+ "\n    authors: [You-know-who]" //
				+ "\n    tags: {type: Computer2}" //
				+ "\n";
		Assertions.assertEquals(expected, actual);
	}
}
