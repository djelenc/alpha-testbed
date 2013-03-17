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

public class EigenTrustGUI extends JPanel implements ParametersPanel {

    private static final long serialVersionUID = -1558821473401798087L;

    protected JSpinner weight, satisfactoryThreshold, opinionSampleNumber,
	    opinionSampleSD;

    protected Observer observer;

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
	return new Object[] { getWeight(), getSatisfactoryThreshold(),
		getOpinionSampleNumber(), getOpinionSampleSD() };
    }

    protected double getOpinionSampleSD() {
	return Double.parseDouble(String.valueOf(opinionSampleSD.getValue()));
    }

    protected int getOpinionSampleNumber() {
	return Integer.parseInt(String.valueOf(opinionSampleNumber.getValue()));
    }

    protected double getSatisfactoryThreshold() {
	return Double.parseDouble(String.valueOf(satisfactoryThreshold
		.getValue()));
    }

    protected double getWeight() {
	return Double.parseDouble(String.valueOf(weight.getValue()));
    }

    /**
     * Creates the panel and the elements and adds elements to the panel.
     * 
     * @return The panel with created elements
     */
    protected JPanel getContentPanel() {
	JPanel panel = new JPanel();

	panel.setLayout(new GridBagLayout());

	GridBagConstraints c = new GridBagConstraints();

	int i = 2;
	JLabel lbl;
	c.gridwidth = 1;

	// Weight
	lbl = new JLabel("Pre-trust peers weight:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = i;
	panel.add(lbl, c);
	weight = new JSpinner(new SpinnerNumberModel(0.5, 0, 1, 0.1));
	((JSpinner.DefaultEditor) weight.getEditor()).getTextField()
		.setColumns(3);
	weight.setToolTipText("The weight assigned to pre-trusted peers. "
		+ "Must be between 0 and 1, inclusively.");
	c.gridx = 1;
	c.gridy = i++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(weight, c);

	// Satisfactory threshold
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
		.setToolTipText("Number of times the obtained opinion will be sampled to obtain a (pos, neg) tuple.");
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
	opinionSampleSD = new JSpinner(new SpinnerNumberModel(0.10, 0, 1, 0.05));
	((JSpinner.DefaultEditor) opinionSampleSD.getEditor()).getTextField()
		.setColumns(3);
	opinionSampleSD
		.setToolTipText("Standard deviation for sampling opinions.");
	c.gridx = 1;
	c.gridy = i++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(opinionSampleSD, c);

	return panel;
    }

    /**
     * Evaluates the deception model distribution parameters.
     * 
     * <p>
     * This method invokes the observer's update method. (Observer should be set
     * to a {@link WizardPanelDescriptor} instance.)
     */
    protected void validateParameters() {
	observer.update(null, true);
    }

}
