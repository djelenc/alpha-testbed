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
 * Builds a GUI for the {@link RandomWithNewcomers} scenario.
 * 
 * @author David
 * 
 */
public class RandomWithNewcomersGUI extends RandomGUI {
    private static final long serialVersionUID = 430824808467284540L;

    protected JSpinner chgInterval, newcomersNumber;

    @Override
    public JPanel getContentPanel() {
	final JPanel parent = super.getContentPanel();

	// remove interaction density
	parent.remove(interDens);

	for (Component c : parent.getComponents()) {
	    if (c instanceof JLabel) {
		JLabel lbl = (JLabel) c;

		if (lbl.getText().startsWith("Interaction density")) {
		    parent.remove(lbl);
		}
	    }

	}

	final GridBagConstraints c = new GridBagConstraints();

	int yPosition = parent.getComponentCount() / 2;

	// Change interval
	JLabel lbl = new JLabel("Change interval:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = yPosition;
	parent.add(lbl, c);
	chgInterval = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 50));
	((JSpinner.DefaultEditor) chgInterval.getEditor()).getTextField()
		.setColumns(3);
	chgInterval.setToolTipText("The number of time ticks between changes.");
	c.gridx = 1;
	c.gridy = yPosition++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	parent.add(chgInterval, c);

	// New agents number
	lbl = new JLabel("New agents count:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = yPosition;
	parent.add(lbl, c);
	newcomersNumber = new JSpinner(
		new SpinnerNumberModel(50, 1, 10000, 50));
	((JSpinner.DefaultEditor) newcomersNumber.getEditor()).getTextField()
		.setColumns(3);
	newcomersNumber.setToolTipText(
		"The number of new agents that enter the system.");
	c.gridx = 1;
	c.gridy = yPosition++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	parent.add(newcomersNumber, c);

	return parent;
    }

    @Override
    public Object[] getParameters() {
	Object[] parent = super.getParameters();
	Object[] results = new Object[parent.length + 2];
	System.arraycopy(parent, 0, results, 0, parent.length);

	results[parent.length] = getChangeInterval();
	results[parent.length + 1] = getNewCommersNumber();

	return results;

    }

    protected int getNewCommersNumber() {
	return Integer.parseInt(String.valueOf(newcomersNumber.getValue()));
    }

    protected int getChangeInterval() {
	return Integer.parseInt(String.valueOf(chgInterval.getValue()));
    }

}
