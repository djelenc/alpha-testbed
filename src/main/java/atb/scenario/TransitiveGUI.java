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
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Observer;

public class TransitiveGUI extends JPanel implements ParametersPanel {
    protected static final long serialVersionUID = -1558821473401798087L;

    protected JSpinner numOfAgents, sdExperiences, sdOpinions, interDens,
            opDens;

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
        return new Object[]{getNumberOfAgents(), getSdExperiences(),
                getSdOpinions(), getInteractionDensity(), getOpinionDensity()};
    }

    protected double getOpinionDensity() {
        return Double.parseDouble(String.valueOf(opDens.getValue()));
    }

    protected double getInteractionDensity() {
        return Double.parseDouble(String.valueOf(interDens.getValue()));
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

    /**
     * Creates the panel and the elements and adds elements to the panel.
     *
     * @return The panel with created elements
     */
    protected JPanel getContentPanel() {
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

        // Interaction density
        lbl = new JLabel("Interaction density:  ");
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 0;
        c.gridy = i;
        panel.add(lbl, c);
        interDens = new JSpinner(new SpinnerNumberModel(0.10, 0, 1, 0.05));
        ((JSpinner.DefaultEditor) interDens.getEditor()).getTextField()
                .setColumns(3);
        interDens.setToolTipText(
                "The proportion of agents with agent Alpha interacts.");
        c.gridx = 1;
        c.gridy = i++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        panel.add(interDens, c);

        // Opinion density
        lbl = new JLabel("Opinion density:  ");
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 0;
        c.gridy = i;
        panel.add(lbl, c);
        opDens = new JSpinner(new SpinnerNumberModel(1, 0, 1, 0.05));
        ((JSpinner.DefaultEditor) opDens.getEditor()).getTextField()
                .setColumns(3);
        opDens.setToolTipText("How many agents does a particular agent knows.");
        c.gridx = 1;
        c.gridy = i++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        panel.add(opDens, c);

        return panel;
    }

    /**
     * Evaluates the deception model distribution parameters.
     * <p>
     * <p>
     * This method invokes the observer's update method. (Observer should be set
     * to a {@link WizardPanelDescriptor} instance.)
     */
    protected void validateParameters() {
        boolean valid = true;

        observer.update(null, valid);
    }
}
