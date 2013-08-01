/*******************************************************************************
 * Copyright (c) 2013 David Jelenc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     David Jelenc - initial API and implementation
 ******************************************************************************/
package testbed.scenario;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class RandomInteractionPartnerSelectionGUI extends RandomGUI {
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
