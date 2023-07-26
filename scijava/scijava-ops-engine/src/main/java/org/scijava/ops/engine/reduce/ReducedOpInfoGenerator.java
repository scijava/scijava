
package org.scijava.ops.engine.reduce;

import org.scijava.common3.validity.ValidityException;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpInfoGenerator;
import org.scijava.struct.Member;

import java.util.*;
import java.util.function.LongFunction;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class ReducedOpInfoGenerator implements OpInfoGenerator {

	public static final List<InfoReducer> infoReducers = ServiceLoader.load(
		InfoReducer.class).stream().map(ServiceLoader.Provider::get).collect(
			Collectors.toList());

	@Override
	public boolean canGenerateFrom(Object o) {
		// We can only generate OpInfos from other OpInfos
		if (!(o instanceof OpInfo)) return false;
		OpInfo info = (OpInfo) o;
		// We only benefit from OpInfos with optional parameters
		boolean allParamsRequired = info.inputs().parallelStream() //
			.allMatch(Member::isRequired); //
		if (allParamsRequired) return false;
		// If we have a InfoReducer, then we can reduce
		return infoReducers //
			.parallelStream() //
			.anyMatch(reducer -> reducer.canReduce(info));
	}

	@Override
	public List<OpInfo> generateInfosFrom(Object o) {
		if (!(o instanceof OpInfo)) return Collections.emptyList();
		return reduce((OpInfo) o);
	}

	private List<OpInfo> reduce(OpInfo info) {
		// Find the correct InfoReducer
		Optional<? extends InfoReducer> optionalReducer = infoReducers //
				.parallelStream() //
				.filter(infoReducer -> infoReducer.canReduce(info)) //
				.findAny();
		if(optionalReducer.isEmpty()) return Collections.emptyList();
		// We can only reduce as many times as we have optional params
		int numReductions = (int) info.struct().members().parallelStream() //
				.filter(m -> !m.isRequired()) //
				.count(); //
		// add a ReducedOpInfo for all possible reductions
		InfoReducer reducer = optionalReducer.get();
		LongFunction<OpInfo> func = l -> reducer.reduce(info, (int) l);
		return LongStream.range(1, numReductions + 1) //
			.mapToObj(i -> {
				try {
					return func.apply(i);
				} catch(ValidityException e) {
					// TODO: Log exception
					return null;
				}
			}) //
			.filter(Objects::nonNull) //
			.collect(Collectors.toList());
	}
	
}
