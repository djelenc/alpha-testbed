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
package testbed.trustmodel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import testbed.gui.WizardPanelDescriptor;
import testbed.interfaces.ParametersPanel;

public class TravosGUI extends JPanel implements ParametersPanel {

    private static final long serialVersionUID = -1558821473401798087L;

    private JSpinner satisfactoryThreshold, opinionSampleNumber,
	    opinionSampleSD, confidenceThreshold, error;

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
	return new Object[] { getSatisfactoryThreshold(),
		getOpinionSampleNumber(), getOpinionSampleSD(),
		getConfidenceThreshold(), getError() };
    }

    protected double getError() {
	return Double.parseDouble(String.valueOf(error.getValue()));
    }

    protected double getConfidenceThreshold() {
	return Double
		.parseDouble(String.valueOf(confidenceThreshold.getValue()));
    }

    protected double getSatisfactoryThreshold() {
	return Double.parseDouble(String.valueOf(satisfactoryThreshold
		.getValue()));
    }

    protected int getOpinionSampleNumber() {
	return Integer.parseInt(String.valueOf(opinionSampleNumber.getValue()));
    }

    protected double getOpinionSampleSD() {
	return Double.parseDouble(String.valueOf(opinionSampleSD.getValue()));
    }

    /**
     * Creates the panel and the elements and adds elements to the panel.
     * 
     * @return The panel with created elements
     */
    private JPanel getContentPanel() {
	JPanel panel = new JPanel();

	panel.setLayout(new GridBagLayout());

	GridBagConstraints c = new GridBagConstraints();

	int i = 2;
	JLabel lbl;
	c.gridwidth = 1;

	// satisfactory threshold
	lbl = new JLabel("Satisfactory threshold:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = i;
	panel.add(lbl, c);
	satisfactoryThreshold = new JSpinner(new SpinnerNumberModel(0.5, 0, 1,
		0.25));
	((JSpinner.DefaultEditor) satisfactoryThreshold.getEditor())
		.getTextField().setColumns(3);
	satisfactoryThreshold
		.setToolTipText("A threshold that determines when the interaction outcome becomes a satisfactory.");
	c.gridx = 1;
	c.gridy = i++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(satisfactoryThreshold, c);

	// opinion sample number
	lbl = new JLabel("Number of opinion samples:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = i;
	panel.add(lbl, c);
	opinionSampleNumber = new JSpinner(new SpinnerNumberModel(10, 1, 50, 1));
	((JSpinner.DefaultEditor) opinionSampleNumber.getEditor())
		.getTextField().setColumns(3);
	opinionSampleNumber
		.setToolTipText("Number of times the obtained opinion will be sampled to obtain an (r, s) tuple.");
	c.gridx = 1;
	c.gridy = i++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(opinionSampleNumber, c);

	// opinion sample standard deviation
	lbl = new JLabel("SD to sample opinions:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = i;
	panel.add(lbl, c);
	opinionSampleSD = new JSpinner(new SpinnerNumberModel(0.1, 0, 1, 0.05));
	((JSpinner.DefaultEditor) opinionSampleSD.getEditor()).getTextField()
		.setColumns(3);
	opinionSampleSD
		.setToolTipText("Standard deviation for sampling opinions.");
	c.gridx = 1;
	c.gridy = i++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(opinionSampleSD, c);

	// confidence threshold
	lbl = new JLabel("Confidence threshold:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = i;
	panel.add(lbl, c);
	confidenceThreshold = new JSpinner(new SpinnerNumberModel(0.95, 0, 1,
		0.05));
	((JSpinner.DefaultEditor) confidenceThreshold.getEditor())
		.getTextField().setColumns(3);
	confidenceThreshold
		.setToolTipText("A threshold that determines the confidence value when the "
			+ "computed trust suffices and opinions are not required.");
	c.gridx = 1;
	c.gridy = i++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(confidenceThreshold, c);

	// error
	lbl = new JLabel("Confidence error:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = i;
	panel.add(lbl, c);
	error = new JSpinner(new SpinnerNumberModel(0.2, 0, 1, 0.05));
	((JSpinner.DefaultEditor) error.getEditor()).getTextField().setColumns(
		3);
	error.setToolTipText("The level of error that an agent is willing "
		+ "to accept when determining the confidence value.");
	c.gridx = 1;
	c.gridy = i++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(error, c);

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
