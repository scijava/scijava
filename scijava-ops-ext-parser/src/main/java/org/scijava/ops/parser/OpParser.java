/*-
 * #%L
 * A tool for wrapping external libraries as SciJava Ops.
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

import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Entry point for parsing external libraries to ops (via {@link #main}). <br>
 * Expected YAML structure is:
 * <ul>
 * <li>namespace:String # Optional string for aliasing methods to
 * "namespace.methodName"</li>
 * <li>version:String # Optional version number for versioning input YAML</li>
 * <li>authors:String|List #Optional one or more authors to apply to all
 * ops</li>
 * <li>class:(Op map) # One or more. Map of class name containing op methods to
 * a map of method names within that class. Supported fields for each method
 * include:
 * <ul>
 * <li>priority:String # Optional priority for Ops of this method</li>
 * <li>description:String # Optional description for Ops of this method</li>
 * <li>authors:String|List # Optional, as global list, but per-method list takes
 * precedence</li>
 * <li>type:String # Optional SciJava Ops type shorthand (e.g. Computer2). If
 * omitted, Ops for this method are assumed to be functions.</li>
 * <li>alias:String|List # Optional, one or more names for the Ops to be aliased
 * under. This is in addition to the namespace aliasing. If omitted, defaults to
 * 'ext.Method'</li>
 * </ul>
 * </li>
 * </ul>
 *
 * @author Mark Hiner
 */
public final class OpParser {

	private static final String NS_KEY = "namespace";
	private static final String VERSION_KEY = "version";
	private static final String AUTHOR_KEY = "authors";
	private static final String PRIORITY_KEY = "priority";
	private static final String ALIAS_KEY = "alias";
	private static final String TYPE_KEY = "type";
	private static final String DESCRIPTION_KEY = "description";

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

		// Parse the config yaml to an Op yaml representation
		final var opYaml = parseOpDocument(args[0]);

		// Write out the results
		try {
			outputYamlDoc(opYaml);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Performs the actual parsing logic
	 *
	 * @param inputYamlPath YAML file to parse
	 * @return A string representation of the resulting YAML Op configuration
	 */
	public static String parseOpDocument(String inputYamlPath)
		throws ClassNotFoundException
	{
        var configYaml = new Yaml();
		String namespace = null;
		List<OpData> ops = new ArrayList<>();
		List<String> authors = new ArrayList<>();
        var version = "unknown";
		Map<String, Object> opsYaml;

		// -- Parse the yaml --
		try (var reader = new FileReader(inputYamlPath)) {
			opsYaml = configYaml.load(reader);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		// -- Extract basic metadata --
		if (opsYaml.containsKey(NS_KEY)) {
			namespace = (String) opsYaml.remove(NS_KEY);
		}
		if (opsYaml.containsKey(VERSION_KEY)) {
			version = (String) opsYaml.remove(VERSION_KEY);
		}
		if (opsYaml.containsKey(AUTHOR_KEY)) {
			authors = getListHelper(AUTHOR_KEY, opsYaml);
			opsYaml.remove(AUTHOR_KEY);
		}

		// We assume the remaining entries are nested maps of classes to (maps of
		// method names to an alias for that method).
		for (var opDeclaration : opsYaml.entrySet()) {
			final var className = opDeclaration.getKey();
			final var clazz = Class.forName(className);
			// As our YAML specification for desired method names, we want all
			// overloaded implementations of those methods.
            var methods = makeMultimap(clazz);
			final var opMethods =
				(Map<String, Map<String, Object>>) opDeclaration.getValue();
			for (var opMethod : opMethods
				.entrySet())
			{
				final var methodName = opMethod.getKey();
				final List<String> opNames = new ArrayList<>();
				final var opMetadata = opMethod.getValue();
				final var opType = (String) opMetadata.getOrDefault(TYPE_KEY, "");
				final var description = (String) opMetadata.getOrDefault(
					DESCRIPTION_KEY, "");
				final var priority = Double.parseDouble((String) opMetadata
					.getOrDefault(PRIORITY_KEY, "0.0"));

				if (opMetadata.containsKey(ALIAS_KEY)) {
                    var alias = opMetadata.get(ALIAS_KEY);
					if (alias instanceof String) {
						opNames.add((String) alias);
					}
					else if (alias instanceof List) {
						opNames.addAll((List<String>) alias);
					}
				}
				else {
					opNames.add("ext." + methodName);
				}

                var opAuthors = authors;
				if (opMetadata.containsKey(AUTHOR_KEY)) {
					opAuthors = getListHelper(AUTHOR_KEY, opMetadata);
				}

				// If a global namespace is specified, we also alias the Op by its
				// method name
				// for a "classic" path to calling the op
				if (namespace != null) {
					opNames.add(namespace + "." + methodName);
				}

				// For each overloaded method we create one OpData instance
				if (!methods.containsKey(methodName)) {
					throw new InvalidOpException("No method named " + methodName +
						" in class " + className);
				}
				for (var method : methods.get(methodName)) {
					Map<String, Object> tags = new HashMap<>();
					List<OpParameter> params = new ArrayList<>();
                    var opSource = parseOpSource(className, methodName, method
						.getParameterTypes());
					parseParams(method, params, tags, opType);
                    var data = new OpData(opSource, version, opNames, params, tags,
						opAuthors, priority, description);
					ops.add(data);
				}
			}
		}

		var data = ops.stream().map(OpData::dumpData).collect(Collectors.toList());
		return new Yaml().dump(data);
	}

	/**
	 * Helper method to extract a key from a map that may point to a single
	 * {@link String}, or a {@link List} thereof.
	 */
	private static List<String> getListHelper(String key,
		Map<String, Object> map)
	{
        var value = map.get(key);
		if (value instanceof List) {
			return (List<String>) value;
		}
		List<String> result = new ArrayList<>();
		result.add((String) value);
		return result;
	}

	/**
	 * Helper method to generate a properly formatted "source" string
	 *
	 * @param className The base class containing the target Op
	 * @param methodName The method within the base class that will be the Op's
	 *          source
	 * @param parameterTypes The array of parameters for the given method
	 * @return A source string that can be written as part of an ops.yaml
	 */
	private static String parseOpSource(String className, String methodName,
		Class<?>[] parameterTypes)
	{
		return "javaMethod:/" //
			+ URLEncoder.encode(className //
				+ "." //
				+ methodName //
				+ Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors
					.joining(",", "(", ")")), UTF_8);
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
	 * @param type Empty string if an {@code ItemIO.FUNCTION}, otherwise
	 *          "ComputerN" where N is the parameter index of the container.
	 */
	private static void parseParams(final Method method,
		final List<OpParameter> params, Map<String, Object> tags, final String type)
	{
        var containerIndex = -1;
		if (!Strings.isNullOrEmpty(type)) {
			containerIndex = Integer.parseInt(type.substring(type.length() - 1)) - 1;
		}

		// Iterate over each parameter
        var types = method.getParameterTypes();
		for (var i = 0; i < types.length; i++) {
            var className = types[i].getName();
            var ioType = OpParameter.IO_TYPE.INPUT;
            var paramName = method.getParameters()[i].getName();
			if (i == containerIndex) {
				ioType = OpParameter.IO_TYPE.CONTAINER;
				tags.put("type", type);
			}
			params.add(new OpParameter(paramName, className, ioType, ""));
		}

		if (containerIndex < 0) {
			method.getReturnType();
            var ioType = OpParameter.IO_TYPE.OUTPUT;
            var paramName = "output";
			params.add(new OpParameter(paramName, method.getReturnType().getName(),
				ioType, ""));
		}
	}

	/**
	 * Helper method to generate a multimap of method names to all methods with
	 * that name from a particular class. Essentially collecting lists of
	 * overloaded methods.
	 */
	private static Multimap<String, Method> makeMultimap(final Class<?> clazz) {
		Multimap<String, Method> multimap = MultimapBuilder.treeKeys()
			.treeSetValues(OpParser::compareParamCount).build();
		for (var m : clazz.getMethods()) {
			multimap.put(m.getName(), m);
		}
		return multimap;
	}

	/**
	 * Simple comparison method for two {@link Method}s that just orders by number
	 * of parameters.
	 */
	private static int compareParamCount(Method m1, Method m2) {
        var result = Integer.compare(m1.getParameterCount(), m2
			.getParameterCount());
		for (var i = 0; result == 0 && i < m1.getParameterCount(); i++) {
			result = m1.getParameterTypes()[i].getName().compareTo(m2
				.getParameterTypes()[i].getName());
		}
		return result;
	}

	/**
	 * Helper method to write an {@link OpData} list to an {@code ops.yaml} file.
	 */
	private static void outputYamlDoc(String opYaml) throws IOException {
        var f = new File("ops.yaml");
		try (OutputStream os = new FileOutputStream(f)) {
			os.write(opYaml.getBytes(UTF_8));
		}
	}

	/**
	 * A data structure containing all the metadata needed to define an Op
	 *
	 * @author Gabriel Selzer
	 * @author Mark Hiner
	 */
	private static class OpData {

		/**
		 * A {@link Map} used to store any implementation-specific and/or nullable
		 * tags.
		 */
		private final Map<String, Object> tags;

		/**
		 * A {@link List} of {@link String}s describing the name(s) of the Op. There
		 * must be at least one.
		 */
		private final List<String> names;

		/**
		 * A {@link List} of {@link OpParameter}, describing the input and output
		 * parameters of this Op.
		 */
		private final List<OpParameter> params;

		/**
		 * A {@link String} identifying the code providing an Op's functionality.
		 */
		private final String source;

		/**
		 * The version of this Op.
		 */
		private final String version;

		/**
		 * The priority of this Op.
		 */
		private final double priority;

		/**
		 * A description of the functionality provided by this Op.
		 */
		private final String description;

		/**
		 * A {@link List} of the authors of this Op
		 */
		private final List<String> authors;

		public OpData(final String source, final String version,
					  final List<String> names, final List<OpParameter> params,
					  final Map<String, Object> tags, List<String> authors, double priority,
					  String description)
		{
			this.source = source;
			this.version = version;
			this.names = names;
			this.params = params;
			this.tags = tags;
			this.authors = authors;
			this.priority = priority;
			this.description = description;

			validateOpData();
		}

		/**
		 * Helper method to ensure this OpImpl is valid. Throws
		 * {@link InvalidOpException} if problems are detected. Parallel
		 * implementation of org.scijava.ops.engine.util.Infos#validate in
		 * scijava-ops-engine
		 */
		private void validateOpData() {
			if (Objects.isNull(names) || names.isEmpty()) {
				throw new InvalidOpException("Invalid Op defined in : " + source +
						". Op names cannot be empty!");
			}

            var outputs = 0;
			for (var p : params) {
				if (p.ioType.equals(OpParameter.IO_TYPE.OUTPUT)) outputs++;
			}
			if (outputs > 1) {
				throw new InvalidOpException("Invalid Op defined in : " + source +
						". Ops cannot have more than one output!");
			}
		}

		/**
		 * Returns a {@link Map} storing the needed Op data hierarchically.
		 *
		 * @return the {@link Map} of data.
		 */
		public Map<String, Object> dumpData() {
			Map<String, Object> map = new HashMap<>();
			map.put("source", source);
			map.put("version", version);
			map.put("names", names.toArray(String[]::new));
			map.put("description", description);
			map.put("priority", priority);
			map.put("authors", authors.toArray(String[]::new));
            var foo = params.stream() //
					.map(OpParameter::data) //
					.collect(Collectors.toList());
			map.put("parameters", foo.toArray(Map[]::new));
			map.put("tags", tags);
			return Collections.singletonMap("op", map);
		}
	}
}
