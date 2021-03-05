package org.scijava.param;

import io.leangen.geantyref.AnnotationFormatException;
import io.leangen.geantyref.TypeFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SynthesizedParameterData implements ParameterData{

	@Override
	public List<Parameter> synthesizeAnnotations(
		List<FunctionalMethodType> fmts)
	{
		List<Parameter> params = new ArrayList<>();
		
		int ins, outs, containers, mutables;
		ins = outs = containers = mutables = 1;
		for (FunctionalMethodType fmt : fmts) {
			Map<String, Object> paramValues = new HashMap<>();
			paramValues.put(Parameter.ITEMIO_FIELD_NAME, fmt.itemIO());
			
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
			
			paramValues.put(Parameter.KEY_FIELD_NAME, key);
			
			try {
				params.add(TypeFactory.annotation(Parameter.class, paramValues));
			} catch (AnnotationFormatException e) {
				throw new RuntimeException("Error during Parameter annotation synthetization. This is "
						+ "most likely an implementation error.", e);
			}
		}
		
		return params;
	}

}
