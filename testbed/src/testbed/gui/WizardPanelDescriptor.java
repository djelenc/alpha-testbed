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
import java.awt.FlowLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import testbed.interfaces.Accuracy;
import testbed.interfaces.OpinionCost;
import testbed.interfaces.ParametersPanel;
import testbed.interfaces.Scenario;
import testbed.interfaces.TrustModel;
import testbed.interfaces.Utility;

/**
 * A base descriptor class used to reference a Component panel for the Wizard,
 * as well as provide general rules as to how the panel should behave.
 */
public class WizardPanelDescriptor implements Observer {

    private static final String DEFAULT_IDENTIFIER = "DefaultIdentifier";
    private static final String TITLE = "<html><h2>%s</h2></html>";

    /**
     * Identifier returned by getNextPanelDescriptor() to indicate that this is
     * the last panel and the text of the 'Next' button should change to
     * 'Finish'.
     */
    public static final FinishIdentifier FINISH = new FinishIdentifier();

    private Wizard wizard;
    private Object identifier;

    private Object nextPanelDescriptor = FINISH;
    private Object backPanelDescriptor = null;

    private Component panel;
    private ParametersPanel paramsPanel;
    private String name = String.format(TITLE, "Unnamed");

    /**
     * Default constructor. The id and the Component panel must be set
     * separately.
     */
    public WizardPanelDescriptor() {
	identifier = DEFAULT_IDENTIFIER;
	panel = createPanel(null);
	name = String.format(TITLE, "Unnamed");
    }

    public WizardPanelDescriptor(Object id) {
	identifier = id;
	panel = createPanel(null);
	name = String.format(TITLE, "Unnamed");
    }

    /**
     * Constructor which accepts both the Object-based identifier and a
     * reference to the Component class which makes up the panel.
     * 
     * @param id
     *            Object-based identifier
     * @param panel
     *            A class which extends java.awt.Component that will be inserted
     *            as a panel into the wizard dialog.
     */
    public WizardPanelDescriptor(Object id, ParametersPanel params,
	    String title) {
	identifier = id;
	paramsPanel = params;
	name = String.format(TITLE, title);
	panel = createPanel((params == null ? null : params.getComponent()));
    }

    /**
     * Returns to java.awt.Component that serves as the actual panel.
     * 
     * @return A reference to the java.awt.Component that serves as the panel
     */
    public final Component getPanelComponent() {
	return panel;
    }

    public final ParametersPanel getIParametersPanel() {
	return paramsPanel;
    }

    public final void setIParamsPanel(ParametersPanel params, String title) {
	paramsPanel = params;
	name = String.format(TITLE, title);
	panel = createPanel((params == null ? null : params.getComponent()));
    }

    /**
     * Returns the unique Object-based identifier for this panel descriptor.
     * 
     * @return The Object-based identifier
     */
    public final Object getID() {
	return identifier;
    }

    /**
     * Sets the Object-based identifier for this panel. The identifier must be
     * unique from all the other identifiers in the panel.
     * 
     * @param id
     *            Object-based identifier for this panel.
     */
    public final void setID(Object id) {
	identifier = id;
    }

    final void setWizard(Wizard w) {
	wizard = w;
    }

    /**
     * Returns a reference to the Wizard component.
     * 
     * @return The Wizard class hosting this descriptor.
     */
    public final Wizard getWizard() {
	return wizard;
    }

    /**
     * Returns a reference to the current WizardModel for this Wizard component.
     * 
     * @return The current WizardModel for this Wizard component.
     */
    public WizardModel getWizardModel() {
	return wizard.getModel();
    }

    // Override this method to provide an Object-based identifier
    // for the next panel.

    /**
     * Override this class to provide the Object-based identifier of the panel
     * that the user should traverse to when the Next button is pressed. Note
     * that this method is only called when the button is actually pressed, so
     * that the panel can change the next panel's identifier dynamically at
     * runtime if necessary. Return null if the button should be disabled.
     * Return FinishIdentfier if the button text should change to 'Finish' and
     * the dialog should end.
     * 
     * @return Object-based identifier.
     */
    public Object getNext() {
	return this.nextPanelDescriptor;
    }

    public void setNext(Object nextPanelDescriptor) {
	this.nextPanelDescriptor = nextPanelDescriptor;
    }

    // Override this method to provide an Object-based identifier
    // for the previous panel.

    /**
     * Override this class to provide the Object-based identifier of the panel
     * that the user should traverse to when the Back button is pressed. Note
     * that this method is only called when the button is actually pressed, so
     * that the panel can change the previous panel's identifier dynamically at
     * runtime if necessary. Return null if the button should be disabled.
     * 
     * @return Object-based identifier
     */
    public Object getBack() {
	return this.backPanelDescriptor;
    }

    public void setBack(Object backPanelDescriptor) {
	this.backPanelDescriptor = backPanelDescriptor;
    }

    // Override this method in the subclass if you wish it to be called
    // just before the panel is displayed.

    /**
     * Override this method to provide functionality that will be performed just
     * before the panel is to be displayed.
     */
    public void aboutToDisplayPanel() {

    }

    // Override this method in the subclass if you wish to do something
    // while the panel is displaying.

    /**
     * Override this method to perform functionality when the panel itself is
     * displayed.
     */
    public void displayingPanel() {

    }

    // Override this method in the subclass if you wish it to be called
    // just before the panel is switched to another or finished.

    /**
     * Override this method to perform functionality just before the panel is to
     * be hidden.
     */
    public void aboutToHidePanel() {

    }

    static class FinishIdentifier {
	public static final String ID = "FINISH";
    }

    /**
     * Returns a Component with title panel at the top and elements panel in the
     * middle.
     * 
     * @param elements
     *            The component that contains elements to be shown on the menu.
     *            If elements is null, then the message "no parameters needed"
     *            is displayed.
     * @return
     */
    public Component createPanel(Component elements) {
	final JPanel panel = new JPanel();
	panel.setLayout(new BorderLayout());

	final JLabel title = new JLabel();
	title.setText(name);
	panel.add(title, BorderLayout.NORTH);

	if (null == elements) {
	    final JPanel e = new JPanel();
	    e.setLayout(new FlowLayout());
	    final JLabel label = new JLabel(
		    "<html><p align='center'>No parameters required.</p></html>");

	    e.add(label);
	    elements = e;
	}

	// to make it scrollable
	panel.add(new JScrollPane(elements), BorderLayout.CENTER);

	return panel;
    }

    @Override
    public void update(Observable o, Object arg) {
	// FIXME: Add final page with OC metric
	if (arg instanceof Boolean) {
	    // next/finish button
	    final boolean isThisWPD = wizard.getModel()
		    .getCurrentPanelDescriptor().equals(this);

	    if (isThisWPD) {
		final boolean flag = (Boolean) arg;
		wizard.setBackButtonEnabled(flag);
		wizard.setNextFinishButtonEnabled(flag);
	    }
	} else {
	    final Object id;
	    final String title;
	    final ParametersPanel current, novel;

	    if (arg instanceof Scenario) {
		final Scenario scn = (Scenario) arg;
		id = ParametersGUI.SCENARIO;
		title = String.format("Scenario: %s", scn);
		current = wizard.getModel().getPanelDescriptor(id)
			.getIParametersPanel();
		novel = scn.getParametersPanel();
	    } else if (arg instanceof TrustModel) {
		final TrustModel<?> tm = (TrustModel<?>) arg;
		id = ParametersGUI.MODELS;
		title = String.format("Trust model: %s", tm);
		current = wizard.getModel().getPanelDescriptor(id)
			.getIParametersPanel();
		novel = tm.getParametersPanel();
	    } else if (arg instanceof Accuracy) {
		final Accuracy rm = (Accuracy) arg;
		id = ParametersGUI.ACCURACY_METRIC;
		title = String.format("Accuracy: %s", rm);
		current = wizard.getModel().getPanelDescriptor(id)
			.getIParametersPanel();
		novel = rm.getParametersPanel();
	    } else if (arg instanceof Utility) {
		final Utility um = (Utility) arg;
		id = ParametersGUI.UTILITY_METRIC;
		title = String.format("Utility: %s", um);
		current = wizard.getModel().getPanelDescriptor(id)
			.getIParametersPanel();
		novel = um.getParametersPanel();
	    } else if (arg instanceof OpinionCost) {
		final OpinionCost ocm = (OpinionCost) arg;
		id = ParametersGUI.OPINIONCOST_METRIC;
		title = String.format("Opinion cost: %s", ocm);
		current = wizard.getModel().getPanelDescriptor(id)
			.getIParametersPanel();
		novel = ocm.getParametersPanel();
	    } else {
		return;
	    }

	    // if new selection is the same as old selection
	    if (null != current && null != novel
		    && current.getClass() == novel.getClass()) {
		return;
	    }

	    // create new WPD
	    final WizardPanelDescriptor wpd = new WizardPanelDescriptor(id,
		    novel, title);

	    // register new WPD
	    wizard.registerWizardPanel(wpd);

	    // initialize WPD
	    if (null != novel)
		novel.initialize(wpd, wizard.getClassLoader());

	    // set next/previous
	    if (arg instanceof Scenario) {
		wpd.setBack(ParametersGUI.MAIN);
		wpd.setNext(ParametersGUI.MODELS);
	    } else if (arg instanceof TrustModel) {
		wpd.setBack(ParametersGUI.SCENARIO);
		wpd.setNext(ParametersGUI.ACCURACY_METRIC);
	    } else if (arg instanceof Accuracy) {
		wpd.setBack(ParametersGUI.MODELS);
	    } else if (arg instanceof Utility) {
		// ACCURACY -> UTILITY
		wizard.getModel()
			.getPanelDescriptor(ParametersGUI.ACCURACY_METRIC)
			.setNext(ParametersGUI.UTILITY_METRIC);
		// ACCURACY <- UTILITY
		wpd.setBack(ParametersGUI.ACCURACY_METRIC);
	    } else {
		/* OPINION COST */

		// ACCURACY -> UTILITY
		wizard.getModel()
			.getPanelDescriptor(ParametersGUI.ACCURACY_METRIC)
			.setNext(ParametersGUI.UTILITY_METRIC);

		// UTILITY -> OPINION COST
		wizard.getModel()
			.getPanelDescriptor(ParametersGUI.UTILITY_METRIC)
			.setNext(ParametersGUI.OPINIONCOST_METRIC);

		// UTILITY <- OPINION COST
		wpd.setBack(ParametersGUI.UTILITY_METRIC);
	    }
	}
    }
}
