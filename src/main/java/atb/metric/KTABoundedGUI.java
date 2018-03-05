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
package atb.metric;

import atb.gui.WizardPanelDescriptor;
import atb.interfaces.ParametersPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Observer;

public class KTABoundedGUI extends JPanel implements ParametersPanel {

    private static final long serialVersionUID = -155882147856087L;

    protected JSpinner lower, upper;

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
        return new Object[]{getLowerBound(), getUpperBound()};
    }

    protected double getLowerBound() {
        return Double.parseDouble(String.valueOf(lower.getValue()));
    }

    protected double getUpperBound() {
        return Double.parseDouble(String.valueOf(upper.getValue()));
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

        int i = 2;
        JLabel lbl;
        c.gridwidth = 1;

        // Lower bound
        lbl = new JLabel("Lower bound:  ");
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 0;
        c.gridy = i;
        panel.add(lbl, c);
        lower = new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.1));
        ((JSpinner.DefaultEditor) lower.getEditor()).getTextField()
                .setColumns(3);
        lower.setToolTipText(
                "The metric will evaluate trust towards agents whose capabilty is higher than the lower bound.");
        c.gridx = 1;
        c.gridy = i++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        panel.add(lower, c);

        // Upper bound
        lbl = new JLabel("Upper bound:  ");
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 0;
        c.gridy = i;
        panel.add(lbl, c);
        upper = new JSpinner(new SpinnerNumberModel(1, 0, 1, 0.1));
        ((JSpinner.DefaultEditor) upper.getEditor()).getTextField()
                .setColumns(3);
        upper.setToolTipText(
                "The metric will evaluate trust towards agents whose capabilty is lower than the upper bound.");
        c.gridx = 1;
        c.gridy = i++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        panel.add(upper, c);

        return panel;
    }

    /**
     * Evaluates the deception model distribution parameters.
     * <p>
     * <p>
     * This method invokes the observer's update method. (Observer should be set
     * to a {@link WizardPanelDescriptor} instance.)
     */
    private void validateParameters() {
        observer.update(null, true);
    }

}
