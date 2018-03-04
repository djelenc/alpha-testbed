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

import testbed.interfaces.ParametersPanel;

import java.util.Arrays;

public class ParametersGUI {
    public static final String MAIN = "main";
    public static final String ACCURACY_METRIC = "accuracy_metric";
    public static final String UTILITY_METRIC = "utility_metric";
    public static final String OPINIONCOST_METRIC = "opinioncost_metric";
    public static final String SCENARIO = "scenario";
    public static final String MODELS = "model";

    private static final Object[] EMPTY = new Object[0];

    private MainPanel mainPanel;
    private Wizard wizard;
    private WizardPanelDescriptor mainWpd;

    public ParametersGUI(ClassLoader cl) {
        wizard = new Wizard();
        wizard.getDialog().setTitle("Evaluation setup");
        wizard.setClassLoader(cl);

        final WizardPanelDescriptor scenarioWpd, modelWpd, accuracyWpd,
                utilityWpd, opinioncostWpd;

        mainPanel = new MainPanel();
        mainWpd = new WizardPanelDescriptor(MAIN, mainPanel,
                "General parameters");

        scenarioWpd = new WizardPanelDescriptor(SCENARIO);
        modelWpd = new WizardPanelDescriptor(MODELS);
        accuracyWpd = new WizardPanelDescriptor(ACCURACY_METRIC);
        utilityWpd = new WizardPanelDescriptor(UTILITY_METRIC);
        opinioncostWpd = new WizardPanelDescriptor(OPINIONCOST_METRIC);

        wizard.registerWizardPanel(mainWpd);
        wizard.registerWizardPanel(scenarioWpd);
        wizard.registerWizardPanel(modelWpd);
        wizard.registerWizardPanel(accuracyWpd);
        wizard.registerWizardPanel(utilityWpd);
        wizard.registerWizardPanel(opinioncostWpd);

        mainWpd.setNext(scenarioWpd.getID());
        scenarioWpd.setBack(mainWpd.getID());
        scenarioWpd.setNext(modelWpd.getID());
        modelWpd.setBack(scenarioWpd.getID());
        modelWpd.setNext(accuracyWpd.getID());
        accuracyWpd.setBack(modelWpd.getID());
        accuracyWpd.setNext(utilityWpd.getID());
        utilityWpd.setBack(accuracyWpd.getID());
        utilityWpd.setNext(opinioncostWpd.getID());
        opinioncostWpd.setBack(utilityWpd.getID());

        mainPanel.initialize(mainWpd, cl);
        wizard.setCurrentPanel(mainWpd.getID());
    }

    public static void main(String[] args) {
        ParametersGUI sim = new ParametersGUI(
                Thread.currentThread().getContextClassLoader());
        int code = sim.showDialog();

        System.out.printf("Return code: %d\n", code);
        System.out.printf("General parameters: %s\n",
                Arrays.toString(sim.getSetupParameters()));
        System.out.printf("Scenario parameters: %s\n",
                Arrays.toString(sim.getScenarioParameters()));
        System.out.printf("Trust model parameters: %s\n",
                Arrays.toString(sim.getTrustModelParameters()));
        System.out.printf("Accuracy parameters: %s\n",
                Arrays.toString(sim.getAccuracyParameters()));
        System.out.printf("Utility parameters: %s\n",
                Arrays.toString(sim.getUtilityParameters()));
        System.out.printf("Opinion cost parameters: %s\n",
                Arrays.toString(sim.getOpinionCostParameters()));
    }

    public void setBatchRun(boolean batch) {
        mainPanel.setBatchRun(batch);
    }

    /**
     * Sets the wizard to the starting panel, while preserving the selected
     * values
     */
    public void refresh() {
        wizard.setCurrentPanel(mainWpd.getID());
    }

    /**
     * Shows the modal dialog (blocks the application).
     *
     * @return The answer code (0 = OK, 1 = Cancel)
     */
    public int showDialog() {
        return wizard.showModalDialog();
    }

    public Object[] getSetupParameters() {
        return wizard.getModel().getPanelDescriptor(MAIN).getIParametersPanel()
                .getParameters();
    }

    public Object[] getScenarioParameters() {
        return getParameters(SCENARIO);
    }

    public Object[] getAccuracyParameters() {
        return getParameters(ACCURACY_METRIC);
    }

    public Object[] getUtilityParameters() {
        return getParameters(UTILITY_METRIC);
    }

    public Object[] getOpinionCostParameters() {
        return getParameters(OPINIONCOST_METRIC);
    }

    public Object[] getTrustModelParameters() {
        return getParameters(MODELS);
    }

    private Object[] getParameters(String str) {
        final ParametersPanel ppanel = wizard.getModel().getPanelDescriptor(str)
                .getIParametersPanel();
        if (null == ppanel) {
            return EMPTY;
        } else {
            return ppanel.getParameters();
        }
    }
}
