/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2025 SciJava developers.
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

package org.scijava.ops.engine.matcher.reduce;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.util.Infos;
import org.scijava.ops.spi.Nullable;
import org.scijava.struct.Member;
import org.scijava.common3.Types;
import org.scijava.types.infer.FunctionalInterfaces;

import com.google.common.collect.Streams;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public final class ReductionUtils {

	private ReductionUtils() {
		// Prevent instantiation of static utility class
	}

	/**
	 * Creates a Class given an Op and a {@link ReducedOpInfo}. This class
	 * implements a {@link FunctionalInterface} that is of a smaller arity than
	 * {@code originalOp}.
	 *
	 * @param originalOp - the Op that will be reduced
	 * @param reducedInfo - the {@link ReducedOpInfo} containing the information
	 *          required to reduce {@code originalOp}.
	 * @return a wrapper of {@code originalOp} taking arguments that are then
	 *         mutated to satisfy {@code originalOp}, producing outputs that are
	 *         then mutated to satisfy the desired output of the wrapper.
	 * @throws Throwable
	 */
	protected static Object javassistOp(Object originalOp,
		ReducedOpInfo reducedInfo) throws Throwable
	{
        var pool = ClassPool.getDefault();

		// NB LambdaMetaFactory only works if this Module (org.scijava.ops.engine)
		// can read the Module containing the Op. So we also have to check that.
        var methodModule = originalOp.getClass().getModule();
        var opsEngine = ReductionUtils.class.getModule();
		opsEngine.addReads(methodModule);

		// Create wrapper class
        var className = formClassName(reducedInfo);
		Class<?> c;
		try {
			c = pool.getClassLoader().loadClass(className);
		}
		catch (ClassNotFoundException e) {
            var cc = generateReducedWrapper(pool, className, reducedInfo);
			c = cc.toClass(MethodHandles.lookup());
		}

		// Return Op instance
		return c.getDeclaredConstructor(Types.raw(reducedInfo.srcInfo().opType()))
			.newInstance(originalOp);
	}

	/**
	 * A valid class name must be unique.
	 *
	 * @param reducedInfo
	 * @return the class name
	 */
	private static String formClassName(ReducedOpInfo reducedInfo) {
		// -- package name --
		// NB required to be this package for the Lookup to work
        var packageName = getPackageName();
        var sb = new StringBuilder(packageName + ".");

		// -- class name --
		// Start with the class of the implementation
        var originalName = className(reducedInfo);
		// Add the input members for uniqueness
        var parameters = memberNames(reducedInfo);

		// -- ensure the name is valid --
        var className = originalName.concat(parameters);
		if (className.chars().anyMatch(c -> !Character.isJavaIdentifierPart(c)))
			throw new IllegalArgumentException(className +
				" is not a valid class name!");

		// -- full name is package + class --
		sb.append(className);
		return sb.toString();
	}

	private static String getPackageName() {
		return ReductionUtils.class.getPackageName();
	}

	private static String className(ReducedOpInfo reducedInfo) {
        var implName = reducedInfo.implementationName();
        var parenIndex = implName.indexOf('(');
		int classStart;
		// no paren - structure is package.class
		if (parenIndex == -1) {
			classStart = implName.lastIndexOf('.') + 1;
		}
		// paren - structure is packge.class.method(params)
		else {
            var methodStart = implName.substring(0, parenIndex).lastIndexOf('.');
			classStart = implName.substring(0, methodStart).lastIndexOf('.') + 1;
		}

        var originalName = implName.substring(classStart); // we only want the
																													// class name
		// replace non-valid identifiers with underscore (the underscore is
		// arbitrary)
		return originalName.replaceAll("[^A-Z^a-z0-9$_]", "_");
	}

	private static String memberNames(ReducedOpInfo reducedInfo) {
        var memberNames = //
			Streams.concat(reducedInfo.inputTypes().stream(), //
				Stream.of(reducedInfo.output().type())) //
				.map(type -> getClassName(Types.raw(type)));
        var iterableNames = (Iterable<String>) memberNames::iterator;
		return String.join("_", iterableNames);
	}

	/**
	 * {@link Class}es of array types return "[]" when
	 * {@link Class#getSimpleName()} is called. Those characters are invalid in a
	 * class name, so we exchange them for the suffix "_Arr".
	 *
	 * @param clazz - the {@link Class} for which we need a name
	 * @return - a name that is legal as part of a class name.
	 */
	private static String getClassName(Class<?> clazz) {
        var className = clazz.getSimpleName();
		if (className.chars().allMatch(c -> Character.isJavaIdentifierPart(c)))
			return className;
		if (clazz.isArray()) return clazz.getComponentType().getSimpleName() +
			"_Arr";
		return className;
	}

	private static CtClass generateReducedWrapper(ClassPool pool,
		String className, ReducedOpInfo reducedInfo) throws Throwable
	{
        var cc = pool.makeClass(className);
		// Add implemented interface
        var jasOpType = pool.get(Types.raw(reducedInfo.opType()).getName());
		cc.addInterface(jasOpType);

		// Add Op field
        var opField = createOpField(pool, cc, Types.raw(reducedInfo.srcInfo()
			.opType()), "op");
		cc.addField(opField);

		// Add constructor
        var constructor = CtNewConstructor.make(createConstructor(cc,
			reducedInfo), cc);
		cc.addConstructor(constructor);

		// add functional interface method
        var functionalMethod = CtNewMethod.make(createFunctionalMethod(
			reducedInfo), cc);
		cc.addMethod(functionalMethod);
		return cc;
	}

	private static CtField createOpField(ClassPool pool, CtClass cc,
		Class<?> opType, String fieldName) throws NotFoundException,
		CannotCompileException
	{
        var fType = pool.get(opType.getName());
        var f = new CtField(fType, fieldName, cc);
		f.setModifiers(Modifier.PRIVATE + Modifier.FINAL);
		return f;
	}

	private static String createConstructor(CtClass cc,
		ReducedOpInfo reducedInfo)
	{
        var sb = new StringBuilder();
		// constructor signature
		sb.append("public " + cc.getSimpleName() + "(");
		// argument - original op
        var opClass = Types.raw(reducedInfo.srcInfo().opType());
		sb.append(" " + opClass.getName() + " op");
		sb.append(") {");

		sb.append("this.op = op;");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Creates the functional method of a reduced Op. This functional method must:
	 * <ol>
	 * <li>Call the {@code Op} using the required <b>pure</b> inputs, followed by
	 * {@code null} {@link Nullable} <b>pure</b> arguments, followed by the i/o
	 * argument (iff it exists).
	 * </ol>
	 * <b>NB</b> The Javassist compiler
	 * <a href="https://www.javassist.org/tutorial/tutorial3.html#generics">does
	 * not fully support generics</a>, so we must ensure that the types are raw.
	 * At compile time, the raw types are equivalent to the generic types, so this
	 * should not pose any issues.
	 *
	 * @param info - the {@link ReducedOpInfo} containing the information needed
	 *          to write the method.
	 * @return a {@link String} that can be used by
	 *         {@link CtMethod#make(String, CtClass)} to generate the functional
	 *         method of the reduced Op
	 */
	private static String createFunctionalMethod(ReducedOpInfo info) {
        var sb = new StringBuilder();

		// determine the name of the functional method
        var fIface = FunctionalInterfaces.findFrom(info.opType());
        var m = FunctionalInterfaces.functionalMethodOf(fIface);
        var srcFIface = FunctionalInterfaces.findFrom(info.srcInfo().opType());
        var srcM = FunctionalInterfaces.functionalMethodOf(srcFIface);
		// determine the name of the output:
        var opOutput = "out";

		// -- signature -- //
		sb.append(generateSignature(m));

		// -- body --//

		// processing
		sb.append(" {");
		if (Infos.hasPureOutput(info)) {
			sb.append("return ");
		}
		sb.append("op." + srcM.getName() + "(");
		int i;
        var totalArguments = info.srcInfo().inputs();
        var totalArgs = totalArguments.size();
        var totalOptionals = totalArguments.parallelStream().filter(
			member -> !member.isRequired()).count();
        var neededOptionals = totalOptionals - info.paramsReduced();
        var reducedArg = 0;
        var nullables = 0;
		for (i = 0; i < totalArgs; i++) {
			// NB due to our nullability paradigm (if there are n nullable parameters,
			// they must be the last n), we just need to pass null for the last n
			// arguments
			if (totalArguments.get(i).isRequired()) {
				sb.append(" in" + reducedArg++);
			}
			else if (nullables < neededOptionals) {
				sb.append(" in" + reducedArg++);
				nullables++;
			}
			else {
				sb.append(" null");
			}
			if (i + 1 < totalArguments.size()) sb.append(",");
		}

		sb.append(");");

		sb.append("}");
		return sb.toString();
	}

	/**
	 * Returns the index of the argument that is both the input and the output.
	 * <b>If there is no such argument (i.e. the Op produces a pure output), -1 is
	 * returned</b>
	 *
	 * @return the index of the mutable argument.
	 */
	private static int ioArgIndex(final OpInfo info) {
        var inputs = info.inputs();
        var ioArg = inputs.stream().filter(m -> m.isInput() && m
			.isOutput()).findFirst();
		if (ioArg.isEmpty()) return -1;
        var ioMember = ioArg.get();
		return inputs.indexOf(ioMember);
	}

	private static boolean hasPureOutput(final OpInfo info) {
		return ioArgIndex(info) == -1;
	}

	private static String generateSignature(Method m) {
        var sb = new StringBuilder();
        var methodName = m.getName();

		// method modifiers
        var isVoid = m.getReturnType() == void.class;
		sb.append("public " + (isVoid ? "void" : "Object") + " " + methodName +
			"(");

        var inputs = m.getParameterCount();
		for (var i = 0; i < inputs; i++) {
			sb.append(" Object in" + i);
			if (i < inputs - 1) sb.append(",");
		}

		sb.append(" )");

		return sb.toString();
	}

}
