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

import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class TransitiveWithChangesGUI extends TransitiveGUI {
    private static final long serialVersionUID = -1558821473401798087L;

    protected JSpinner chgDens, chgInterval;

    @Override
    public Object[] getParameters() {
	Object[] parent = super.getParameters();
	Object[] results = new Object[parent.length + 2];
	System.arraycopy(parent, 0, results, 0, parent.length);

	results[parent.length] = getChangeDensity();
	results[parent.length + 1] = getChangeInterval();

	return results;
    }

    protected int getChangeInterval() {
	return Integer.parseInt(String.valueOf(chgInterval.getValue()));
    }

    protected double getChangeDensity() {
	return Double.parseDouble(String.valueOf(chgDens.getValue()));
    }

    @Override
    protected JPanel getContentPanel() {
	final JPanel panel = super.getContentPanel();
	final GridBagConstraints c = new GridBagConstraints();
	int yPosition = panel.getComponentCount() / 2;

	// Change density
	JLabel lbl = new JLabel("Change density:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = yPosition;
	panel.add(lbl, c);
	chgDens = new JSpinner(new SpinnerNumberModel(0.01, 0, 1, 0.05));
	((JSpinner.DefaultEditor) chgDens.getEditor()).getTextField()
		.setColumns(3);
	chgDens.setToolTipText("How many agents are affected by the change in the system?");
	c.gridx = 1;
	c.gridy = yPosition++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(chgDens, c);

	// Change interval
	lbl = new JLabel("Change interval:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = yPosition;
	panel.add(lbl, c);
	chgInterval = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 50));
	((JSpinner.DefaultEditor) chgInterval.getEditor()).getTextField()
		.setColumns(3);
	chgInterval.setToolTipText("The number of time ticks between changes.");
	c.gridx = 1;
	c.gridy = yPosition++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(chgInterval, c);

	return panel;
    }
}
