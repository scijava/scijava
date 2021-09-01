
package org.scijava.ops.engine.struct;

import java.lang.reflect.Type;

import org.scijava.function.Producer;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;

/**
 * {@link Member} synthesized using constructor arguments
 *
 * @author Gabriel Selzer
 * @param <T>
 */
public class SynthesizedParameterMember<T> implements Member<T> {

	/** Type, or a subtype thereof, which houses the field. */
	private final Type itemType;

	/** Producer able to generate the parameter name */
	private final Producer<String> nameGenerator;

	/** Name of the parameter */
	private String name = null;

	/** Producer able to generate the parameter descriptor */
	private final Producer<String> descriptionGenerator;

	private String description = null;

	/** IO status of the parameter */
	private final ItemIO itemIO;

	public SynthesizedParameterMember(final Type itemType, final Producer<MethodParamInfo> nameInfo, final ItemIO ioType,
		final int paramNo)
	{
		this.itemType = itemType;
		this.nameGenerator = () -> nameInfo.create().name(paramNo);
		this.descriptionGenerator = () -> nameInfo.create().description(paramNo);
		this.itemIO = ioType;
	}

	public SynthesizedParameterMember(final Type itemType, final String name,
		final String description, final ItemIO ioType)
	{
		this.itemType = itemType;
		this.name = name;
		this.nameGenerator = () -> name;
		this.description = description;
		this.descriptionGenerator = () -> description;
		this.itemIO = ioType;
	}

	// -- Member methods --

	@Override
	public String getKey() {
		if (name == null) generateName();
		return name;
	}

	private synchronized void generateName() {
		if (name != null) return;
		name = nameGenerator.create();
	}

	@Override
	public String getDescription() {
		if (description == null) generateDescription();
		return description;
	}

	private synchronized void generateDescription() {
		if (description != null) return;
		description = descriptionGenerator.create();
	}

	@Override
	public Type getType() {
		return itemType;
	}

	@Override
	public ItemIO getIOType() {
		return itemIO;
	}

	@Override
	public boolean isStruct() {
		return false;
	}
}
