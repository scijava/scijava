
package org.scijava.ops.engine.struct;

import java.lang.reflect.Type;

import org.scijava.function.Producer;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;

/**
 * {@link Member} synthesized using constructor arguments
 *
 * @author Gabriel Selzer
 * @param <T>
 */
public class SynthesizedParameterMember<T> implements Member<T> {

	/** {@link FunctionalMethodType} describing this Member */
	private final FunctionalMethodType fmt;

	/** Producer able to generate the parameter name */
	private final Producer<String> nameGenerator;

	/** Name of the parameter */
	private String name = null;

	/** Producer able to generate the parameter descriptor */
	private final Producer<String> descriptionGenerator;

	private String description = null;

	public SynthesizedParameterMember(final FunctionalMethodType fmt, final Producer<MethodParamInfo> synthesizerGenerator)
	{
		this.fmt = fmt;
		this.nameGenerator = () -> synthesizerGenerator.create().name(fmt);
		this.descriptionGenerator = () -> synthesizerGenerator.create().description(fmt);
	}

	// -- Member methods --

	@Override
	public String getKey() {
		if (name == null) generateName();
		return name;
	}

	private synchronized void generateName() {
		if (name != null) return;
		String temp = nameGenerator.create();
		name = temp;
	}

	@Override
	public String getDescription() {
		if (description == null) generateDescription();
		return description;
	}

	private synchronized void generateDescription() {
		if (description != null) return;
		String temp = descriptionGenerator.create();
		description = temp;
	}

	@Override
	public Type getType() {
		return fmt.type();
	}

	@Override
	public ItemIO getIOType() {
		return fmt.itemIO();
	}

	@Override
	public boolean isStruct() {
		return false;
	}
}
