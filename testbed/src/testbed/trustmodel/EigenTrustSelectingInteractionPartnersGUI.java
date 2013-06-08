package testbed.trustmodel;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class EigenTrustSelectingInteractionPartnersGUI extends EigenTrustGUI {

    private static final long serialVersionUID = -9133573313499877710L;

    protected JLabel zeroThresholdLabel;
    protected JSpinner zeroThreshold;
    protected JComboBox selectionProcedure;

    @Override
    protected JPanel getContentPanel() {
	final JPanel panel = super.getContentPanel();
	GridBagConstraints c = new GridBagConstraints();

	JLabel lbl = new JLabel("Selection procedure:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;

	int offset = panel.getComponentCount() / 2 + 1;

	c.gridy = ++offset;
	panel.add(lbl, c);
	selectionProcedure = new JComboBox();
	selectionProcedure.addItem("Deterministic ");
	selectionProcedure.addItem("Probabilistic ");
	selectionProcedure.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		zeroThreshold.setEnabled(isProbabilistic());
		zeroThresholdLabel.setEnabled(isProbabilistic());
	    }
	});

	c.gridx = 1;
	c.gridy = offset;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(selectionProcedure, c);

	zeroThresholdLabel = new JLabel("Zero threshold:  ");
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = ++offset;
	panel.add(zeroThresholdLabel, c);
	zeroThreshold = new JSpinner(new SpinnerNumberModel(0.1, 0, 1, 0.05));
	((JSpinner.DefaultEditor) zeroThreshold.getEditor()).getTextField()
		.setColumns(3);
	zeroThreshold
		.setToolTipText("Probability with which a peer with trust value 0 is selected.");
	c.gridx = 1;
	c.gridy = offset;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	zeroThreshold.setEnabled(isProbabilistic());
	panel.add(zeroThreshold, c);

	return panel;
    }

    @Override
    public Object[] getParameters() {
	Object[] parent = super.getParameters();

	Object[] results = new Object[parent.length + 2];

	for (int i = 0; i < parent.length; i++)
	    results[i] = parent[i];

	results[parent.length] = isProbabilistic();
	results[parent.length + 1] = Double.parseDouble(String
		.valueOf(zeroThreshold.getValue()));

	return results;
    }

    protected boolean isProbabilistic() {
	return selectionProcedure.getSelectedItem().equals("Probabilistic ");
    }
}
