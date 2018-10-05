package org.scijava.ops.transform;

import java.util.List;

import org.scijava.ops.matcher.OpRef;
import org.scijava.plugin.SingletonService;
import org.scijava.service.SciJavaService;

public interface OpTransformerService extends SciJavaService, SingletonService<OpTransformer> {

	@Override
	default Class<OpTransformer> getPluginType() {
		return OpTransformer.class;
	}
	
	List<OpTransformationInfo> getTansformationsTo (OpRef opRef);
}
