package org.scijava.widget;

import org.scijava.struct.MemberInstance;

public abstract class AbstractWidget implements Widget {

	private final MemberInstance<?> model;

	public AbstractWidget(final MemberInstance<?> model) {
		this.model = model;
	}

	@Override
	public MemberInstance<?> model() {
		return model;
	}
}
