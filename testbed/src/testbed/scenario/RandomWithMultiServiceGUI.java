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
import testbed.deceptionmodel.Silent;
import testbed.deceptionmodel.Truthful;
import testbed.gui.WizardPanelDescriptor;
import testbed.interfaces.IDeceptionModel;
import testbed.interfaces.IParametersPanel;

/**
 * Builds a GUI for the {@link Random}.
 * 
 * <p>
 * The method {@link RandomWithMultiServiceGUI#getParameters()} returns an array
 * of objects that represent parameters with the following semantics ():
 * <ul>
 * <li>0: (int) number of agents
 * <li>1: (double) standard deviation for generating experiences
 * <li>2: (double) standard deviation for generation opinions
 * <li>3: (Map<IDeceptionModel, Double>) distribution of deception models
 * <li>4: (double) positive exaggeration coefficient
 * <li>5: (double) negative exaggeration coefficient
 * </ul>
 * 
 * @author David
 * 
 */
public class RandomWithMultiServiceGUI extends JPanel implements
	IParametersPanel {
    private static final long serialVersionUID = -155882645839798087L;

    private Map<IDeceptionModel, JSpinner> dmsPMF;
    private JSpinner numOfAgents, numOfServices, sdExperiences, sdOpinions,
	    posExCoef, negExCoef;

    private ClassLoader cl;
    private Observer observer;

    @Override
    public void initialize(Observer o, Object... params) {
	this.observer = o;
	this.cl = (ClassLoader) params[0];
	this.dmsPMF = new HashMap<IDeceptionModel, JSpinner>();

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
	return new Object[] { getNumberOfAgents(), getNumberOfServices(),
		getSdExperiences(), getSdOpinions(), getDeceptionModelsPMF(),
		getPosExCoef(), getNegExCoef() };
    }

    private int getNumberOfAgents() {
	return Integer.parseInt(String.valueOf(numOfAgents.getValue()));
    }

    private int getNumberOfServices() {
	return Integer.parseInt(String.valueOf(numOfServices.getValue()));
    }

    private double getSdExperiences() {
	return Double.parseDouble(String.valueOf(sdExperiences.getValue()));
    }

    private double getSdOpinions() {
	return Double.parseDouble(String.valueOf(sdOpinions.getValue()));
    }

    private double getPosExCoef() {
	return Double.parseDouble(String.valueOf(posExCoef.getValue()));
    }

    private double getNegExCoef() {
	return Double.parseDouble(String.valueOf(negExCoef.getValue()));
    }

    /**
     * Creates the panel and the elements and adds elements to the panel.
     * 
     * @return The panel with created elements
     */
    private JPanel getContentPanel() {
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
	numOfAgents
		.setToolTipText("The number of agents must be between 1 and 300.");
	c.gridx = 1;
	c.gridy = i++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(numOfAgents, c);

	// Number of services
	lbl = new JLabel("Number of services:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = i;
	panel.add(lbl, c);
	numOfServices = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
	((JSpinner.DefaultEditor) numOfServices.getEditor()).getTextField()
		.setColumns(3);
	numOfServices
		.setToolTipText("The number of services must be between 1 and 10.");
	c.gridx = 1;
	c.gridy = i++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(numOfServices, c);

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
	for (IDeceptionModel dm : ClassLoaderUtils.lookUp(
		IDeceptionModel.class, cl)) {
	    lbl = new JLabel(dm.getName() + ":  ");
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

	    if (dm instanceof Truthful || dm instanceof Complementary) {
		tf.setValue(0.1);
	    } else if (dm instanceof Silent) {
		tf.setValue(0.8);
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
		exagg.setToolTipText("The coefficient for the " + dm
			+ " deception model.");
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
			    "Unknown exaggeration deception model: "
				    + dm.getName());
		}
	    }

	    i++;

	    dmsPMF.put(dm, tf);
	}

	return panel;
    }

    /**
     * Returns a map that represents the distribution of deception models.
     * 
     * @return
     */
    private Map<IDeceptionModel, Double> getDeceptionModelsPMF() {
	Map<IDeceptionModel, Double> pmf = new HashMap<IDeceptionModel, Double>();

	double val = 0;

	for (Entry<IDeceptionModel, JSpinner> t : dmsPMF.entrySet()) {
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
	    for (Entry<IDeceptionModel, JSpinner> t : dmsPMF.entrySet())
		sum += Double.parseDouble(String.valueOf(t.getValue()
			.getValue()));
	} catch (Exception e) {
	    observer.update(null, false);
	    return;
	}

	valid = valid && Math.abs(1d - sum) < 0.001;

	observer.update(null, valid);
    }
}
