/*
 * Copyright (c) 2013 David Jelenc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     David Jelenc - initial API and implementation
 */
package atb.scenario;

import atb.gui.WizardPanelDescriptor;
import atb.interfaces.ParametersPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Observer;

public class TargetedAttackGUI extends JPanel implements ParametersPanel {
    protected static final long serialVersionUID = -1558821473401798087L;

    protected JSpinner numOfAgents, numOfTargets, numOfAttackers, numOfPartners,
            sdExperiences, sdOpinions;
    @SuppressWarnings("rawtypes")
    protected JComboBox strategy;

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
        return new Object[]{getNumberOfAgents(), getNumberOfAttackers(),
                getNumberOfTargets(), getNumberOfPartners(), getSdExperiences(),
                getSdOpinions(), getStrategy()};
    }

    protected int getNumberOfTargets() {
        return Integer.parseInt(String.valueOf(numOfTargets.getValue()));
    }

    protected int getNumberOfAttackers() {
        return Integer.parseInt(String.valueOf(numOfAttackers.getValue()));
    }

    protected int getNumberOfPartners() {
        return Integer.parseInt(String.valueOf(numOfPartners.getValue()));
    }

    protected int getNumberOfAgents() {
        return Integer.parseInt(String.valueOf(numOfAgents.getValue()));
    }

    protected double getSdExperiences() {
        return Double.parseDouble(String.valueOf(sdExperiences.getValue()));
    }

    protected double getSdOpinions() {
        return Double.parseDouble(String.valueOf(sdOpinions.getValue()));
    }

    protected TargetedAttackStrategy getStrategy() {
        return (TargetedAttackStrategy) strategy.getSelectedItem();
    }

    /**
     * Creates the panel and the elements and adds elements to the panel.
     *
     * @return The panel with created elements
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected JPanel getContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        // this evaluates input whenever a value changes
        final ChangeListener listener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                validateParameters();
            }
        };

        GridBagConstraints c = new GridBagConstraints();

        int i = 0;
        JLabel lbl;
        c.gridwidth = 1;

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
        numOfAgents.setToolTipText(
                "The number of agents must be between 1 and 300.");
        numOfAgents.addChangeListener(listener);
        c.gridx = 1;
        c.gridy = i++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        panel.add(numOfAgents, c);

        // Number of attackers
        lbl = new JLabel("Number of attackers:  ");
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 0;
        c.gridy = i;
        panel.add(lbl, c);
        numOfAttackers = new JSpinner(new SpinnerNumberModel(40, 0, 300, 10));
        ((JSpinner.DefaultEditor) numOfAttackers.getEditor()).getTextField()
                .setColumns(3);
        numOfAttackers.setToolTipText(
                "The number of agents that badmounth targeted agents.");
        numOfAttackers.addChangeListener(listener);
        c.gridx = 1;
        c.gridy = i++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        panel.add(numOfAttackers, c);

        // Number of targets
        lbl = new JLabel("Number of targets:  ");
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 0;
        c.gridy = i;
        panel.add(lbl, c);
        numOfTargets = new JSpinner(new SpinnerNumberModel(20, 0, 300, 10));
        ((JSpinner.DefaultEditor) numOfTargets.getEditor()).getTextField()
                .setColumns(3);
        numOfTargets.setToolTipText(
                "The number of agents that get badmounthed by attackers. "
                        + "Alpha does not get to interact with these agents.");
        numOfTargets.addChangeListener(listener);
        c.gridx = 1;
        c.gridy = i++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        panel.add(numOfTargets, c);

        // Number of partners
        lbl = new JLabel("Number of interaction partners:  ");
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 0;
        c.gridy = i;
        panel.add(lbl, c);
        numOfPartners = new JSpinner(new SpinnerNumberModel(20, 0, 300, 10));
        ((JSpinner.DefaultEditor) numOfPartners.getEditor()).getTextField()
                .setColumns(3);
        numOfPartners.setToolTipText(
                "The number of agents with which Alpha interacts. "
                        + "Targeted agents are never selected as interaction partners.");
        numOfPartners.addChangeListener(listener);
        c.gridx = 1;
        c.gridy = i++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        panel.add(numOfPartners, c);

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
                        + " distribution that generates opinions. "
                        + "Valid nubers range between 0 and 1.");
        c.gridx = 1;
        c.gridy = i++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        panel.add(sdOpinions, c);

        // Strategy
        lbl = new JLabel("Strategy:  ");
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 0;
        c.gridy = i;
        panel.add(lbl, c);
        strategy = new JComboBox();

        for (TargetedAttackStrategy s : TargetedAttackStrategy.values())
            strategy.addItem(s);

        strategy.setToolTipText("In LEVEL 1 attackers lie about targets; "
                + "in LEVEL 2 attackers additionally give honest opinions about other attackers; "
                + "in LEVEL 3 also targets give honest opinions about attackers.");
        c.gridx = 1;
        c.gridy = i++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        panel.add(strategy, c);

        return panel;
    }

    /**
     * Evaluates the scenario parameters.
     * <p>
     * This method invokes the observer's update method. (Observer should be set
     * to a {@link WizardPanelDescriptor} instance.)
     */
    protected void validateParameters() {
        boolean valid = true;

        valid &= getNumberOfAttackers() < getNumberOfAgents();
        valid &= getNumberOfPartners() <= getNumberOfAgents()
                - getNumberOfTargets();

        observer.update(null, valid);
    }
}
