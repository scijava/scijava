
package org.scijava.ops.struct;

import java.lang.reflect.Type;

import org.scijava.param.ParameterMember;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;

/**
 * {@link Member} synthesized using constructor arguments
 *
 * @author Gabriel Selzer
 * @param <T>
 */
public class SynthesizedParameterMember<T> implements ParameterMember<T> {

	/** Type, or a subtype thereof, which houses the field. */
	private final Type itemType;

	/** Name of the parameter */
	private final String name;

	/** Description of the parameter */
	private final String description;

	/** IO status of the parameter */
	private final ItemIO itemIO;

	public SynthesizedParameterMember(final Type itemType,
		final String name, final String description, final ItemIO ioType)
	{
		this.itemType = itemType;
		this.name = name;
		this.description = description;
		this.itemIO = ioType;
	}

	// -- Member methods --

	@Override
	public String getKey() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
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
