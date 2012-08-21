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
import testbed.interfaces.IParametersPanel;

public class TravosGUI extends JPanel implements IParametersPanel {

    private static final long serialVersionUID = -1558821473401798087L;

    private JSpinner multiplier, threshold, error;

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
	return new Object[] { getMultiplier(), getThreshold(), getError() };
    }

    private double getError() {
	return Double.parseDouble(String.valueOf(error.getValue()));
    }

    private double getThreshold() {
	return Double.parseDouble(String.valueOf(threshold.getValue()));
    }

    private double getMultiplier() {
	return Double.parseDouble(String.valueOf(multiplier.getValue()));
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

	// multiplier
	lbl = new JLabel("Multiplier:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = i;
	panel.add(lbl, c);
	multiplier = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
	((JSpinner.DefaultEditor) multiplier.getEditor()).getTextField()
		.setColumns(3);
	multiplier
		.setToolTipText("Multiplier that converts a contionouos interaction outcome from [0, 1] "
			+ "to a discreete value.");
	c.gridx = 1;
	c.gridy = i++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(multiplier, c);

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
