package org.scijava.ops.engine.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.InstantiableException;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpInfoGenerator;
import org.scijava.ops.api.OpUtils;
import org.scijava.ops.discovery.Discoverer;
import org.scijava.ops.engine.matcher.impl.OpFieldInfo;
import org.scijava.ops.engine.matcher.impl.OpMethodInfo;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;
import org.scijava.plugin.Plugin;
import org.scijava.util.ClassUtils;


public class OpCollectionInfoGenerator implements OpInfoGenerator {

	private final List<Discoverer> discoverers;

	public OpCollectionInfoGenerator(Discoverer... d) {
		this.discoverers = Arrays.asList(d);
	}

	@Override
	public List<OpInfo> generateInfos() {
		List<OpInfo> infos = discoverers.stream() //
			.flatMap(d -> d.implementingClasses(OpCollection.class).stream()) //
			.filter(cls -> cls.getAnnotation(Plugin.class) != null) //
			.map(cls -> {
			try {
				List<OpInfo> collectionInfos = new ArrayList<>();
				final List<Field> fields = ClassUtils.getAnnotatedFields(cls, OpField.class);
				Object instance = null;
				for (Field field : fields) {
					final boolean isStatic = Modifier.isStatic(field.getModifiers());
					if (!isStatic && instance == null) {
						instance = field.getDeclaringClass().newInstance();
					}
					String unparsedOpNames = field.getAnnotation(OpField.class).names();
					String[] parsedOpNames = OpUtils.parseOpNames(unparsedOpNames);
					collectionInfos.add(new OpFieldInfo(isStatic ? null : instance, field,
						parsedOpNames));
				}
				final List<Method> methods = ClassUtils.getAnnotatedMethods(cls, OpMethod.class);
				for (final Method method: methods) {
					String unparsedOpNames = method.getAnnotation(OpMethod.class).names();
					String[] parsedOpNames = OpUtils.parseOpNames(unparsedOpNames);
					collectionInfos.add(new OpMethodInfo(method, parsedOpNames));
				}
				return collectionInfos;
			} catch (InstantiationException | IllegalAccessException exc) {
				// TODO: Consider how best to handle this.
				return null;
			}
			
			}) //
			.filter(list -> list!= null) //
			.flatMap(list -> list.stream()) //
			.collect(Collectors.toList());
		return infos;
	}

}
