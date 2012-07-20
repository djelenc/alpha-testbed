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
import java.util.Observer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import testbed.common.ClassLoaderUtils;
import testbed.interfaces.IParametersPanel;
import testbed.interfaces.IRankingMetric;
import testbed.interfaces.IScenario;
import testbed.interfaces.ITrustModel;
import testbed.interfaces.IUtilityMetric;

public class MainPanel extends JPanel implements IParametersPanel {
    private static final long serialVersionUID = -1187728078314667265L;

    private ClassLoader cl;
    private Observer observer;

    private JComboBox trustModel = new JComboBox();
    private JComboBox scenario = new JComboBox();
    private JComboBox rankingMetric = new JComboBox();
    private JComboBox utilityMetric = new JComboBox();

    private JLabel tmLabel = new JLabel("Trust model:  ");
    private JLabel scnLabel = new JLabel("Scenario:  ");
    private JLabel rmLabel = new JLabel("Ranking metric:  ");
    private JLabel umLabel = new JLabel("Utility metric:  ");

    @Override
    public Component getComponent() {
	return this;
    }

    @Override
    public void initialize(Observer o, Object... params) {
	observer = o;
	cl = (ClassLoader) params[0];

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
	return new Object[] { trustModel.getSelectedItem(),
		scenario.getSelectedItem(), rankingMetric.getSelectedItem(),
		utilityMetric.getSelectedItem() };
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

	// Trust models
	for (ITrustModel tm : ClassLoaderUtils.lookUp(ITrustModel.class, cl))
	    trustModel.addItem(tm);

	trustModel.addActionListener(listener);

	c.gridx = 0;
	c.gridy = 0;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	panel.add(tmLabel, c);

	c.gridx = 1;
	c.gridy = 0;
	c.anchor = GridBagConstraints.LINE_START;
	c.fill = GridBagConstraints.HORIZONTAL;
	panel.add(trustModel, c);

	// Scenarios
	for (IScenario scn : ClassLoaderUtils.lookUp(IScenario.class, cl))
	    scenario.addItem(scn);

	scenario.addActionListener(listener);

	c.gridx = 0;
	c.gridy = 1;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	panel.add(scnLabel, c);

	c.gridx = 1;
	c.gridy = 1;
	c.anchor = GridBagConstraints.LINE_START;
	c.fill = GridBagConstraints.HORIZONTAL;
	panel.add(scenario, c);

	// Ranking metric
	for (IRankingMetric mtr : ClassLoaderUtils.lookUp(IRankingMetric.class,
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
	for (IUtilityMetric mtr : ClassLoaderUtils.lookUp(IUtilityMetric.class,
		cl)) {
	    utilityMetric.addItem(mtr);
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

	return panel;
    }

    public void validateParameters() {
	// TODO: include both metrics
	observer.update(null, scenario.getSelectedItem());
	observer.update(null, trustModel.getSelectedItem());
    }
}
