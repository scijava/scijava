
package org.scijava.ops.engine.struct;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.scijava.function.Producer;
import org.scijava.ops.engine.exceptions.impl.NullablesOnMultipleMethodsException;
import org.scijava.ops.engine.util.Ops;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.types.inference.InterfaceInference;

/**
 * Lazily generates the parameter data for a {@link List} of
 * {@link FunctionalMethodType}s. <b>If</b> there exists a <b>full</b> set of
 * {@code @param} and {@code @return} tags, the javadoc will be used to create
 * the parameter names and descriptions. Otherwise, reasonable defaults will be
 * used.
 *
 * @author Gabriel Selzer
 */
public class LazilyGeneratedFieldParameterData implements ParameterData {

	private static final Map<FieldInstance, MethodParamInfo> paramDataMap =
		new HashMap<>();

	private final FieldInstance fieldInstance;

	public LazilyGeneratedFieldParameterData(FieldInstance fieldInstance) {
		this.fieldInstance = fieldInstance;
	}

	public static MethodParamInfo getInfo(List<FunctionalMethodType> fmts,
		FieldInstance fieldInstance)
	{
		if (!paramDataMap.containsKey(fieldInstance)) generateFieldParamInfo(fmts,
			fieldInstance);
		return paramDataMap.get(fieldInstance);
	}

	public static synchronized void generateFieldParamInfo(
		List<FunctionalMethodType> fmts, FieldInstance fieldInstance)
	{
		if (paramDataMap.containsKey(fieldInstance)) return;

		Method sam = InterfaceInference.singularAbstractMethod(fieldInstance.field()
			.getType());
		// There is always one output, but we need the number of inputs
		long numIns = sam.getParameterCount();

		// determine the Op inputs/outputs
		Boolean[] paramNullability = getParameterNullability(fieldInstance
			.instance(), fieldInstance.field(), (int) numIns);

		paramDataMap.put(fieldInstance, synthesizedMethodParamInfo(fmts,
			paramNullability));
	}

	private static MethodParamInfo synthesizedMethodParamInfo(
		List<FunctionalMethodType> fmts, Boolean[] paramNullability)
	{
		Map<FunctionalMethodType, String> fmtNames = new HashMap<>(fmts.size());
		Map<FunctionalMethodType, String> fmtDescriptions = new HashMap<>(fmts
			.size());
		Map<FunctionalMethodType, Boolean> fmtNullability = new HashMap<>(fmts
			.size());

		int ins, outs, containers, mutables;
		ins = outs = containers = mutables = 1;
		int nullableIndex = 0;
		for (FunctionalMethodType fmt : fmts) {
			fmtDescriptions.put(fmt, "");
			switch (fmt.itemIO()) {
				case INPUT:
					fmtNames.put(fmt, "input" + ins++);
					fmtNullability.put(fmt, paramNullability[nullableIndex++]);
					break;
				case OUTPUT:
					fmtNames.put(fmt, "output" + outs++);
					break;
				case CONTAINER:
					fmtNames.put(fmt, "container" + containers++);
					break;
				case MUTABLE:
					fmtNames.put(fmt, "mutable" + mutables++);
					break;
				default:
					throw new RuntimeException("Unexpected ItemIO type encountered!");
			}
		}
		return new MethodParamInfo(fmtNames, fmtDescriptions, fmtNullability);
	}

	@Override
	public List<SynthesizedParameterMember<?>> synthesizeMembers(
		List<FunctionalMethodType> fmts)
	{
		Producer<MethodParamInfo> p = //
			() -> LazilyGeneratedFieldParameterData.getInfo(fmts, fieldInstance);

		return fmts.stream() //
			.map(fmt -> new SynthesizedParameterMember<>(fmt, p)) //
			.collect(Collectors.toList());
	}

	// Helper methods
	private static Boolean[] getParameterNullability(Object instance, Field field,
		int opParams)
	{

		Class<?> fieldClass;
		try {
			fieldClass = field.get(instance).getClass();
		}
		catch (IllegalArgumentException | IllegalAccessException exc) {
			// TODO Auto-generated catch block
			throw new IllegalArgumentException(exc);
		}
		List<Method> fMethodsWithNullables = FunctionalParameters
			.fMethodsWithNullable(fieldClass);
		Class<?> fIface = Ops.findFunctionalInterface(fieldClass);
		List<Method> fIfaceMethodsWithNullables = FunctionalParameters
			.fMethodsWithNullable(fIface);

		if (fMethodsWithNullables.isEmpty() && fIfaceMethodsWithNullables
			.isEmpty())
		{
			return FunctionalParameters.generateAllRequiredArray(opParams);
		}
		if (!fMethodsWithNullables.isEmpty() && !fIfaceMethodsWithNullables
			.isEmpty())
		{
			List<Method> nullables = new ArrayList<>(fMethodsWithNullables);
			nullables.addAll(fIfaceMethodsWithNullables);
			throw new NullablesOnMultipleMethodsException(field, nullables);
		}
		if (fMethodsWithNullables.isEmpty()) {
			return FunctionalParameters.findParameterNullability(
				fIfaceMethodsWithNullables.get(0));
		}
		if (fIfaceMethodsWithNullables.isEmpty()) {
			return FunctionalParameters.findParameterNullability(fMethodsWithNullables
				.get(0));
		}
		return FunctionalParameters.generateAllRequiredArray(opParams);
	}

}
