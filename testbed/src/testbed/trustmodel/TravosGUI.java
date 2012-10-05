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

    private JSpinner expMultiplier, opMultiplier, threshold, error;

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
	return new Object[] { getExperienceMultiplier(),
		getOpinionMultiplier(), getThreshold(), getError() };
    }

    private double getError() {
	return Double.parseDouble(String.valueOf(error.getValue()));
    }

    private double getThreshold() {
	return Double.parseDouble(String.valueOf(threshold.getValue()));
    }

    private double getExperienceMultiplier() {
	return Double.parseDouble(String.valueOf(expMultiplier.getValue()));
    }

    private double getOpinionMultiplier() {
	return Double.parseDouble(String.valueOf(opMultiplier.getValue()));
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

	// experience multiplier
	lbl = new JLabel("Experience multiplier:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = i;
	panel.add(lbl, c);
	expMultiplier = new JSpinner(new SpinnerNumberModel(10, 1, 50, 1));
	((JSpinner.DefaultEditor) expMultiplier.getEditor()).getTextField()
		.setColumns(3);
	expMultiplier
		.setToolTipText("Multiplier that converts a contionouos interaction outcome from [0, 1] "
			+ "to a discreete value.");
	c.gridx = 1;
	c.gridy = i++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(expMultiplier, c);

	// opinion multiplier
	lbl = new JLabel("Opinion multiplier:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = i;
	panel.add(lbl, c);
	opMultiplier = new JSpinner(new SpinnerNumberModel(10, 1, 50, 1));
	((JSpinner.DefaultEditor) opMultiplier.getEditor()).getTextField()
		.setColumns(3);
	opMultiplier
		.setToolTipText("Multiplier that converts a contionouos interaction outcome from [0, 1] "
			+ "to a discreete value.");
	c.gridx = 1;
	c.gridy = i++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(opMultiplier, c);

	// threshold
	lbl = new JLabel("Confidence threshold:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = i;
	panel.add(lbl, c);
	threshold = new JSpinner(new SpinnerNumberModel(0.95, 0, 1, 0.05));
	((JSpinner.DefaultEditor) threshold.getEditor()).getTextField()
		.setColumns(3);
	threshold
		.setToolTipText("A threshold that determines the confidence value when the "
			+ "computed trust suffices and opinions are not required.");
	c.gridx = 1;
	c.gridy = i++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(threshold, c);

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
