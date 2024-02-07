
package org.scijava.ops.flim.types;

import net.imglib2.util.Util;
import org.scijava.ops.flim.FitParams;
import org.scijava.types.SubTypeExtractor;
import org.scijava.types.TypeReifier;

import java.lang.reflect.Type;

public class FitParamsTypeExtractor extends SubTypeExtractor<FitParams<?>> {

	@Override
	public Class<?> baseClass() {
		return FitParams.class;
	}

	@Override
	protected Type[] getTypeParameters(TypeReifier r, FitParams<?> object) {
		Type elementType = r.reify(Util.getTypeFromInterval(object.transMap));
		return new Type[] { elementType };
	}

}
