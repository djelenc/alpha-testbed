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
import testbed.interfaces.DecisionMaking;
import testbed.interfaces.ParametersPanel;
import testbed.interfaces.PartnerSelection;
import testbed.interfaces.RankingMetric;
import testbed.interfaces.Scenario;
import testbed.interfaces.TrustModel;
import testbed.interfaces.UtilityMetric;

public class MainPanel extends JPanel implements ParametersPanel {
    private static final long serialVersionUID = -1187728078314667265L;

    private List<TrustModel<?>> allTrustModels;

    private ClassLoader cl;
    private Observer observer;

    private JComboBox trustModel = new JComboBox();
    private JComboBox scenario = new JComboBox();
    private JComboBox rankingMetric = new JComboBox();
    private JComboBox utilityMetric = new JComboBox();

    private JSpinner batchRunDuration = new JSpinner(new SpinnerNumberModel(
	    300, 1, Integer.MAX_VALUE, 100));

    private JLabel tmLabel = new JLabel("Trust model:  ");
    private JLabel scnLabel = new JLabel("Scenario:  ");
    private JLabel rmLabel = new JLabel("Ranking metric:  ");
    private JLabel umLabel = new JLabel("Utility metric:  ");
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
		trustModel.getSelectedItem(), rankingMetric.getSelectedItem(),
		getUtilityMetric(), getBatchRunDuration() };
    }

    private UtilityMetric getUtilityMetric() {
	if (utilityMetric.isEnabled()) {
	    return (UtilityMetric) utilityMetric.getSelectedItem();
	} else {
	    return null;
	}
    }

    private int getBatchRunDuration() {
	return Integer.parseInt(String.valueOf(batchRunDuration.getValue()));
    }

    private void populateTrustModels(boolean decisionMaking) {
	trustModel.removeAllItems();

	for (TrustModel<?> tm : allTrustModels) {
	    if (tm instanceof DecisionMaking && decisionMaking
		    || !(tm instanceof DecisionMaking) && !decisionMaking) {
		trustModel.addItem(tm);
	    }
	}
    }

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
	for (Scenario scn : ClassLoaderUtils.lookUp(Scenario.class, cl))
	    scenario.addItem(scn);

	// scenario.setRenderer(renderer);
	scenario.addActionListener(listener);
	scenario.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		populateTrustModels(scenario.getSelectedItem() instanceof PartnerSelection);
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
	populateTrustModels(scenario.getSelectedItem() instanceof PartnerSelection);
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

	// Ranking metric
	for (RankingMetric mtr : ClassLoaderUtils.lookUp(RankingMetric.class,
		cl)) {
	    rankingMetric.addItem(mtr);
	}

	rankingMetric.addActionListener(listener);

	c.gridx = 0;
	c.gridy = 2;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	panel.add(rmLabel, c);

	c.gridx = 1;
	c.gridy = 2;
	c.anchor = GridBagConstraints.LINE_START;
	c.fill = GridBagConstraints.HORIZONTAL;
	panel.add(rankingMetric, c);

	// Utility metric
	for (UtilityMetric m : ClassLoaderUtils.lookUp(UtilityMetric.class,
		cl)) {
	    utilityMetric.addItem(m);
	}

	utilityMetric.addActionListener(listener);

	c.gridx = 0;
	c.gridy = 3;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	panel.add(umLabel, c);

	c.gridx = 1;
	c.gridy = 3;
	c.anchor = GridBagConstraints.LINE_START;
	c.fill = GridBagConstraints.HORIZONTAL;
	panel.add(utilityMetric, c);

	// empty region
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = 4;
	panel.add(new JLabel(), c);
	c.gridx = 1;
	c.gridy = 4;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(new JLabel("<html><br/>"), c);

	// The batch run duration
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	c.gridx = 0;
	c.gridy = 5;
	panel.add(brdLabel, c);
	((JSpinner.DefaultEditor) batchRunDuration.getEditor()).getTextField()
		.setColumns(4);
	batchRunDuration
		.setToolTipText("The duration of the batch run in ticks.");
	c.gridx = 1;
	c.gridy = 5;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_START;
	panel.add(batchRunDuration, c);

	return panel;
    }

    public void validateParameters() {
	final TrustModel<?> tm = (TrustModel<?>) trustModel.getSelectedItem();
	final Scenario scn = (Scenario) scenario.getSelectedItem();
	final RankingMetric rm = (RankingMetric) rankingMetric
		.getSelectedItem();
	final UtilityMetric um = (UtilityMetric) utilityMetric
		.getSelectedItem();

	final boolean enabled = scn instanceof PartnerSelection;
	utilityMetric.setEnabled(enabled);
	umLabel.setEnabled(enabled);

	observer.update(null, scn);
	observer.update(null, tm);
	observer.update(null, rm);

	if (utilityMetric.isEnabled()) {
	    observer.update(null, um);
	}
    }

    public void setBatchRun(boolean batchRun) {
	batchRunDuration.setEnabled(batchRun);
	brdLabel.setEnabled(batchRun);
    }
}