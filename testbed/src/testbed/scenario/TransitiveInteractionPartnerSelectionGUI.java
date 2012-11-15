package testbed.scenario;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class TransitiveInteractionPartnerSelectionGUI extends TransitiveGUI {
    private static final long serialVersionUID = 7248570829649169366L;

    @Override
    protected JPanel getContentPanel() {
	JPanel parent = super.getContentPanel();

	parent.remove(interDens);

	for (Component c : parent.getComponents()) {
	    if (c instanceof JLabel) {
		JLabel lbl = (JLabel) c;

		if (lbl.getText().startsWith("Interaction density")) {
		    parent.remove(lbl);
		}
	    }

	}

	return parent;
    }
}
