package org.scijava.ops.engine.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.scijava.InstantiableException;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpInfoGenerator;
import org.scijava.ops.api.OpUtils;
import org.scijava.ops.engine.matcher.impl.OpFieldInfo;
import org.scijava.ops.engine.matcher.impl.OpMethodInfo;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.util.ClassUtils;


public class PluginBasedOpCollectionInfoGenerator implements OpInfoGenerator {

	private final PluginService service;

	public PluginBasedOpCollectionInfoGenerator(PluginService service) {
		this.service = service;
	}

	@Override
	public List<OpInfo> generateInfos() {
		List<OpInfo> infos = new ArrayList<>();
		for (PluginInfo<OpCollection> info : service.getPluginsOfType(OpCollection.class) ) {
			try {
				Class<?> c = info.loadClass();
				final List<Field> fields = ClassUtils.getAnnotatedFields(c, OpField.class);
				Object instance = null;
				for (Field field : fields) {
					final boolean isStatic = Modifier.isStatic(field.getModifiers());
					if (!isStatic && instance == null) {
						instance = field.getDeclaringClass().newInstance();
					}
					String unparsedOpNames = field.getAnnotation(OpField.class).names();
					String[] parsedOpNames = OpUtils.parseOpNames(unparsedOpNames);
					infos.add(new OpFieldInfo(isStatic ? null : instance, field,
						parsedOpNames));
				}
				final List<Method> methods = ClassUtils.getAnnotatedMethods(c, OpMethod.class);
				for (final Method method: methods) {
					String unparsedOpNames = method.getAnnotation(OpMethod.class).names();
					String[] parsedOpNames = OpUtils.parseOpNames(unparsedOpNames);
					infos.add(new OpMethodInfo(method, parsedOpNames));
				}
			} catch (InstantiationException | IllegalAccessException | InstantiableException exc) {
				// TODO: Consider how best to handle this.
				exc.printStackTrace();
			}
		}
		return infos;
	}

}
