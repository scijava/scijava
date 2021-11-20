
package org.scijava.ops.struct;

import java.util.ArrayList;
import java.util.List;

import org.scijava.struct.FunctionalMethodType;

public class SynthesizedParameterData implements ParameterData {

	@Override
	public List<SynthesizedParameterMember<?>> synthesizeMembers(List<FunctionalMethodType> fmts) {
		List<SynthesizedParameterMember<?>> params = new ArrayList<>();

		int ins, outs, containers, mutables;
		ins = outs = containers = mutables = 1;
		for (FunctionalMethodType fmt : fmts) {
			String key;
			switch (fmt.itemIO()) {
				case INPUT:
					key = "input" + ins++;
					break;
				case OUTPUT:
					key = "output" + outs++;
					break;
				case CONTAINER:
					key = "container" + containers++;
					break;
				case MUTABLE:
					key = "mutable" + mutables++;
					break;
				default:
					throw new RuntimeException("Unexpected ItemIO type encountered!");
			}
			params.add(new SynthesizedParameterMember<>(fmt.type(), key, "", fmt
				.itemIO()));
		}

		return params;
	}

}
