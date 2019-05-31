/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2016 Board of Regents of the University of
 * Wisconsin-Madison and University of Konstanz.
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

package org.scijava.ops;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.io.CharStreams;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import net.imagej.ops.AbstractOpTest;

import org.junit.Before;
import org.junit.Test;
import org.scijava.ops.matcher.OpInfo;
import org.scijava.ops.matcher.OpRef;
import org.scijava.types.Nil;
import org.scijava.util.Types;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.bytecode.ClassFile;

public class JavassistTest extends AbstractOpTest {

	private OpRef ref;
	private OpInfo matchingCandidate;
	private OpInfo notMatchingCandidate;

	@Before
	public void setUpRefAndCandidate() {
		ref = createOpRef(new Nil<Foo<Double, Integer>>() {}, new Nil[] {
			new Nil<String>()
			{} }, new Nil<String>() {});
		matchingCandidate = findValidCandidateMock();
		notMatchingCandidate = findInvalidCandidateMock();
	}

	private static <T> OpRef createOpRef(final Nil<T> specialType,
		final Nil<?>[] inTypes, final Nil<?> outType)
	{
		final OpRef ref = OpRef.fromTypes("foo", toTypes(specialType), toTypes(
			outType), toTypes(inTypes));
		return ref;
	}

	private static Type[] toTypes(Nil<?>... nils) {
		return Arrays.stream(nils).filter(n -> n != null).map(n -> n.getType())
			.toArray(Type[]::new);
	}

	private static OpInfo findValidCandidateMock() {
		for (OpInfo info : ops().infos("foo")) {
			if (Types.raw(info.opType()) == MyFoo.class) {
				return info;
			}
		}
		throw new IllegalStateException("Test setup is broken.");
	}

	private static OpInfo findInvalidCandidateMock() {
		for (OpInfo info : ops().infos("foo")) {
			if (Types.raw(info.opType()) == YourFoo.class) {
				return info;
			}
		}
		throw new IllegalStateException("Test setup is broken.");
	}

	private void test(Function<String, Boolean> compiler) {
		final String validCode = createAssignabilityTestCode(matchingCandidate,
			ref);
		assertTrue("Code did not compile, but should:\n" + validCode, compiler
			.apply(validCode));
		final String invalidCode = createAssignabilityTestCode(notMatchingCandidate,
			ref);
		assertFalse("Code compiled, but should not:\n" + invalidCode, compiler
			.apply(invalidCode));
	}

	private static String createAssignabilityTestCode(OpInfo candidate,
		OpRef ref)
	{
		return "public void testAssignability(" + candidateToString(candidate,
			ref) + " candidate, " + refToString(ref) + " ref) {" //
			+ "ref = candidate;" + //
			"}";
	}

	private static String candidateToString(OpInfo candidate, OpRef ref) {
		final Type opType = candidate.opType();
		final Type inferredOpType;
		if (opType instanceof Class) {
			inferredOpType = opType;
		}
		else {
			final Map<TypeVariable<?>, Type> typeVarAssigns = new HashMap<>();
			// TODO: Can we get rid of that or boil it down? We just want to properly
			// parameterize the candidate here. If something goes wrong here (or if
			// this step is already error-prone/too complex or if this steps is
			// already doing the bulk of the matching, the whole point of compiling
			// the assignment does not make sense.
			if (ref.typesMatch(opType, typeVarAssigns)) {
				inferredOpType = Types.parameterize(Types.raw(opType), typeVarAssigns);
			}
			else {
				throw new RuntimeException(
					"Candidate does not match ref, no need to test assignability.");
			}
		}
		return inferredOpType.getTypeName();
	}

	private static String refToString(OpRef ref) {
		// TODO: We need to do test assignability for each item of ref.getTypes().
		return ref.getTypes()[0].getTypeName();
	}

	@Test
	public void testJavaassist() {
		test(JavassistTest::javassistCompilesMethod);
		// TODO: For some reason, Javassist doesn't check for incompatible
		// assignments. The following code is considered valid:
		// public void testAssignability(java.util.function.Function candidate,
		// org.scijava.ops.core.computer.Computer ref)
		// {
		// ref = candidate;
		// }
	}

	private static boolean javassistCompilesMethod(String source) {
		final ClassFile cf = new ClassFile(false,
			"org.scijava.ops.matcher._runtimegenerated.JavassistTestClass", null);
		final ClassPool cp = ClassPool.getDefault();
		final CtClass ct = cp.makeClass(cf);
		// TODO: Support generics:
		// https://stackoverflow.com/questions/31050473/javassist-creating-an-interface-that-extends-another-interface-with-generics
		try {
			final CtMethod m = CtNewMethod.make(source, ct);
			ct.addMethod(m);
			// new InstructionPrinter(System.out).print(m);
		}
		catch (CannotCompileException exc) {
			return false;
		}
		return true;
	}

	@Test
	public void testExternalJavac() {
		test(JavassistTest::externalJavacCompilesMethod);
	}

	private static boolean externalJavacCompilesMethod(String source) {
		try {
			final File f = wrapInTempFile(source);
			final Process p = Runtime.getRuntime().exec(
				"/home/marcel/applications/jdk/jdk1.8.0_191/bin/javac " + f
					.getAbsolutePath());
			p.waitFor();
			final String output = toString(p.getInputStream());
			System.out.println(output);
			final String error = toString(p.getErrorStream());
			System.out.println(error);
			return p.exitValue() == 0;
		}
		catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private static File wrapInTempFile(String source) throws IOException {
		final File temp = File.createTempFile("GeneratedJavacTestClass", ".java");
		temp.deleteOnExit();
		source = "public class " + temp.getName().split("\\.")[0] + " { " + source +
			" }";
		Files.write(source, temp, StandardCharsets.UTF_8);
		return temp;
	}

	private static String toString(InputStream stream) throws IOException {
		return CharStreams.toString(new InputStreamReader(stream,
			StandardCharsets.UTF_8));
	}

	@Test
	public void testPlatformCompiler() {
		test(JavassistTest::platformCompilerCompilesMethod);
	}

	// From
	// https://stackoverflow.com/questions/21544446/how-do-you-dynamically-compile-and-load-external-java-classes
	// and
	// https://stackoverflow.com/questions/12173294/compile-code-fully-in-memory-with-javax-tools-javacompiler
	// Requires SDK.
	private static boolean platformCompilerCompilesMethod(String source) {
		final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		final DiagnosticCollector<JavaFileObject> diagnostics =
			new DiagnosticCollector<>();
		// Optionally provide class path:
		final List<String> optionList = new ArrayList<>();
		optionList.add("-classpath");
		optionList.add(System.getProperty("java.class.path"));
		final JavaFileObject f = new WrappedMethodJavalFileObject(source);
		final Iterable<? extends JavaFileObject> compilationUnit = Arrays.asList(f);
		final JavaCompiler.CompilationTask task = compiler.getTask(null, null,
			diagnostics, optionList, null, compilationUnit);
		final boolean compiled = task.call();
		// TODO: Use diagnostics to distinguish matching errors from bugs in the
		// preparation of the code (e.g., unresolved generic type variables, etc.).
		// Only "Incompatible Type" compilation errors should be interpreted as
		// mismatch.
		// if (compiled) {
		// for (Diagnostic<? extends JavaFileObject> d : diagnostics
		// .getDiagnostics())
		// {
		// System.out.println(d.toString());
		// }
		// }
		return compiled;
	}

	private static class WrappedMethodJavalFileObject extends
		SimpleJavaFileObject
	{

		private static final String NAME = "JavaCompilerGeneratedTestClass";

		private final String source;

		WrappedMethodJavalFileObject(String source) {
			super(URI.create("string:///" + NAME.replace('.', '/') +
				Kind.SOURCE.extension), Kind.SOURCE);
			this.source = "public class " + NAME + " { " + source + " }";
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return source;
		}
	}

	@Test
	public void testInternalJavac() {
		test(JavassistTest::internalJavacCompilesMethod);
	}

	// This is essentially the same as using JavaCompiler, just old API.
	private static boolean internalJavacCompilesMethod(String source) {
		try {
			final File f = wrapInTempFile(source);
			int errorCode = com.sun.tools.javac.Main.compile(new String[] { f
				.getAbsolutePath() } /* , new PrintWriter(System.out) */);
			return errorCode == 0;
		}
		catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	// TODO: Actual use case:
	// @Test
	// public void findOpInstanceTest() {
	// final Foo<Double> foo = findOpInstance(new Nil<Foo<Double>>() {},
	// new Nil[] { new Nil<String>()
	// {} }, new Nil<String>() {});
	// System.out.println(foo);
	// }
	//
	// private static <T> T findOpInstance(final Nil<T> specialType,
	// final Nil<?>[] inTypes, final Nil<?> outType)
	// {
	// final OpRef ref = createOpRef(specialType, inTypes, outType);
	// final OpInfo candidate = findCandidateMock();
	// if (isAssignable(candidate, ref)) {
	// System.out.println("Assignable!");
	// @SuppressWarnings("unchecked")
	// final T casted = (T) candidate.createOpInstance().object();
	// return casted;
	// }
	// throw new IllegalStateException("Not assignable!");
	// }
	//
	// private static boolean isAssignable(OpInfo candidate, OpRef ref) {
	// // return javassistCompilesMethod( //
	// // return javacCompilesMethod( //
	// return platformCompilerCompilesMethod( //
	// createAssignabilityTestCode(candidate, ref));
	// }

	public static void main(String[] args) {
		final JavassistTest test = new JavassistTest();
		test.setUp();
		test.setUpRefAndCandidate();
		final String validCode = createAssignabilityTestCode(test.matchingCandidate,
			test.ref);
		final int n = 20000;
		final long start = System.nanoTime();
		for (int i = 0; i < n; i++) {
			// Platform compiler:
			// 100 runs: Elapsed (ms) - Total: 2507 - Avg. per run: 25
			// 1500 runs: Elapsed (ms) - Total: 25601 - Avg. per run: 17
			// 20000 runs: Elapsed (ms) - Total: 308794 - Avg. per run: 15
			JavassistTest.internalJavacCompilesMethod(validCode);
		}
		final long stop = System.nanoTime();
		final long elapsedMillis = (stop - start) / 1000 / 1000;
		System.out.println(n + " runs: Elapsed (ms) - Total: " + elapsedMillis +
			" - Avg. per run: " + elapsedMillis / n);
	}
}
