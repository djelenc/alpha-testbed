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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import testbed.common.ClassLoaderUtils;
import testbed.deceptionmodel.Complementary;
import testbed.deceptionmodel.ExaggerationModel;
import testbed.deceptionmodel.NegativeExaggeration;
import testbed.deceptionmodel.PositiveExaggeration;
import testbed.deceptionmodel.Truthful;
import testbed.gui.WizardPanelDescriptor;
import testbed.interfaces.DeceptionModel;
import testbed.interfaces.ParametersPanel;

/**
 * Builds a GUI for the {@link Random} scenario.
 * 
 * @author David
 * 
 */
public class RandomGUI extends JPanel implements ParametersPanel {
    protected static final long serialVersionUID = -155882645839798087L;

    protected Map<DeceptionModel, JSpinner> dmsPMF;
    protected JSpinner numOfAgents, sdExperiences, sdOpinions, posExCoef,
	    negExCoef, interDens;

    protected ClassLoader cl;
    protected Observer observer;

    @Override
    public void initialize(Observer o, Object... params) {
	this.observer = o;
	this.cl = (ClassLoader) params[0];
	this.dmsPMF = new HashMap<DeceptionModel, JSpinner>();

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
	return new Object[] { getNumberOfAgents(), getSdExperiences(),
		getSdOpinions(), getDeceptionModelsPMF(), getPosExCoef(),
		getNegExCoef(), getInteractionDensity() };
    }

    protected int getNumberOfAgents() {
	return Integer.parseInt(String.valueOf(numOfAgents.getValue()));
    }

    protected double getSdExperiences() {
	return Double.parseDouble(String.valueOf(sdExperiences.getValue()));
    }

    protected double getSdOpinions() {
	return Double.parseDouble(String.valueOf(sdOpinions.getValue()));
    }

    protected double getPosExCoef() {
	return Double.parseDouble(String.valueOf(posExCoef.getValue()));
    }

    protected double getNegExCoef() {
	return Double.parseDouble(String.valueOf(negExCoef.getValue()));
    }

    protected double getInteractionDensity() {
	return Double.parseDouble(String.valueOf(interDens.getValue()));
    }

    /**
     * Creates the panel and the elements and adds elements to the panel.
     * 
     * @return The panel with created elements
     */
    protected JPanel getContentPanel() {
	final JPanel panel = new JPanel();

	panel.setLayout(new GridBagLayout());

	GridBagConstraints c = new GridBagConstraints();

	int i = 2;
	JLabel lbl;
	c.gridwidth = 1;

	// this evaluates input whenever a value changes
	final ChangeListener listener = new ChangeListener() {
	    @Override
	    public void stateChanged(ChangeEvent e) {
		validateParameters();
	    }
	};

	// Number of agents
	lbl = new JLabel("Number of agents:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = i;
	panel.add(lbl, c);
	numOfAgents = new JSpinner(new SpinnerNumberModel(100, 1, 300, 1));
	((JSpinner.DefaultEditor) numOfAgents.getEditor()).getTextField()
		.setColumns(3);
	numOfAgents.setToolTipText(
		"The number of agents must be between 1 and 300.");
	c.gridx = 1;
	c.gridy = i++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(numOfAgents, c);

	// Standard deviation of experiences
	lbl = new JLabel("Experiences deviation:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = i;
	panel.add(lbl, c);
	sdExperiences = new JSpinner(new SpinnerNumberModel(0.10, 0, 1, 0.05));
	((JSpinner.DefaultEditor) sdExperiences.getEditor()).getTextField()
		.setColumns(3);
	sdExperiences
		.setToolTipText("The standard deviation of the truncated normal"
			+ " distribution that generates interaction outcomes. "
			+ "Valid nubers range between 0 and 1.");
	c.gridx = 1;
	c.gridy = i++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(sdExperiences, c);

	// Standard deviation of opinions
	lbl = new JLabel("Opinions deviation:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = i;
	panel.add(lbl, c);
	sdOpinions = new JSpinner(new SpinnerNumberModel(0.05, 0, 1, 0.05));
	((JSpinner.DefaultEditor) sdOpinions.getEditor()).getTextField()
		.setColumns(3);
	sdOpinions
		.setToolTipText("The standard deviation of the truncated normal"
			+ " distribution that generates opinions.");
	c.gridx = 1;
	c.gridy = i++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(sdOpinions, c);

	JSpinner tf = null;

	// Distribution of deception models
	for (DeceptionModel dm : ClassLoaderUtils.lookUp(DeceptionModel.class,
		cl)) {
	    lbl = new JLabel(dm + ":  ");
	    c.fill = GridBagConstraints.NONE;
	    c.anchor = GridBagConstraints.LINE_END;
	    c.gridx = 0;
	    c.gridy = i;
	    panel.add(lbl, c);

	    tf = new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.05));
	    ((JSpinner.DefaultEditor) tf.getEditor()).getTextField()
		    .setColumns(3);
	    tf.setToolTipText("The percentage of " + dm + " deception model.");
	    tf.addChangeListener(listener);

	    if (dm instanceof Truthful) {
		tf.setValue(0.1);
	    } else if (dm instanceof Complementary) {
		tf.setValue(0.9);
	    }

	    c.gridx = 1;
	    c.gridy = i;
	    c.fill = GridBagConstraints.NONE;
	    c.anchor = GridBagConstraints.LINE_START;
	    panel.add(tf, c);

	    if (dm instanceof ExaggerationModel) {
		final JSpinner exagg;

		lbl = new JLabel("  Coefficient:  ");
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 2;
		c.gridy = i;
		panel.add(lbl, c);

		exagg = new JSpinner(new SpinnerNumberModel(0.25, 0, 1, 0.05));
		((JSpinner.DefaultEditor) exagg.getEditor()).getTextField()
			.setColumns(3);
		exagg.setToolTipText(
			"The coefficient for the " + dm + " deception model.");
		c.gridx = 3;
		c.gridy = i;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.LINE_START;
		panel.add(exagg, c);

		if (dm instanceof PositiveExaggeration) {
		    posExCoef = exagg;
		} else if (dm instanceof NegativeExaggeration) {
		    negExCoef = exagg;
		} else {
		    throw new IllegalArgumentException(
			    "Unknown exaggeration deception model: " + dm);
		}
	    }

	    i++;

	    dmsPMF.put(dm, tf);
	}

	// Interaction density
	lbl = new JLabel("Interaction density:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = i;
	panel.add(lbl, c);
	interDens = new JSpinner(new SpinnerNumberModel(0.10, 0, 1, 0.05));
	((JSpinner.DefaultEditor) interDens.getEditor()).getTextField()
		.setColumns(3);
	interDens.setToolTipText(
		"The proportion of agents with agent Alpha interacts.");
	c.gridx = 1;
	c.gridy = i++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(interDens, c);

	return panel;
    }

    /**
     * Returns a map that represents the distribution of deception models.
     * 
     * @return
     */
    protected Map<DeceptionModel, Double> getDeceptionModelsPMF() {
	Map<DeceptionModel, Double> pmf = new HashMap<DeceptionModel, Double>();

	double val = 0;

	for (Entry<DeceptionModel, JSpinner> t : dmsPMF.entrySet()) {
	    val = Double.parseDouble(String.valueOf(t.getValue().getValue()));

	    if (val > 0)
		pmf.put(t.getKey(), val);
	}

	return pmf;
    }

    /**
     * Evaluates the scenario parameters.
     * 
     * <p>
     * This method invokes the observer's update method. (Observer should be set
     * to a {@link WizardPanelDescriptor} instance.)
     * 
     * <p>
     * This method checks if the sum of the probabilities for deception models
     * adds up to one. If the sum adds up to one, the method invokes the update
     * method with the second argument set to true, otherwise the second
     * argument is set to false. (The first argument is always ignored, while
     * the seconds tells the validity of the input.)
     */
    public void validateParameters() {
	double sum = 0;
	boolean valid = true;

	try {
	    for (Entry<DeceptionModel, JSpinner> t : dmsPMF.entrySet())
		sum += Double
			.parseDouble(String.valueOf(t.getValue().getValue()));
	} catch (Exception e) {
	    observer.update(null, false);
	    return;
	}

	valid = valid && Math.abs(1d - sum) < 0.001;

	observer.update(null, valid);
    }
}
