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

public class OscillationGUI extends JPanel implements ParametersPanel {
    private static final long serialVersionUID = -1558821473401798087L;

    private JSpinner numOfAgents, sdExperiences, sdOpinions, fracGood, fracBad,
            changeInterval;
    private JLabel fracNeutral;

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
        return new Object[]{getNumberOfAgents(), getSdExperiences(),
                getSdOpinions(), getChangeInterval(), getFracGood(),
                getFracBad()};
    }

    private Object getChangeInterval() {
        return Integer.parseInt(String.valueOf(changeInterval.getValue()));
    }

    private double getFracBad() {
        return Double.parseDouble(String.valueOf(fracBad.getValue()));
    }

    private double getFracGood() {
        return Double.parseDouble(String.valueOf(fracGood.getValue()));
    }

    private int getNumberOfAgents() {
        return Integer.parseInt(String.valueOf(numOfAgents.getValue()));
    }

    private double getSdExperiences() {
        return Double.parseDouble(String.valueOf(sdExperiences.getValue()));
    }

    private double getSdOpinions() {
        return Double.parseDouble(String.valueOf(sdOpinions.getValue()));
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
        c.gridx = 1;
        c.gridy = i++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        panel.add(numOfAgents, c);

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

        // Change interval
        lbl = new JLabel("Change interval:  ");
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 0;
        c.gridy = i;
        panel.add(lbl, c);
        changeInterval = new JSpinner(new SpinnerNumberModel(100, 1, 1000, 50));
        ((JSpinner.DefaultEditor) changeInterval.getEditor()).getTextField()
                .setColumns(3);
        changeInterval.setToolTipText("The interval between oscillation.");
        c.gridx = 1;
        c.gridy = i++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        panel.add(changeInterval, c);

        // Fraction of "good" agents
        lbl = new JLabel("Good agents (%):  ");
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 0;
        c.gridy = i;
        panel.add(lbl, c);
        fracGood = new JSpinner(new SpinnerNumberModel(0.3, 0, 1, 0.05));
        ((JSpinner.DefaultEditor) fracGood.getEditor()).getTextField()
                .setColumns(3);
        fracGood.setToolTipText(
                "The proportion of agents that belong to the \"Good\" group.");
        c.gridx = 1;
        c.gridy = i++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        panel.add(fracGood, c);

        // Fraction of "bad" agents
        lbl = new JLabel("Bad agents (%):  ");
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 0;
        c.gridy = i;
        panel.add(lbl, c);
        fracBad = new JSpinner(new SpinnerNumberModel(0.3, 0, 1, 0.05));
        ((JSpinner.DefaultEditor) fracBad.getEditor()).getTextField()
                .setColumns(3);
        fracBad.setToolTipText(
                "The proportion of agents that belong to the \"Bad\" group.");
        c.gridx = 1;
        c.gridy = i++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        panel.add(fracBad, c);

        // Neutral agents (label only)
        lbl = new JLabel("Neutral agents (%):  ");
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 0;
        c.gridy = i;
        panel.add(lbl, c);
        fracNeutral = new JLabel();
        c.gridx = 1;
        c.gridy = i++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        panel.add(fracNeutral, c);

        ChangeListener listener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                validateParameters();
            }
        };

        fracGood.addChangeListener(listener);
        fracBad.addChangeListener(listener);

        return panel;
    }

    /**
     * Evaluates the parameters.
     * <p>
     * This method invokes the observer's update method. (Observer should be set
     * to a {@link WizardPanelDescriptor} instance.)
     */
    private void validateParameters() {
        boolean valid = true;

        valid = getFracBad() + getFracGood() <= 1;

        if (valid) {
            fracNeutral.setText(String.format("%.2f",
                    Math.abs(1 - getFracBad() - getFracGood())));
        } else {
            fracNeutral
                    .setText("<html><font color='red'>Invalid.</font></html>");
        }

        observer.update(null, valid);
    }
}
