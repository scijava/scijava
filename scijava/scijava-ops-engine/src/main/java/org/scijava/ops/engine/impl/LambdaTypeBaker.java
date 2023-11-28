/*-
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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
package org.scijava.ops.engine.impl;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import org.scijava.ops.api.Ops;
import org.scijava.types.GenericTyped;
import org.scijava.types.Types;
import org.scijava.types.inference.InterfaceInference;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public final class LambdaTypeBaker {

	/**
	 * Prevents instantiation of this public utility class
	 */
	private LambdaTypeBaker() {}

	/**
	 * Used to enrich a lambda expression with its generic type. Its usage is
	 * necessary in order to find Ops that could take this lamdba expression as an
	 * argument.
	 * <p>
	 * Suppose, for example, that a user has written a lambda
	 * {@code Computers.Arity1<Double, Double> computer}, but a user wishes to
	 * have a {@code Computers.Arity1<Iterable<Double>, Iterable<Double>>}. They
	 * know of an Op in the {@code OpEnvironment} that is able to adapt
	 * {@code computer} so that it is run in a parallel fashion. They cannot
	 * simply call
	 * <p>
	 *
	 * <pre>{@code
	 * op("adapt")
	 *   .input(computer)
	 *   .outType(new Nil&lt;Computers.Arity1&lt;Iterable&lt;Double&gt;, Iterable&lt;Double&gt;&gt;&gt;() {})
	 *   .apply()
	 * }</pre>
	 *
	 * since the type parameters of {@code computer} are not retained at runtime.
	 * <p>
	 * {@code bakeLambdaType} should be used as a method of retaining that fully
	 * reified lambda type so that the lambda can be used
	 * <p>
	 * Note: {@code bakeLambdaType} <b>does not</b> need to be used with anonymous
	 * subclasses; these retain their type parameters at runtime. It is only
	 * lambda expressions that need to be passed to this method.
	 * 
	 * @param <T> The type of the op instance to enrich.
	 * @param originalOp The op instance to enrich.
	 * @param reifiedType The intended generic type of the object to be known at runtime.
	 * @return An enriched version of the object with full knowledge of its
	 *         generic type.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T bakeLambdaType(T originalOp, Type reifiedType) {
		ensureImplementation(originalOp, reifiedType);
		try {
			return (T) javassistOp(originalOp, reifiedType);
		}
		catch (Throwable exc) {
			throw new RuntimeException(exc);
		}
	}

	private static <T> void ensureImplementation(T originalOp, Type reifiedType) {
		Class<?> opClass = originalOp.getClass();
		Class<?> opFIFace = Ops.findFunctionalInterface(opClass);
		Class<?> typeFIFace = Ops.findFunctionalInterface(Types.raw(reifiedType));
		if (!opFIFace.equals(typeFIFace)) {
			throw new IllegalArgumentException(originalOp + " does not implement " + Types.raw(reifiedType));
		}
	}

	/**
	 * Creates a Class given an Op and the reified {@link Type}. This class:
	 * <ul>
	 * <li>is of the same functional type as the given Op</li>
	 * <li>knows its generic type by nature of being a {@link GenericTyped}</li>
	 * </ul>
	 * 
	 * @param originalOp - the Op that will be simplified
	 * @param reifiedType - the {@link Type} to bake into {@code originalOp}.
	 * @return a wrapper of {@code originalOp} taking arguments that are then
	 *         mutated to satisfy {@code originalOp}, producing outputs that are
	 *         then mutated to satisfy the desired output of the wrapper.
	 */
	private static Object javassistOp(Object originalOp, Type reifiedType) throws Throwable {
		ClassPool pool = ClassPool.getDefault();

		// Create wrapper class
		String className = formClassName(originalOp, reifiedType);
		Class<?> c;
		try {
			c = pool.getClassLoader().loadClass(className);
		}
		catch (ClassNotFoundException e) {
			CtClass cc = generateSimplifiedWrapper(pool, className, reifiedType);
			c = cc.toClass(MethodHandles.lookup());
		}

		// Return Op instance
		return c.getDeclaredConstructor(Types.raw(reifiedType), Type.class)
			.newInstance(originalOp, reifiedType);
	}

	// TODO: consider correctness
	private static String formClassName(Object op,  Type reifiedType) {
		// package name - required to be this package for the Lookup to work
		String packageName = LambdaTypeBaker.class.getPackageName();
		StringBuilder sb = new StringBuilder(packageName + ".");

		// class name
		String implementationName = op.getClass().getSimpleName();
		String originalName = implementationName.substring(implementationName
			.lastIndexOf('.') + 1); // we only want the class name
		String className = originalName.concat("_typeBaked_" + reifiedType.getTypeName());
		className = className.replaceAll("[^a-zA-Z0-9_]", "_");
		if(className.chars().anyMatch(c -> !Character.isJavaIdentifierPart(c)))
			throw new IllegalArgumentException(className + " is not a valid class name!");

		sb.append(className);
		return sb.toString();
	}

	private static CtClass generateSimplifiedWrapper(ClassPool pool, String className, Type reifiedType) throws Throwable
	{
		CtClass cc = pool.makeClass(className);

		// Add implemented interface
		CtClass jasOpType = pool.get(Types.raw(reifiedType).getName());
		cc.addInterface(jasOpType);
		CtClass genTyped = pool.get(GenericTyped.class.getName());
		cc.addInterface(genTyped);

		// Add Op field
		CtField opField = createTypeField(pool, cc, Types.raw(reifiedType), "op");
		cc.addField(opField);

		// Add Type field
		CtField type = createTypeField(pool, cc, Type.class, "type");
		cc.addField(type);

		// Add constructor to take the Simplifiers, as well as the original op.
		CtConstructor constructor = CtNewConstructor.make(createConstructor(cc, Types.raw(reifiedType)), cc);
		cc.addConstructor(constructor);

		// add functional interface method
		CtMethod functionalMethod = CtNewMethod.make(createFunctionalMethod(reifiedType),
			cc);
		cc.addMethod(functionalMethod);

		CtMethod genTypedMethod = CtNewMethod.make(createGenericTypedMethod(), cc);
		cc.addMethod(genTypedMethod);

		// add GenericTyped method
		return cc;
	}

	private static String createGenericTypedMethod() {
		return "public java.lang.reflect.Type getType() {return type;}";
	}

	private static CtField createTypeField(ClassPool pool, CtClass cc, Class<?> opType, String fieldName)
			throws NotFoundException, CannotCompileException
		{
			CtClass fType = pool.get(opType.getName());
			CtField f = new CtField(fType, fieldName, cc);
			f.setModifiers(Modifier.PRIVATE + Modifier.FINAL);
			return f;
		}

	private static String createConstructor(CtClass cc, Class<?> opType) {
		StringBuilder sb = new StringBuilder();
		// constructor signature
		sb.append("public " + cc.getSimpleName() + "( ");

		// op
		sb.append(" " + opType.getName() + " op, java.lang.reflect.Type type ) {");
		// assign type to field
		sb.append("this.op = op;");
		sb.append("this.type = type;");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Creates the functional method of a simplified Op. This functional method
	 * simply wraps the lambda's method:
	 * <b>NB</b> The Javassist compiler
	 * <a href="https://www.javassist.org/tutorial/tutorial3.html#generics">does
	 * not fully support generics</a>, so we must ensure that the types are raw.
	 * At compile time, the raw types are equivalent to the generic types, so this
	 * should not pose any issues.
	 * 
	 * @param reifiedType - the {@link Type} of the lambda, containing the
	 *          information needed to write the method.
	 * @return a {@link String} that can be used by
	 *         {@link CtMethod#make(String, CtClass)} to generate the functional
	 *         method of the simplified Op
	 */
	private static String createFunctionalMethod(Type reifiedType) {
		StringBuilder sb = new StringBuilder();

		// determine the name of the functional method
		Class<?> raw = Types.raw(reifiedType);
		Method m = InterfaceInference.singularAbstractMethod(raw);

		//-- signature -- //
		sb.append(generateSignature(m));

		//-- body --//

		sb.append(" {");

		// processing
		if (m.getReturnType() != void.class) {
			sb.append("return ");
		}
		sb.append("op." + m.getName() + "(");
		for (int i = 0; i < m.getParameterCount(); i++) {
			sb.append(" in" + i);
			if (i + 1 < m.getParameterCount()) sb.append(",");
		}
		sb.append(");");

		sb.append("}");
		return sb.toString();
	}

	private static String generateSignature(Method m) {
		StringBuilder sb = new StringBuilder();
		String methodName = m.getName();

		// method modifiers
		boolean isVoid = m.getReturnType() == void.class;
		sb.append("public " + (isVoid ? "void" : "Object") + " " + methodName +
			"(");

		int inputs = m.getParameterCount();
		for (int i = 0; i < inputs; i++) {
			sb.append(" Object in" + i);
			if (i < inputs - 1) sb.append(",");
		}

		sb.append(" )");

		return sb.toString();
	}

}
