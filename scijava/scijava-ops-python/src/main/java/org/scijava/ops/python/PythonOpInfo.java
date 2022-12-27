
package org.scijava.ops.python;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.scijava.common3.validity.ValidityException;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpDependencyMember;
import org.scijava.ops.api.OpInfo;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.types.Types;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class PythonOpInfo implements OpInfo {

	private final List<String> names;
	private final double priority;
	private final String version;
	private final String source;
	private final Hints hints = new TempHints();
	private final Type opType;

	private final Struct struct;

	public PythonOpInfo(List<String> names, final Class<?> opType,
		double priority, String version, String source,
		List<Map<String, Object>> parameters)
	{
		this.names = names;
		this.priority = priority;
		this.version = version;
		this.source = source;

		List<Member<?>> members;
		try {
			members = parseParams(parameters);
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		this.opType = Types.parameterize(opType, members.stream().map(
			Member::getType).toArray(Type[]::new));

		struct = () -> members;
	}

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
		return source;
	}

	@Override
	public StructInstance<?> createOpInstance(List<?> dependencies) {
		try {
			return struct().createInstance(javassistOp(source));
		}
		catch (Throwable ex) {
			throw new IllegalStateException("Failed to invoke Op method: " + source +
				". Provided Op dependencies were: " + Objects.toString(dependencies),
				ex);
		}

	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public ValidityException getValidityException() {
		return null;
	}

	@Override
	public AnnotatedElement getAnnotationBearer() {
		return null;
	}

	@Override
	public String version() {
		return version;
	}

	@Override
	public String id() {
		return null;
	}

	/**
	 * TODO: This is SUPER hacky. Yeehaw!
	 * 
	 */
	private static List<Member<?>> parseParams(List<Map<String, Object>> params)
		throws ClassNotFoundException
	{
		List<Member<?>> members = new ArrayList<>();
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		for (Map<String, Object> map : params) {
			Class<?> type = cl.loadClass((String) map.get("type"));
			String description = (String) map.getOrDefault("description", "");
			List<String> keys = new ArrayList<>(map.keySet());
			keys.remove("type");
			keys.remove("description");
			String ioType = keys.get(0);
			String key = (String) map.get(ioType);
			members.add(new Member<>() {

				@Override
				public String getKey() {
					return key;
				}

				@Override
				public Type getType() {
					return type;
				}

				@Override
				public ItemIO getIOType() {
					switch (ioType) {
						case "input":
							return ItemIO.INPUT;
						case "output":
							return ItemIO.OUTPUT;
						case "container":
							return ItemIO.CONTAINER;
						case "mutable":
							return ItemIO.MUTABLE;
						default:
							throw new IllegalStateException("Invalid IO type");
					}
				}

				@Override
				public String getDescription() {
					return description;
				}
			});

		}
		return members;
	}

	private Object javassistOp(String source) throws Throwable {
		ClassPool pool = ClassPool.getDefault();

		// Create wrapper class
		String className = formClassName(source);
		Class<?> c;
		try {
			CtClass cc = pool.makeClass(className);

			// Add implemented interface
			CtClass jasOpType = pool.get(Types.raw(opType).getName());
			cc.addInterface(jasOpType);

			// Add constructor
			CtConstructor constructor = CtNewConstructor.make(createConstructor(cc), cc);
			cc.addConstructor(constructor);

			// add functional interface method
			CtMethod functionalMethod = CtNewMethod.make(createFunctionalMethod(
				source), cc);
			cc.addMethod(functionalMethod);
			c = cc.toClass(MethodHandles.lookup());
		}
		catch (Exception e) {
			c = this.getClass().getClassLoader().loadClass(className);
		}

		// Return Op instance
		return c.getDeclaredConstructor().newInstance();
	}

	private String formClassName(String source) {
		// package name
		String packageName = PythonOpInfo.class.getPackageName();

		// class name -> OwnerName_PythonFunction
		List<String> nameElements = List.of(source.split("\\."));
		String className = packageName + "." + String.join("_", nameElements);
		return className;
	}

	private String createConstructor(CtClass cc)
	{
		// constructor signature
		return "public " + cc.getSimpleName() + "() {}";
	}

	private String createFunctionalMethod(String source) {
		StringBuilder sb = new StringBuilder();
		return sb.toString();
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
}
