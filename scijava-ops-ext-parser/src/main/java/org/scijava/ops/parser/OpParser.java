/*-
 * #%L
 * SciJava Ops External Parser: A tool for parsing external libraries to ops
 * %%
 * Copyright (C) 2024 SciJava developers.
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

package org.scijava.ops.parser;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Entry point for parsing external libraries to ops (via {@link #main}). <br/>
 * Expected YAML structure is:
 * <ul>
 * <li>namespace:string # Optional string for aliasing methods to
 * "namespace.methodName"</li>
 * <li>version:string # Optional version number for versioning input YAML</li>
 * <li>authors:List #Optional list of authors to apply to all ops</li>
 * <li>containers:List # Optional list fully-qualified data structure classes to
 * consider as potential containers</li>
 * <li>class:(method:alias) # One or more. Map of class name containing op
 * methods to a map of method names within that class, mapped to the "SciJava
 * Ops-style" name for that method</li>
 * </ul>
 *
 * @author Mark Hiner
 */
public final class OpParser {

	private static final String NS_KEY = "namespace";
	private static final String CONTAINER_KEY = "containers";
	private static final String VERSION_KEY = "version";
	private static final String AUTHOR_KEY = "authors";

	/**
	 * @param args One argument is required: path to a YAML file containing Op
	 *          information to wrap.
	 * @throws ClassNotFoundException If any of the indicated classes to be
	 *           wrapped as ops are not found.
	 */
	public static void main(String... args) throws ClassNotFoundException {
		if (args.length < 1) {
			throw new RuntimeException("OpParser requires at least one argument: " //
				+ "a YAML file containing mappings of fully-qualified class names " //
				+ "to subsequent mappings of static methods (without params) in" //
				+ "that class to op names.");
		}

		Yaml yaml = new Yaml();
		String namespace = null;
		List<OpData> ops = new ArrayList<>();
		Set<String> containerClasses = new HashSet<>();
		List<String> authors = new ArrayList<>();
		String version = "unknown";
		Map<String, Object> opsYaml;

		// -- Parse the yaml --
		try (FileReader reader = new FileReader(args[0])) {
			opsYaml = yaml.load(reader);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		// -- Extract basic metadata --
		if (opsYaml.containsKey(NS_KEY)) {
			namespace = (String) opsYaml.remove(NS_KEY);
		}
		if (opsYaml.containsKey(CONTAINER_KEY)) {
			containerClasses = new HashSet<>((List<String>) opsYaml.remove(
				CONTAINER_KEY));
		}
		if (opsYaml.containsKey(VERSION_KEY)) {
			version = (String) opsYaml.remove(VERSION_KEY);
		}
		if (opsYaml.containsKey(AUTHOR_KEY)) {
			authors = (List<String>) opsYaml.remove(AUTHOR_KEY);
		}

		// We assume the remaining entries are nested maps of classes to (maps of
		// method names to an alias for that method).
		for (Map.Entry<String, Object> opDeclaration : opsYaml.entrySet()) {
			final String className = opDeclaration.getKey();
			final Class<?> clazz = Class.forName(className);
			// As our YAML specification for desired method names, we want all
			// overloaded implementations of those methods.
			Multimap<String, Method> methods = makeMultimap(clazz);
			final Map<String, String> opMethods = (Map<String, String>) opDeclaration
				.getValue();
			for (Map.Entry<String, String> opMethod : opMethods.entrySet()) {
				final String methodName = opMethod.getKey();
				List<String> opNames = new ArrayList<>();
				// The value for each op method is the "SciJava Ops-style" name
				opNames.add(opMethod.getValue());

				// If a namespace is specified, we also alias the Op by its method name
				// for a "classic" path to calling the op
				if (namespace != null) {
					opNames.add(namespace + "." + methodName);
				}

				// For each overloaded method we create one OpData instance
				for (Method method : methods.get(methodName)) {
					Map<String, Object> tags = new HashMap<>();
					List<OpParameter> params = new ArrayList<>();
					String opSource = parseOpSource(className, methodName, method
						.getParameterTypes());
					parseParams(method, params, tags, containerClasses);
					OpData data = new OpData(opSource, version, opNames, params, tags,
						authors);
					ops.add(data);
				}
			}
		}

		// Write out the results
		try {
			outputYamlDoc(ops);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Helper method to generate a properly formatted "source" string
	 *
	 * @param className The base class containing the target Op
	 * @param methodName The method within the base class that will be the Op's
	 *          source
	 * @param parameterTypes The array of parameters for the given method
	 * @return A source string that can be written as part of an op.yaml
	 */
	private static String parseOpSource(String className, String methodName,
		Class<?>[] parameterTypes)
	{
		String opSource = "javaMethod:/" //
			+ className //
			+ "." //
			+ methodName //
			+ "%28" //
			+ Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors
				.joining("%2C")) //
			+ "%29";
		return opSource;
	}

	/**
	 * Helper method to populate 1) the list of {@link OpParameter}s and 2) tag
	 * {@link Map} required for a particular Op's YAML. <br/>
	 * Methods are assumed to be functions unless 2 or more {@code containerClass}
	 * parameters are found in {@code params}. In that case, the second entry is
	 * considered the {@code CONTAINER} and the {@code tags} will include an
	 * appropriate {@code ComputerN} entry.
	 *
	 * @param method The {@link Method} being wrapped to an Op
	 * @param params Empty list of {@link OpParameter}s to be populated
	 * @param tags Empty {@link Map} of tags to be populated
	 * @param containerClasses Known parameter classes that should be considered
	 *          potential {@code IO_TYPE#CONTAINER}s
	 */
	private static void parseParams(Method method, List<OpParameter> params,
		Map<String, Object> tags, Set<String> containerClasses)
	{
		int containersSeen = 0;
		int containerIndex = -1;

		// Iterate over each parameter
		Class<?>[] types = method.getParameterTypes();
		for (int i = 0; i < types.length; i++) {
			String className = types[i].getName();
			OpParameter.IO_TYPE ioType = OpParameter.IO_TYPE.INPUT;
			String paramName = "in" + (i + 1 - (containersSeen > 1 ? 1 : 0));
			// If this the second containerClass seen, mark it as the output type and
			// include a Computer tag
			if (containerClasses.contains(className)) {
				if (containersSeen == 1) {
					ioType = OpParameter.IO_TYPE.CONTAINER;
					paramName = "out";
					containerIndex = i + 1;
					tags.put("type", "Computer" + containerIndex);
				}
				containersSeen++;
			}
			params.add(new OpParameter(paramName, className, ioType, ""));
		}
	}

	/**
	 * Helper method to generate a multimap of method names to all methods with
	 * that name from a particular class. Essentially collecting lists of
	 * overloaded methods.
	 */
	private static Multimap<String, Method> makeMultimap(final Class<?> clazz) {
		Multimap<String, Method> multimap = MultimapBuilder.hashKeys()
			.arrayListValues().build();
		for (Method m : clazz.getMethods()) {
			multimap.put(m.getName(), m);
		}
		return multimap;
	}

	/**
	 * Helper method to write an {@link OpData} list to an {@code op.yaml} file.
	 */
	private static void outputYamlDoc(List<OpData> collectedData)
		throws IOException
	{
		var data = collectedData.stream().map(OpData::dumpData).collect(Collectors
			.toList());
		Yaml yaml = new Yaml();
		String doc = yaml.dump(data);
		File f = new File("op.yaml");
		try (OutputStream os = new FileOutputStream(f)) {
			os.write(doc.getBytes(UTF_8));
		}
	}
}
