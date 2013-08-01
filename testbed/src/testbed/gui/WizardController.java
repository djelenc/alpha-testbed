/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Robert Eckstein - initial API and implementation
 *        http://www.oracle.com/technetwork/articles/javase/wizard-136789.html
 *     David Jelenc - adaptation and modification for ATB
 ******************************************************************************/
package testbed.gui;

import java.awt.event.ActionListener;

/**
 * This class is responsible for reacting to events generated by pushing any of
 * the three buttons, 'Next', 'Previous', and 'Cancel.' Based on what button is
 * pressed, the controller will update the model to show a new panel and reset
 * the state of the buttons as necessary.
 */
public class WizardController implements ActionListener {

    private Wizard wizard;

    /**
     * This constructor accepts a reference to the Wizard component that created
     * it, which it uses to update the button components and access the
     * WizardModel.
     * 
     * @param w
     *            A callback to the Wizard component that created this
     *            controller.
     */
    public WizardController(Wizard w) {
	wizard = w;
    }

    /**
     * Calling method for the action listener interface. This class listens for
     * actions performed by the buttons in the Wizard class, and calls methods
     * below to determine the correct course of action.
     * 
     * @param evt
     *            The ActionEvent that occurred.
     */
    public void actionPerformed(java.awt.event.ActionEvent evt) {

	if (evt.getActionCommand().equals(Wizard.CANCEL_BUTTON_ACTION_COMMAND))
	    cancelButtonPressed();
	else if (evt.getActionCommand().equals(
		Wizard.BACK_BUTTON_ACTION_COMMAND))
	    backButtonPressed();
	else if (evt.getActionCommand().equals(
		Wizard.NEXT_BUTTON_ACTION_COMMAND))
	    nextButtonPressed();

    }

    private void cancelButtonPressed() {

	wizard.close(Wizard.CANCEL_RETURN_CODE);
    }

    private void nextButtonPressed() {

	WizardModel model = wizard.getModel();
	WizardPanelDescriptor descriptor = model.getCurrentPanelDescriptor();

	// If it is a finishable panel, close down the dialog. Otherwise,
	// get the ID that the current panel identifies as the next panel,
	// and display it.

	Object nextPanelDescriptor = descriptor.getNext();

	if (nextPanelDescriptor instanceof WizardPanelDescriptor.FinishIdentifier) {
	    wizard.close(Wizard.FINISH_RETURN_CODE);
	} else {
	    wizard.setCurrentPanel(nextPanelDescriptor);
	}

    }

    private void backButtonPressed() {

	WizardModel model = wizard.getModel();
	WizardPanelDescriptor descriptor = model.getCurrentPanelDescriptor();

	// Get the descriptor that the current panel identifies as the previous
	// panel, and display it.

	Object backPanelDescriptor = descriptor.getBack();
	wizard.setCurrentPanel(backPanelDescriptor);

    }

    void resetButtonsToPanelRules() {

	// Reset the buttons to support the original panel rules,
	// including whether the next or back buttons are enabled or
	// disabled, or if the panel is finishable.

	WizardModel model = wizard.getModel();
	WizardPanelDescriptor descriptor = model.getCurrentPanelDescriptor();

	model.setCancelButtonText(Wizard.CANCEL_TEXT);

	// If the panel in question has another panel behind it, enable
	// the back button. Otherwise, disable it.

	model.setBackButtonText(Wizard.BACK_TEXT);

	if (descriptor.getBack() != null)
	    model.setBackButtonEnabled(Boolean.TRUE);
	else
	    model.setBackButtonEnabled(Boolean.FALSE);

	// If the panel in question has one or more panels in front of it,
	// enable the next button. Otherwise, disable it.

	if (descriptor.getNext() != null)
	    model.setNextFinishButtonEnabled(Boolean.TRUE);
	else
	    model.setNextFinishButtonEnabled(Boolean.FALSE);

	// If the panel in question is the last panel in the series, change
	// the Next button to Finish. Otherwise, set the text back to Next.

	if (descriptor.getNext() instanceof WizardPanelDescriptor.FinishIdentifier) {
	    model.setNextFinishButtonText(Wizard.FINISH_TEXT);
	} else {
	    model.setNextFinishButtonText(Wizard.NEXT_TEXT);
	}

    }

}
