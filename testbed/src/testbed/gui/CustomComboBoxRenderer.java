package testbed.gui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

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
class CustomComboBoxRenderer extends JLabel implements ListCellRenderer {
    private static final long serialVersionUID = 2824150777814818533L;

    private ListCellRenderer parent;

    public CustomComboBoxRenderer(ListCellRenderer parent) {
	this.parent = parent;
    }

    public Component getListCellRendererComponent(JList list, Object value,
	    int index, boolean isSelected, boolean cellHasFocus) {

	parent.getListCellRendererComponent(list, value, index, isSelected,
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

	return (Component) parent;
    }

    @Override
    public void setText(String text) {
	if (parent != null)
	    ((JLabel) parent).setText(text);
    }

}