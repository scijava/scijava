
package org.scijava.ops.python;

import org.junit.jupiter.api.Test;

import jep.Interpreter;

/**
 * Tests that we have access to the Python interpreter
 *
 * @author Gabriel Selzer
 */
public class PythonTest {

	@Test
	public void testOpsInterpreter() {
		Interpreter interp = OpsPythonInterpreter.interpreter();
		interp.exec("from java.lang import System");
		interp.exec("s = 'Hello World'");
		Object result = interp.getValue("s");
		System.out.println(result);
	}
}
