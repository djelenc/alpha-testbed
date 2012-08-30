package testbed.gui;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.aspectj.bridge.IMessage;

import testbed.interfaces.IDeceptionModel;
import testbed.interfaces.IMetric;
import testbed.interfaces.IScenario;
import testbed.interfaces.ITrustModel;

/**
 * Renders the value of ComboBoxes
 * 
 * @author David
 * 
 */
class CustomComboBoxRenderer extends BasicComboBoxRenderer {
    private static final long serialVersionUID = 2824150777814818533L;

    public Component getListCellRendererComponent(JList list, Object value,
	    int index, boolean isSelected, boolean cellHasFocus) {
	super.getListCellRendererComponent(list, value, index, isSelected,
		cellHasFocus);

	if (value instanceof ITrustModel<?>) {
	    ITrustModel<?> tm = (ITrustModel<?>) value;
	    setText(tm.getName());
	} else if (value instanceof IMessage) {
	    IMetric m = (IMetric) value;
	    setText(m.getName());
	} else if (value instanceof IScenario) {
	    IScenario s = (IScenario) value;
	    setText(s.getName());
	} else if (value instanceof IDeceptionModel) {
	    IDeceptionModel dm = (IDeceptionModel) value;
	    setText(dm.getName());
	}

	return this;
    }
}