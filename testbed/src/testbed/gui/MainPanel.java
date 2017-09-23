/*
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Robert Eckstein - initial API and implementation
 *        http://www.oracle.com/technetwork/articles/javase/wizard-136789.html
 *     David Jelenc - adaptation and modification for ATB
 */
package testbed.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;
import java.util.Observer;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import testbed.common.ClassLoaderUtils;
import testbed.interfaces.Accuracy;
import testbed.interfaces.InteractionPartnerSelection;
import testbed.interfaces.OpinionCost;
import testbed.interfaces.OpinionProviderSelection;
import testbed.interfaces.ParametersPanel;
import testbed.interfaces.Scenario;
import testbed.interfaces.SelectingInteractionPartners;
import testbed.interfaces.SelectingOpinionProviders;
import testbed.interfaces.TrustModel;
import testbed.interfaces.Utility;

public class MainPanel extends JPanel implements ParametersPanel {
    private static final long serialVersionUID = -1187728078314667265L;

    private List<TrustModel<?, ?>> allTrustModels;

    private ClassLoader cl;
    private Observer observer;

    @SuppressWarnings("rawtypes")
    private JComboBox trustModel = new JComboBox();
    @SuppressWarnings("rawtypes")
    private JComboBox scenario = new JComboBox();
    @SuppressWarnings("rawtypes")
    private JComboBox accMetric = new JComboBox();
    @SuppressWarnings("rawtypes")
    private JComboBox utilMetric = new JComboBox();
    @SuppressWarnings("rawtypes")
    private JComboBox ocMetric = new JComboBox();

    private JSpinner batchRunDuration = new JSpinner(
	    new SpinnerNumberModel(500, 1, Integer.MAX_VALUE, 100));

    private JLabel tmLabel = new JLabel("Trust model:  ");
    private JLabel scnLabel = new JLabel("Scenario:  ");
    private JLabel rmLabel = new JLabel("Accuracy:  ");
    private JLabel utilLabel = new JLabel("Utility:  ");
    private JLabel ocLabel = new JLabel("Opinion cost:  ");
    private JLabel brdLabel = new JLabel("Batch run duration:  ");

    @Override
    public Component getComponent() {
	return this;
    }

    @Override
    public void initialize(Observer o, Object... params) {
	observer = o;
	cl = (ClassLoader) params[0];

	allTrustModels = ClassLoaderUtils.lookUpTrustModels(cl);

	JPanel contentPanel = getContentPanel();
	contentPanel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
	setLayout(new BorderLayout());
	add(contentPanel, BorderLayout.CENTER);
	addComponentListener(new ComponentListener() {
	    @Override
	    public void componentHidden(ComponentEvent e) {
		validateParameters();
	    }

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
	});
    }

    @Override
    public Object[] getParameters() {
	return new Object[] { scenario.getSelectedItem(),
		trustModel.getSelectedItem(), accMetric.getSelectedItem(),
		getUtilityMetric(), getOpinionCostMetric(),
		getBatchRunDuration() };
    }

    private OpinionCost getOpinionCostMetric() {
	if (ocMetric.isEnabled()) {
	    return (OpinionCost) ocMetric.getSelectedItem();
	} else {
	    return null;
	}
    }

    private Utility getUtilityMetric() {
	if (utilMetric.isEnabled()) {
	    return (Utility) utilMetric.getSelectedItem();
	} else {
	    return null;
	}
    }

    private int getBatchRunDuration() {
	return Integer.parseInt(String.valueOf(batchRunDuration.getValue()));
    }

    private boolean[] type(Scenario<?> scn) {
	final boolean noDecisions = !(scn instanceof InteractionPartnerSelection)
		&& !(scn instanceof OpinionProviderSelection);
	final boolean modeA = (scn instanceof InteractionPartnerSelection)
		&& !(scn instanceof OpinionProviderSelection);
	final boolean modeB = (scn instanceof InteractionPartnerSelection)
		&& (scn instanceof OpinionProviderSelection);

	return new boolean[] { noDecisions, modeA, modeB };
    }

    @SuppressWarnings("unchecked")
    private void populateTrustModels(boolean noDecisions, boolean modeA,
	    boolean modeB) {
	trustModel.removeAllItems();

	for (TrustModel<?, ?> tm : allTrustModels) {
	    final boolean tmNoDecisions = !(tm instanceof SelectingInteractionPartners)
		    && !(tm instanceof SelectingOpinionProviders);
	    final boolean tmModeA = tm instanceof SelectingInteractionPartners
		    && !(tm instanceof SelectingOpinionProviders);
	    final boolean tmModeB = tm instanceof SelectingInteractionPartners
		    && tm instanceof SelectingOpinionProviders;

	    if (modeA && tmModeA || modeB && tmModeB
		    || noDecisions && tmNoDecisions) {
		trustModel.addItem(tm);
	    }
	}
    }

    @SuppressWarnings("unchecked")
    private JPanel getContentPanel() {
	JPanel panel = new JPanel();

	// validate all parameters whenever an item changes
	ActionListener listener = new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		validateParameters();
	    }
	};

	panel.setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();

	// Scenarios
	for (Scenario<?> scn : ClassLoaderUtils.lookUp(Scenario.class, cl))
	    scenario.addItem(scn);

	// scenario.setRenderer(renderer);
	scenario.addActionListener(listener);
	scenario.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		final Scenario<?> scn = (Scenario<?>) scenario
			.getSelectedItem();
		final boolean[] type = type(scn);

		populateTrustModels(type[0], type[1], type[2]);
	    }
	});

	c.gridx = 0;
	c.gridy = 0;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	panel.add(scnLabel, c);

	c.gridx = 1;
	c.gridy = 0;
	c.anchor = GridBagConstraints.LINE_START;
	c.fill = GridBagConstraints.HORIZONTAL;
	panel.add(scenario, c);

	// Trust models
	final boolean[] types = type((Scenario<?>) scenario.getSelectedItem());
	populateTrustModels(types[0], types[1], types[2]);
	trustModel.addActionListener(listener);

	c.gridx = 0;
	c.gridy = 1;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	panel.add(tmLabel, c);

	c.gridx = 1;
	c.gridy = 1;
	c.anchor = GridBagConstraints.LINE_START;
	c.fill = GridBagConstraints.HORIZONTAL;
	panel.add(trustModel, c);

	// Accuracy
	for (Accuracy mtr : ClassLoaderUtils.lookUp(Accuracy.class, cl)) {
	    accMetric.addItem(mtr);
	}

	accMetric.addActionListener(listener);

	c.gridx = 0;
	c.gridy = 2;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	panel.add(rmLabel, c);

	c.gridx = 1;
	c.gridy = 2;
	c.anchor = GridBagConstraints.LINE_START;
	c.fill = GridBagConstraints.HORIZONTAL;
	panel.add(accMetric, c);

	// Utility
	for (Utility m : ClassLoaderUtils.lookUp(Utility.class, cl)) {
	    utilMetric.addItem(m);
	}

	utilMetric.addActionListener(listener);

	c.gridx = 0;
	c.gridy = 3;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	panel.add(utilLabel, c);

	c.gridx = 1;
	c.gridy = 3;
	c.anchor = GridBagConstraints.LINE_START;
	c.fill = GridBagConstraints.HORIZONTAL;
	panel.add(utilMetric, c);

	// Opinion cost
	for (OpinionCost m : ClassLoaderUtils.lookUp(OpinionCost.class, cl)) {
	    ocMetric.addItem(m);
	}

	ocMetric.addActionListener(listener);

	c.gridx = 0;
	c.gridy = 4;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	panel.add(ocLabel, c);

	c.gridx = 1;
	c.gridy = 4;
	c.anchor = GridBagConstraints.LINE_START;
	c.fill = GridBagConstraints.HORIZONTAL;
	panel.add(ocMetric, c);

	// empty region
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = 5;
	panel.add(new JLabel(), c);
	c.gridx = 1;
	c.gridy = 5;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(new JLabel("<html><br/>"), c);

	// The batch run duration
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = 6;
	panel.add(brdLabel, c);
	((JSpinner.DefaultEditor) batchRunDuration.getEditor()).getTextField()
		.setColumns(4);
	batchRunDuration
		.setToolTipText("The duration of the batch run in ticks.");
	c.gridx = 1;
	c.gridy = 6;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(batchRunDuration, c);

	return panel;
    }

    public void validateParameters() {
	final TrustModel<?, ?> tm = (TrustModel<?, ?>) trustModel
		.getSelectedItem();
	final Scenario<?> scn = (Scenario<?>) scenario.getSelectedItem();
	final Accuracy acc = (Accuracy) accMetric.getSelectedItem();
	final Utility util = (Utility) utilMetric.getSelectedItem();
	final OpinionCost opcst = (OpinionCost) ocMetric.getSelectedItem();

	final boolean modeA = (scn instanceof InteractionPartnerSelection)
		&& !(scn instanceof OpinionProviderSelection);

	final boolean modeB = (scn instanceof InteractionPartnerSelection)
		&& (scn instanceof OpinionProviderSelection);

	utilMetric.setEnabled(modeA || modeB);
	utilLabel.setEnabled(modeA || modeB);

	ocMetric.setEnabled(modeB);
	ocLabel.setEnabled(modeB);

	observer.update(null, tm);
	observer.update(null, scn);
	observer.update(null, acc);

	if (utilMetric.isEnabled())
	    observer.update(null, util);

	if (ocMetric.isEnabled())
	    observer.update(null, opcst);
    }

    public void setBatchRun(boolean batchRun) {
	batchRunDuration.setEnabled(batchRun);
	brdLabel.setEnabled(batchRun);
    }
}
