package org.scijava.nwidget;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.scijava.struct.Member;
import org.scijava.struct.MemberInstance;
import org.scijava.struct.StructInstance;
import org.scijava.util.ClassUtils;

public class NSwingNumberWidgetFactory implements NSwingWidgetFactory {

	@Override
	public boolean supports(final Member<?> member) {
		return ClassUtils.isNumber(member.getRawType());
	}

	@Override
	public NWidget create(MemberInstance<?> memberInstance) {
		return new Widget(memberInstance);
	}

	private class Widget implements NSwingWidget {

		public Widget(final MemberInstance<?> memberInstance) {
			super(memberInstance);
		}

		@Override
		public JPanel getComponent() {
			// TODO - lazy/null caching
			final JPanel panel = new JPanel();
			final MigLayout layout =
				new MigLayout("fillx,ins 3 0 3 0", "[fill,grow|pref]");
			panel.setLayout(layout);

			/*
			final Number min = model.getMin();
			final Number max = model.getMax();
			final Number softMin = model.getSoftMin();
			final Number softMax = model.getSoftMax();
			final Number stepSize = model.getStepSize();

			// add optional widgets, if specified
			if (model.isStyle(NumberWidget.SCROLL_BAR_STYLE)) {
				int smx = softMax.intValue();
				if (smx < Integer.MAX_VALUE) smx++;
				scrollBar =
					new JScrollBar(Adjustable.HORIZONTAL, softMin.intValue(), 1, softMin
						.intValue(), smx);
				scrollBar.setUnitIncrement(stepSize.intValue());
				setToolTip(scrollBar);
				getComponent().add(scrollBar);
				scrollBar.addAdjustmentListener(this);
			}
			else if (model.isStyle(NumberWidget.SLIDER_STYLE)) {
				slider =
					new JSlider(softMin.intValue(), softMax.intValue(), softMin.intValue());
				slider.setMajorTickSpacing((softMax.intValue() - softMin.intValue()) / 4);
				slider.setMinorTickSpacing(stepSize.intValue());
				slider.setPaintLabels(true);
				slider.setPaintTicks(true);
				setToolTip(slider);
				getComponent().add(slider);
				slider.addChangeListener(this);
			}

			// add spinner widget
			final Class<?> type = model.getItem().getType();
			final Number value = (Number) model.getValue();
			final SpinnerNumberModel spinnerModel =
				new SpinnerNumberModelFactory().createModel(value, min, max, stepSize);
			spinner = new JSpinner(spinnerModel);
			fixSpinner(type);
			setToolTip(spinner);
			getComponent().add(spinner);
			limitWidth(200);
			spinner.addChangeListener(this);

			refreshWidget();
			syncSliders();
			*/
			return panel;
		}
	}
}