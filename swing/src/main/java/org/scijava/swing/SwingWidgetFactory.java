/*
 * #%L
 * Swing widgets, and a dialog to harvest inputs with them.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.swing;

import org.scijava.ui3.WidgetFactory;

/**
 * A {@link WidgetFactory} that makes Swing widgets.
 * <p>
 * Contribute one as a plugin:
 * </p>
 *
 * <pre>
 * &#64;Plugin(type = WidgetFactory.class)
 * public class MyWidgetFactory implements SwingWidgetFactory { ... }
 * </pre>
 * <p>
 * The plugin type is {@link WidgetFactory}, not this interface, so that every
 * toolkit's widgets are discovered the same way; a Swing dialog then keeps the
 * ones whose {@link #widgetType()} it can use.
 * </p>
 *
 * @author Curtis Rueden
 */
public interface SwingWidgetFactory extends WidgetFactory<SwingWidget> {

	@Override
	default Class<SwingWidget> widgetType() {
		return SwingWidget.class;
	}
}
