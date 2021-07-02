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

package org.scijava.ops.matcher.impl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.scijava.Priority;
import org.scijava.ValidityProblem;
import org.scijava.ops.Hints;
import org.scijava.ops.OpDependency;
import org.scijava.ops.OpDependencyMember;
import org.scijava.ops.OpHints;
import org.scijava.ops.OpInfo;
import org.scijava.ops.OpMethod;
import org.scijava.ops.OpUtils;
import org.scijava.ops.ValidityException;
import org.scijava.ops.hint.ImmutableHints;
import org.scijava.ops.struct.MethodOpDependencyMemberParser;
import org.scijava.ops.struct.MethodParameterMemberParser;
import org.scijava.ops.struct.Structs;
import org.scijava.ops.util.Adapt;
import org.scijava.ops.util.OpMethodUtils;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
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

/**
 * @author Marcel Wiedenmann
 */
public class OpMethodInfo implements OpInfo {

	private final Method method;
	private Type opType;
	private Struct struct;
	private final ValidityException validityException;

	private final Hints hints;

	public OpMethodInfo(final Method method) {
		final List<ValidityProblem> problems = new ArrayList<>();
		// Reject all non public methods
		if (!Modifier.isPublic(method.getModifiers())) {
			problems.add(new ValidityProblem("Method to parse: " + method +
				" must be public."));
		}
		if (!Modifier.isStatic(method.getModifiers())) {
			// TODO: Should throw and error if the method is not static.
			// TODO: We can't properly infer the generic types of static methods at
			// the moment. This might be a Java limitation.
			problems.add(new ValidityProblem("Method to parse: " + method +
				" must be static."));
		}
		this.method = method;
		this.hints = formHints(method.getAnnotation(OpHints.class));
		// determine the functional interface this Op should implement
		final OpMethod methodAnnotation = method.getAnnotation(OpMethod.class);
		try {
			opType = OpMethodUtils.getOpMethodType(methodAnnotation.type(),
				method);
			struct = Structs.from(method, problems, new MethodParameterMemberParser(), new MethodOpDependencyMemberParser());
//			struct = ParameterStructs.structOf(method.getDeclaringClass(), method);
		}
		catch (IllegalArgumentException e) {
			problems.add(new ValidityProblem(e));
		}
		validityException = problems.isEmpty() ? null : new ValidityException(
			problems);
	}

	// -- OpInfo methods --

	@Override
	public Type opType() {
		return opType;
	}

	@Override
	public Struct struct() {
		return struct;
	}

	@Override
	public Hints declaredHints() {
		return hints;
	}

	@Override
	public double priority() {
		final OpMethod opMethod = method.getAnnotation(OpMethod.class);
		return opMethod == null ? Priority.NORMAL : opMethod.priority();
	}

	@Override
	public String implementationName() {
		// TODO: This includes all of the modifiers etc. of the method which are not
		// necessary to identify it. Use something custom? We need to be careful
		// because of method overloading, so just using the name is not sufficient.
		return method.toGenericString();
	}

	@Override
	public StructInstance<?> createOpInstance(
		final List<? extends Object> dependencies)
	{

		// Case 1: no dependencies - lambdaMetaFactory is fastest
		if (OpUtils.dependencies(struct()).size() == 0) {
			try {
				method.setAccessible(true);
				MethodHandle handle = MethodHandles.lookup().unreflect(method);
				Object op = Adapt.Methods.lambdaize(Types.raw(opType), handle);
				return struct().createInstance(op);
			}
			catch (Throwable exc) {
				throw new IllegalStateException("Failed to invoke Op method: " + method,
					exc);
			}
		}

		// Case 2: dependenies - Javassist is best
		try {
			return struct().createInstance(javassistOp(method, dependencies));
		}
		catch (Throwable ex) {
			throw new IllegalStateException("Failed to invoke Op method: " + method +
				". Provided Op dependencies were: " + Objects.toString(dependencies),
				ex);
		}
		
	}
	
	/**
	 * Creates a class that knows how to create a partial application of an Op,
	 * and returns that partial application. Specifically, the class knows how to
	 * fix all of the {@link OpDependency}s of an Op, returning an Op taking only
	 * the primary parameters
	 * 
	 * @param m - the {@link OpMethod}
	 * @param dependencies - the {@OpDependency}s associated with {@code m}
	 * @return a partial application of {@code m} with all {@link OpDependency}s
	 *         injected.
	 * @throws Throwable
	 */
	private Object javassistOp(Method m, List<? extends Object> dependencies)
		throws Throwable
	{
		ClassPool pool = ClassPool.getDefault();

		// Create wrapper class
		String className = formClassName(m);
		CtClass cc = pool.makeClass(className);

		// Add implemented interface
		CtClass jasOpType = pool.get(Types.raw(opType).getName());
		cc.addInterface(jasOpType);

		// Add OpDependency fields
		List<OpDependencyMember<?>> depMembers = OpUtils.dependencies(struct());
		for (int i = 0; i < depMembers.size(); i++) {
			CtField f = createDependencyField(pool, cc, depMembers.get(i), i);
			cc.addField(f);
		}

		// Add constructor
		CtConstructor constructor = CtNewConstructor.make(createConstructor(
			depMembers, cc), cc);
		cc.addConstructor(constructor);

		// add functional interface method
		CtMethod functionalMethod = CtNewMethod.make(createFunctionalMethod(m), cc);
		cc.addMethod(functionalMethod);

		// Return Op instance
		Class<?>[] depClasses = depMembers.stream().map(dep -> dep.getRawType())
			.toArray(Class[]::new);
		Class<?> c = cc.toClass(MethodHandles.lookup());
		return c.getDeclaredConstructor(depClasses).newInstance(dependencies
			.toArray());
	}

	private String formClassName(Method m) {
		// package name
		String packageName = OpMethodInfo.class.getPackageName();

		// class name -> OwnerName_MethodName_Params_ReturnType
    List<String> nameElements = new ArrayList<>();
    nameElements.add(m.getDeclaringClass().getSimpleName());
    nameElements.add(m.getName());
    for(Class<?> c : m.getParameterTypes()) {
			nameElements.add(getParameterName(c));
    }
    nameElements.add(m.getReturnType().getSimpleName());
    String className = packageName + "." + String.join("_", nameElements);
		return className;
	}

	private CtField createDependencyField(ClassPool pool, CtClass cc,
		OpDependencyMember<?> dependency, int i) throws NotFoundException,
		CannotCompileException
	{
		Class<?> depClass = dependency.getRawType();
		CtClass fType = pool.get(depClass.getName());
		CtField f = new CtField(fType, "dep" + i, cc);
		f.setModifiers(Modifier.PRIVATE + Modifier.FINAL);
		return f;
	}

	private String createConstructor(List<OpDependencyMember<?>> depMembers,
		CtClass cc)
	{
		StringBuilder sb = new StringBuilder();
		// constructor signature
		sb.append("public " + cc.getSimpleName() + "(");
		for (int i = 0; i < depMembers.size(); i++) {
			Class<?> depClass = depMembers.get(i).getRawType();
			sb.append(depClass.getName() + " dep" + i);
			if (i < depMembers.size() - 1) sb.append(",");
		}
		sb.append(") {");

		// assign dependencies to field
		for (int i = 0; i < depMembers.size(); i++) {
			sb.append("this.dep" + i + " = dep" + i + ";");
		}
		sb.append("}");
		return sb.toString();
	}

	private String createFunctionalMethod(Method m) {
		StringBuilder sb = new StringBuilder();

		// determine the name of the functional method
		String methodName = InterfaceInference.singularAbstractMethod(Types.raw(
			opType)).getName();

		// method modifiers
		boolean isVoid = m.getReturnType() == void.class;
		sb.append("public " + (isVoid ? "void" : "Object") + " " + methodName +
			"(");

		// method inputs
		int applyInputs = OpUtils.inputs(struct()).size();
		for (int i = 0; i < applyInputs; i++) {
			sb.append(" Object in" + i);
			if (i < applyInputs - 1) sb.append(",");
		}

		// method body
		sb.append(") { return " + m.getDeclaringClass().getName() + "." + m
			.getName() + "(");
		int numInputs = 0;
		int numDependencies = 0;
		List<Member<?>> members = struct().members().stream() //
			.filter(member -> !(!member.isInput() && member.isOutput())) //
			.collect(Collectors.toList());
		Parameter[] mParams = m.getParameters();
		for (int i = 0; i < mParams.length; i++) {
			Class<?> paramRawType = Types.raw(mParams[i].getParameterizedType());
			String castClassName = paramRawType.getName();
			if (paramRawType.isArray()) castClassName = paramRawType.getSimpleName();
			sb.append("(" + castClassName + ") ");
			if (mParams[i].getAnnotation(OpDependency.class) != null) sb.append(
				"dep" + numDependencies++);
			else sb.append("in" + numInputs++);
			if (numDependencies + numInputs < members.size()) sb.append(", ");
		}
		sb.append("); }");
		return sb.toString();
	}

	private String getParameterName(Class<?> c) {
		if (!c.isArray()) return c.getSimpleName();
		// TODO: if c is an array, simpleName will include brackets (which is
		// illegal in a class name). To differentiate Object[] from Object, we map
		// Object[] to ObjectArr. This is not truly extensible, since someone
		// could create an ObjectArr class which might conflict, so it would be
		// best to find a better solution.
		return  c.getComponentType().getSimpleName() + "Arr";
	}

	@Override
	public boolean isValid() {
		return validityException == null;
	}

	@Override
	public ValidityException getValidityException() {
		return validityException;
	}

	// -- Object methods --

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof OpMethodInfo)) return false;
		final OpInfo that = (OpInfo) o;
		return struct().equals(that.struct());
	}

	@Override
	public int hashCode() {
		return struct().hashCode();
	}

	@Override
	public String toString() {
		return OpUtils.opString(this);
	}

	@Override
	public AnnotatedElement getAnnotationBearer() {
		return method;
	}

	// -- Helper methods -- //

	private Hints formHints(OpHints h) {
		if (h == null) return new ImmutableHints(new String[0]);
		return new ImmutableHints(h.hints());
	}

}