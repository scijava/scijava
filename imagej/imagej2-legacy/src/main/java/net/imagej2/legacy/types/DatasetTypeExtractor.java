
package net.imagej2.legacy.types;

import java.lang.reflect.Type;

import org.scijava.priority.Priority;
import org.scijava.types.TypeExtractor;
import org.scijava.types.TypeReifier;

import net.imagej.Dataset;

public class DatasetTypeExtractor implements TypeExtractor {

	@Override
	public double getPriority() {
		return Priority.HIGH;
	}

	@Override
	public boolean canReify(TypeReifier r, Class<?> object) {
		return Dataset.class.isAssignableFrom(object);
	}

	@Override public Type reify(TypeReifier r, Object object) {
		if (!(object instanceof Dataset)) throw new IllegalArgumentException(object + " cannot be reified because it is not a Dataset!");
		return r.reify(((Dataset) object).getImgPlus());
	}
}
