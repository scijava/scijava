
package org.scijava.ops.simplify;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to denote that an Op should not be simplified TODO: Can we think of a
 * better name?
 * 
 * @author Gabriel Selzer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface Unsimplifiable {

}
