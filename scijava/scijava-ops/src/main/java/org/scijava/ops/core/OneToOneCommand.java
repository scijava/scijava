package org.scijava.ops.core;

import java.util.function.Supplier;

import org.scijava.command.Command;
import org.scijava.param.Parameter;
import org.scijava.struct.ItemIO;

public abstract class OneToOneCommand<I, O> implements Command, Supplier<O> {

	@Parameter
	protected I input;
	
	@Parameter(itemIO = ItemIO.OUTPUT)
	protected O output;
	
	@Override
	public O get() {
		return output;
	}

}
