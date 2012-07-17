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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observer;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import testbed.common.ClassLoaderUtils;
import testbed.interfaces.IMetric;
import testbed.interfaces.IParametersPanel;
import testbed.interfaces.IScenario;
import testbed.interfaces.ITrustModel;

public class MainPanel extends JPanel implements IParametersPanel {
    private static final long serialVersionUID = -1187728078314667265L;

    private ClassLoader cl;
    private Observer observer;

    private JComboBox tmCmb = new JComboBox();
    private JComboBox scnCmb = new JComboBox();
    private List<JCheckBox> mtrChbs = new ArrayList<JCheckBox>();

    private JLabel tmLbl = new JLabel("Select trust model:  ");
    private JLabel scnLbl = new JLabel("Select scenario:  ");
    private JLabel mtrLbl = new JLabel("Select metrics:  ");

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
	return new Object[] { tmCmb.getSelectedItem(),
		scnCmb.getSelectedItem(), getMetrics() };
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
	    tmCmb.addItem(tm);

	tmCmb.addActionListener(listener);

	c.gridx = 0;
	c.gridy = 0;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	panel.add(tmLbl, c);

	c.gridx = 1;
	c.gridy = 0;
	c.anchor = GridBagConstraints.LINE_START;
	c.fill = GridBagConstraints.HORIZONTAL;
	panel.add(tmCmb, c);

	// Scenarios
	for (IScenario scn : ClassLoaderUtils.lookUp(IScenario.class, cl))
	    scnCmb.addItem(scn);

	scnCmb.addActionListener(listener);

	c.gridx = 0;
	c.gridy = 1;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	panel.add(scnLbl, c);

	c.gridx = 1;
	c.gridy = 1;
	c.anchor = GridBagConstraints.LINE_START;
	c.fill = GridBagConstraints.HORIZONTAL;
	panel.add(scnCmb, c);

	// Metrics
	c.gridx = 0;
	c.gridy = 2;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.LINE_END;
	panel.add(mtrLbl, c);

	JCheckBox cb = null;
	int i = 2;
	c.anchor = GridBagConstraints.LINE_START;

	for (IMetric mtr : ClassLoaderUtils.lookUp(IMetric.class, cl)) {
	    cb = new JCheckBox(mtr.getName(), true);
	    cb.addActionListener(listener);
	    cb.putClientProperty(mtr.getName(), mtr);
	    c.gridx = 1;
	    c.gridy = i++;
	    panel.add(cb, c);
	    mtrChbs.add(cb);
	}

	return panel;
    }

    private Set<IMetric> getMetrics() {
	Set<IMetric> metrics = new HashSet<IMetric>();

	for (JCheckBox jcb : mtrChbs) {
	    if (jcb.isSelected()) {
		metrics.add((IMetric) jcb.getClientProperty(jcb.getText()));
	    }
	}

	return metrics;
    }

    public void validateParameters() {
	boolean valid = false;

	for (JCheckBox jcb : mtrChbs)
	    valid = valid || jcb.isSelected();

	observer.update(null, valid);
	observer.update(null, scnCmb.getSelectedItem());
	observer.update(null, tmCmb.getSelectedItem());
    }
}
