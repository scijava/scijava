package org.scijava.types.infer;

import org.scijava.common3.Any;
import org.scijava.common3.Types;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * @author Gabriel Selzer
 */
class AnyTypeMapping extends TypeMapping {

    private final Any boundingAny;

    AnyTypeMapping(TypeVariable<?> typeVar, Type any,
                        boolean malleable)
    {
        super(typeVar, any, malleable);
        this.boundingAny = any instanceof Any ? (Any) any : new Any(typeVar.getBounds());
    }

    @Override
    public void refine(Type newType, boolean malleable) {
        super.refine(newType, malleable);
        for(Type t: boundingAny.getLowerBounds()) {
            if (!Types.isAssignable(t, newType)) {
                throw new TypeInferenceException("The new type " + newType + " is not a parent of the Any bound " + t);
            }
        }
        for(Type t: boundingAny.getUpperBounds()) {
            if (!Types.isAssignable(newType, t)) {
                throw new TypeInferenceException("The new type " + newType + " is not a child of the Any bound " + t);
            }
        }
    }
}
