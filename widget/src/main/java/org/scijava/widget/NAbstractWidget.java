package org.scijava.nwidget;

import org.scijava.struct.MemberInstance;

public abstract class NAbstractWidget implements NWidget {

	private final MemberInstance<?> model;

	public NAbstractWidget(final MemberInstance<?> model) {
		this.model = model;
	}

	@Override
	public MemberInstance<?> model() {
		return model;
	}
}
