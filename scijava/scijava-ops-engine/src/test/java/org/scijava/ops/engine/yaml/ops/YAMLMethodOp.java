
package org.scijava.ops.engine.yaml.ops;

import java.lang.reflect.Method;

/**
 * A static {@link Method}, exposed to Ops via YAML
 * 
 * @author Gabriel Selzer
 */
public class YAMLMethodOp {

	public static Double subtract(Double aDouble, Double aDouble2) {
		return aDouble - aDouble2;
	}

}
