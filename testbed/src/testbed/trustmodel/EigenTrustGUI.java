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

public class EigenTrustGUI extends JPanel implements IParametersPanel {

    private static final long serialVersionUID = -1558821473401798087L;

    protected JSpinner weight, expMltpl, opMltpl;

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
	return new Object[] { getWeight(), getExperienceMultiplier(),
		getOpinionMultiplier() };
    }

    protected double getOpinionMultiplier() {
	return Double.parseDouble(String.valueOf(opMltpl.getValue()));
    }

    protected double getExperienceMultiplier() {
	return Double.parseDouble(String.valueOf(expMltpl.getValue()));
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

	// experience multiplier
	lbl = new JLabel("Experience multiplier:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = i;
	panel.add(lbl, c);
	expMltpl = new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));
	((JSpinner.DefaultEditor) expMltpl.getEditor()).getTextField()
		.setColumns(3);
	expMltpl.setToolTipText("Multiplier that converts a contionouos interaction outcome from [0, 1] "
		+ "to a discreete value.");
	c.gridx = 1;
	c.gridy = i++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(expMltpl, c);

	// opinion multiplier
	lbl = new JLabel("Opinions multiplier:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = i;
	panel.add(lbl, c);
	opMltpl = new JSpinner(new SpinnerNumberModel(10, 1, 50, 1));
	((JSpinner.DefaultEditor) opMltpl.getEditor()).getTextField()
		.setColumns(3);
	opMltpl.setToolTipText("Multiplier that converts a contionouos internal trust degree "
		+ "from [0, 1] to a discreete value.");
	c.gridx = 1;
	c.gridy = i++;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(opMltpl, c);

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
