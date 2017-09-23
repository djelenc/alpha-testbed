/*
 * Copyright (c) 2013 David Jelenc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     David Jelenc - initial API and implementation
 */
package testbed.scenario;

import java.awt.Component;
import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Builds a GUI for the {@link RandomMultiService}.
 * 
 * @author David
 * 
 */
public class RandomMultiServiceGUI extends RandomGUI {
    private static final long serialVersionUID = -155882645839798087L;

    protected JSpinner numOfServices;

    @Override
    public JPanel getContentPanel() {
	final JPanel panel = super.getContentPanel();

	// remove interaction density
	panel.remove(interDens);

	for (Component c : panel.getComponents()) {
	    if (c instanceof JLabel) {
		JLabel lbl = (JLabel) c;

		if (lbl.getText().startsWith("Interaction density")) {
		    panel.remove(lbl);
		}
	    }

	}

	final GridBagConstraints c = new GridBagConstraints();
	int yPosition = panel.getComponentCount() / 2;

	// add number of services
	JLabel lbl = new JLabel("Number of services:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = yPosition;
	panel.add(lbl, c);
	numOfServices = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
	((JSpinner.DefaultEditor) numOfServices.getEditor()).getTextField()
		.setColumns(3);
	numOfServices.setToolTipText(
		"The number of services must be between 1 and 10.");
	c.gridx = 1;
	c.gridy = yPosition++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(numOfServices, c);

	return panel;
    }

    @Override
    public Object[] getParameters() {
	return new Object[] { getNumberOfAgents(), getNumberOfServices(),
		getSdExperiences(), getSdOpinions(), getDeceptionModelsPMF(),
		getPosExCoef(), getNegExCoef() };
    }

    protected int getNumberOfServices() {
	return Integer.parseInt(String.valueOf(numOfServices.getValue()));
    }

}
