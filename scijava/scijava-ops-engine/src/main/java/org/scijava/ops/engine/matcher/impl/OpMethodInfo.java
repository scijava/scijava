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

package org.scijava.ops.engine.matcher.impl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.scijava.common3.validity.ValidityException;
import org.scijava.common3.validity.ValidityProblem;
import org.scijava.meta.Versions;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpDependencyMember;
import org.scijava.ops.api.OpDescription;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.OpUtils;
import org.scijava.ops.engine.struct.MethodOpDependencyMemberParser;
import org.scijava.ops.engine.struct.MethodParameterMemberParser;
import org.scijava.ops.engine.util.Adapt;
import org.scijava.ops.engine.util.internal.OpMethodUtils;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpMethod;
import org.scijava.priority.Priority;
import org.scijava.struct.Member;
import org.scijava.struct.MemberParser;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.struct.Structs;
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
	private final String version;
	private final List<String> names;
	private Type opType;
	private Struct struct;
	private final ValidityException validityException;
	private final double priority;

	private final Hints hints;

	public OpMethodInfo(final Method method, final Class<?> opType, final Hints hints, final String... names) {
		this(method, opType, Versions.getVersion(method.getDeclaringClass()), hints, Priority.NORMAL, names);
	}

	public OpMethodInfo(final Method method, final Class<?> opType, final Hints hints, final double priority,
			final String... names) {
		this(method, opType, Versions.getVersion(method.getDeclaringClass()), hints, Priority.NORMAL, names);
	}

	public OpMethodInfo(final Method method, final Class<?> opType, final String version, final Hints hints, final String... names) {
		this(method, opType, version, hints, Priority.NORMAL, names);
	}

	public OpMethodInfo(final Method method, final Class<?> opType, final String version, final Hints hints, final double priority, final String... names) {
		this.method = method;
		this.version = version;
		this.names = Arrays.asList(names);
		this.hints = hints;
		this.priority = priority;

		final List<ValidityProblem> problems = new ArrayList<>();
		checkModifiers(method, problems);

		this.opType = findOpType(method, opType, problems);
		this.struct = generateStruct(method, opType, problems, new MethodParameterMemberParser(), new MethodOpDependencyMemberParser());

		validityException = problems.isEmpty() ? null : new ValidityException(
			problems);
	}

	@SafeVarargs
	private Struct generateStruct(Method m, Type structType,
		List<ValidityProblem> problems,
		MemberParser<Method, ? extends Member<?>>... memberParsers)
	{
		try {
			return Structs.from(m, structType, problems, memberParsers);
		} catch (IllegalArgumentException e) {
			problems.add(new ValidityProblem(e));
			return null;
		}
	}

	private Type findOpType(Method m, Class<?> opType,
		List<ValidityProblem> problems)
	{
		try {
			return OpMethodUtils.getOpMethodType(opType,
				method);
		}
		catch (IllegalArgumentException e) {
			problems.add(new ValidityProblem(e));
			return null;
		}
	}

	private void checkModifiers(Method m, List<ValidityProblem> problems) {
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
	}

	// -- OpInfo methods --

	@Override
	public List<String> names() {
		return names;
	}

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
		return priority;
	}

	@Override
	public String implementationName() {
		// Get generic string without modifiers and return type
		String fullyQualifiedMethod = method.toGenericString();
		String packageName = method.getDeclaringClass().getPackageName();
		int classNameIndex = fullyQualifiedMethod.indexOf(packageName);
		return fullyQualifiedMethod.substring(classNameIndex);
	}

	@Override
	public StructInstance<?> createOpInstance(
		final List<? extends Object> dependencies)
	{

		// Case 1: no dependencies - lambdaMetaFactory is fastest
		// NB LambdaMetaFactory only works if this Module (org.scijava.ops.engine)
		// can read the Module containing the Op. So we also have to check that.
		Module methodModule = method.getDeclaringClass().getModule();
		Module opsEngine = this.getClass().getModule();

		if (opsEngine.canRead(methodModule) && dependencies.isEmpty()) {
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
				". Provided Op dependencies were: " + dependencies,
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
	 * @param dependencies - the {@link OpDependency}s associated with {@code m}
	 * @return a partial application of {@code m} with all {@link OpDependency}s
	 *         injected.
	 */
	private Object javassistOp(Method m, List<? extends Object> dependencies)
		throws Throwable
	{
		ClassPool pool = ClassPool.getDefault();

		// Create wrapper class
		String className = formClassName(m);
		List<OpDependencyMember<?>> depMembers = dependencies();
		Class<?> c;
		try {
			CtClass cc = pool.makeClass(className);

			// Add implemented interface
			CtClass jasOpType = pool.get(Types.raw(opType).getName());
			cc.addInterface(jasOpType);

			// Add Method field
			CtField methodField = createOpMethodField(pool, cc);
			cc.addField(methodField);

			// Add OpDependency fields
			for (int i = 0; i < depMembers.size(); i++) {
				CtField f = createDependencyField(pool, cc, depMembers.get(i), i);
				cc.addField(f);
			}

			// Add constructor
			CtConstructor constructor = CtNewConstructor.make(createConstructor(
				depMembers, cc), cc);
			cc.addConstructor(constructor);

			// add functional interface method
			CtMethod functionalMethod = CtNewMethod.make(createFunctionalMethod(m),
				cc);
			cc.addMethod(functionalMethod);
			c = cc.toClass(MethodHandles.lookup());
		}
		catch (Exception e) {
			c = this.getClass().getClassLoader().loadClass(className);
		}

		// Return Op instance
		Class<?>[] constructorClasses = constructorClasses(depMembers);
		Object[] constructorObjects = constructorObjects(method, dependencies);

		return c.getDeclaredConstructor(constructorClasses) //
			.newInstance(constructorObjects);
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
		// Return the className
		return packageName + "." + String.join("_", nameElements);
	}

	private CtField createOpMethodField(ClassPool pool, CtClass cc) throws NotFoundException,
			CannotCompileException
	{
		Class<?> depClass = Method.class;
		CtClass fType = pool.get(depClass.getName());
		CtField f = new CtField(fType, "opMethod", cc);
		f.setModifiers(Modifier.PRIVATE + Modifier.FINAL);
		return f;
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
		sb.append("public ") //
			.append(cc.getSimpleName()) //
			.append("(")//
			.append(Method.class.getName()) //
			.append(" opMethod");
		if (depMembers.size() > 0) {
			sb.append(", ");
		}
		for (int i = 0; i < depMembers.size(); i++) {
			Class<?> depClass = depMembers.get(i).getRawType();
			sb.append(depClass.getName()) //
				.append(" dep") //
				.append(i);
			if (i < depMembers.size() - 1) sb.append(",");
		}
		sb.append(") {");

		// assign dependencies to field
		sb.append("this.opMethod = opMethod;");
		for (int i = 0; i < depMembers.size(); i++) {
			sb.append("this.dep") //
				.append(i) //
				.append(" = dep") //
				.append(i) //
				.append(";");
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
		sb.append("public ") //
			.append(isVoid ? "void" : "Object") //
			.append(" ") //
			.append(methodName) //
			.append("(");

		// method inputs
		int applyInputs = inputs().size();
		for (int i = 0; i < applyInputs; i++) {
			sb.append(" Object in") //
				.append(i);
			if (i < applyInputs - 1) sb.append(",");
		}

		// method body
		sb.append(") { return opMethod.invoke(null, new Object[] {");
		int numInputs = 0;
		int numDependencies = 0;
		List<Member<?>> members = struct().members().stream() //
			.filter(member -> !(!member.isInput() && member.isOutput())) //
			.collect(Collectors.toList());
		Parameter[] mParams = m.getParameters();
		for (Parameter mParam : mParams) {
			Class<?> paramRawType = Types.raw(mParam.getParameterizedType());
			String castClassName = paramRawType.getName();
			if (paramRawType.isArray()) castClassName = paramRawType.getSimpleName();
			sb.append("(") //
				.append(castClassName) //
				.append(") ");
			//
			if (mParam.getAnnotation(OpDependency.class) != null)
				sb.append("dep") //
					.append(numDependencies++);
			else sb.append("in") //
				.append(numInputs++);
			if (numDependencies + numInputs < members.size()) sb.append(", ");
		}
		sb.append("}); }");
		return sb.toString();
	}
	private Class<?>[] constructorClasses(List<OpDependencyMember<?>> depMembers) {
		Class<?>[] constructorClasses = new Class<?>[depMembers.size() + 1];
		constructorClasses[0] = Method.class;

		for (int i = 0; i < depMembers.size(); i++) {
			constructorClasses[i + 1] = depMembers.get(i).getRawType();
		}
		return constructorClasses;
	}

	private Object[] constructorObjects(final Method opMethod, final List<?> dependencies) {
		Object[] constructorObjects = new Object[dependencies.size() + 1];
		constructorObjects[0] = opMethod;

		for (int i = 0; i < dependencies.size(); i++) {
			constructorObjects[i + 1] = dependencies.get(i);
		}
		return constructorObjects;
	}

	/**
	 * Returns a "simple" name for {@code Class<?> c}.
	 * <p>
	 * Since this should be a java identifier, it cannot have illegal characters;
	 * thus we replace illegal characters with an underscore.
	 * 
	 * @param c the {@link Class} for which we need an identifier
	 * @return a {@link String} that can identify the class
	 */
	private String getParameterName(Class<?> c) {
		return c.getSimpleName().replaceAll("[^a-zA-Z0-9_]", "_");
	}

	@Override
	public boolean isValid() {
		return validityException == null;
	}

	@Override
	public ValidityException getValidityException() {
		return validityException;
	}

	@Override
	public String version() {
		return version;
	}

	/**
	 * For an {@link OpMethod}, we define the implementation as the concatenation
	 * of:
	 * <ol>
	 * <li>The fully qualified name of the class containing the method</li>
	 * <li>The method name</li>
	 * <li>The method parameters</li>
	 * <li>The version of the class containing the method, with a preceding
	 * {@code @}</li>
	 * </ol>
	 * <p>
	 * For example, for a method {@code baz(Double in1, String in2)} in class
	 * {@code com.example.foo.Bar}, you might have
	 * <p>
	 * {@code com.example.foo.Bar.baz(Double in1,String in2)@1.0.0}
	 */
	@Override
	public String id() {
		return OpInfo.IMPL_DECLARATION + implementationName() + "@" + version();
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
		return OpDescription.basic(this);
	}

	@Override
	public AnnotatedElement getAnnotationBearer() {
		return method;
	}

}
