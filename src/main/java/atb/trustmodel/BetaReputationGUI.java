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
package atb.trustmodel;

import atb.gui.WizardPanelDescriptor;
import atb.interfaces.ParametersPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Observer;

public class BetaReputationGUI extends JPanel implements ParametersPanel {

    private static final long serialVersionUID = -1558821473401798087L;

    private JSpinner lambdaExp, lambdaOp;

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
        return new Object[]{getThreshold(), getError()};
    }

    private double getError() {
        return Double.parseDouble(String.valueOf(lambdaOp.getValue()));
    }

    private double getThreshold() {
        return Double.parseDouble(String.valueOf(lambdaExp.getValue()));
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

        // lambda experiences
        lbl = new JLabel("Experiences aging factor:  ");
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 0;
        c.gridy = i;
        panel.add(lbl, c);
        lambdaExp = new JSpinner(new SpinnerNumberModel(1, 0, 1, 0.05));
        ((JSpinner.DefaultEditor) lambdaExp.getEditor()).getTextField()
                .setColumns(3);
        lambdaExp.setToolTipText(
                "Factor that determines the exponential aging of the experiences.");
        c.gridx = 1;
        c.gridy = i++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        panel.add(lambdaExp, c);

        // lambda opinions
        lbl = new JLabel("Opinions aging factor:  ");
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 0;
        c.gridy = i;
        panel.add(lbl, c);
        lambdaOp = new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.05));
        ((JSpinner.DefaultEditor) lambdaOp.getEditor()).getTextField()
                .setColumns(3);
        lambdaOp.setToolTipText(
                "Factor that determines the exponential aging of the opinions.");
        c.gridx = 1;
        c.gridy = i++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        panel.add(lambdaOp, c);

        return panel;
    }

    /**
     * Evaluates the deception model distribution parameters.
     * <p>
     * This method invokes the observer's update method. (Observer should be set
     * to a {@link WizardPanelDescriptor} instance.)
     */
    private void validateParameters() {
        boolean valid = true;

        observer.update(null, valid);
    }

}
