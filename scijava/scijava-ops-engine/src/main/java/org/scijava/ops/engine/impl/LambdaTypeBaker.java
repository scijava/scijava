package org.scijava.ops.engine.impl;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import org.scijava.ops.engine.OpUtils;
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

public class LambdaTypeBaker {

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
		Class<?> opFIFace = OpUtils.findFunctionalInterface(opClass);
		Class<?> typeFIFace = OpUtils.findFunctionalInterface(Types.raw(reifiedType));
		if (!opFIFace.equals(typeFIFace)) {
			throw new IllegalArgumentException(originalOp + " does not implement " + Types.raw(reifiedType));
		}
	}

	/**
	 * Creates a Class given an Op and the reified {@link Type}. This class:
	 * <ul>
	 * <li>is of the same functional type as the given Op
	 * <li>knows its generic type by nature of being a {@link GenericTyped}
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
