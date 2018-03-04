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
package testbed.trustmodel.qad;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Observer;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import testbed.gui.WizardPanelDescriptor;
import testbed.interfaces.ParametersPanel;

public class QADGUI extends JPanel implements ParametersPanel {

    private static final long serialVersionUID = -1558821473401798087L;

    @SuppressWarnings("rawtypes")
    private JComboBox operator;

    private Observer observer;

    @Override
    public void initialize(Observer o, Object... params) {
	this.observer = o;

	JPanel contentPanel = getContentPanel();
	contentPanel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
	setLayout(new BorderLayout());
	add(contentPanel, BorderLayout.CENTER);
	addComponentListener(new ComponentListener() {

	    @Override
	    public void componentShown(ComponentEvent e) {
		validateParameters();
	    }

	    @Override
	    public void componentResized(ComponentEvent e) {
		validateParameters();
	    }

	    @Override
	    public void componentMoved(ComponentEvent e) {
		validateParameters();
	    }

	    @Override
	    public void componentHidden(ComponentEvent e) {
		validateParameters();
	    }
	});
    }

    @Override
    public Component getComponent() {
	return this;
    }

    @Override
    public Object[] getParameters() {
	return new Object[] { getOperator() };
    }

    private Operator getOperator() {
	return (Operator) operator.getSelectedItem();
    }

    /**
     * Creates the panel and the elements and adds elements to the panel.
     * 
     * @return The panel with created elements
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private JPanel getContentPanel() {
	JPanel panel = new JPanel();

	panel.setLayout(new GridBagLayout());

	GridBagConstraints c = new GridBagConstraints();

	int i = 2;
	JLabel lbl;
	c.gridwidth = 1;

	// Operator
	lbl = new JLabel("Operator:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = i;
	panel.add(lbl, c);
	operator = new JComboBox();

	for (Operator o : Operator.values())
	    operator.addItem(o);

	operator.setToolTipText("Select trust operator.");
	c.gridx = 1;
	c.gridy = i++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(operator, c);

	return panel;
    }

    /**
     * Evaluates the deception model distribution parameters.
     * 
     * <p>
     * This method invokes the observer's update method. (Observer should be set
     * to a {@link WizardPanelDescriptor} instance.)
     */
    private void validateParameters() {
	boolean valid = true;

	observer.update(null, valid);
    }

}
