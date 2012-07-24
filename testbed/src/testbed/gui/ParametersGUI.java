package testbed.gui;

import java.util.Arrays;

import testbed.interfaces.IParametersPanel;

public class ParametersGUI {
    public static final String MAIN = "main";
    public static final String METRICS = "metrics";
    public static final String SCENARIO = "scenario";
    public static final String MODELS = "models";

    private static final Object[] EMPTY = new Object[0];

    private Wizard wizard;
    private WizardPanelDescriptor mainWpd;

    public ParametersGUI(ClassLoader cl) {
	wizard = new Wizard();
	wizard.getDialog().setTitle("Evaluation setup");
	wizard.setClassLoader(cl);

	final IParametersPanel main;
	final WizardPanelDescriptor scenarioWpd, metricsWpd;

	main = new MainPanel();
	mainWpd = new WizardPanelDescriptor(MAIN, main, "General parameters");

	scenarioWpd = new WizardPanelDescriptor(SCENARIO);
	metricsWpd = new WizardPanelDescriptor(MODELS);

	wizard.registerWizardPanel(mainWpd);
	wizard.registerWizardPanel(scenarioWpd);
	wizard.registerWizardPanel(metricsWpd);

	mainWpd.setNext(scenarioWpd.getID());
	scenarioWpd.setBack(mainWpd.getID());
	scenarioWpd.setNext(metricsWpd.getID());
	metricsWpd.setBack(scenarioWpd.getID());

	main.initialize(mainWpd, cl);
	wizard.setCurrentPanel(mainWpd.getID());
    }

    // sets the wizard to the starting panel
    // while preserving the selected values
    public void refresh() {
	wizard.setCurrentPanel(mainWpd.getID());
    }

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

    public Object[] getMetricsParameters() {
	throw new UnsupportedOperationException("Not implemented yet!");
    }

    public Object[] getTrustModelParameters() {
	return getParameters(MODELS);
    }

    private Object[] getParameters(String str) {
	final IParametersPanel ppanel = wizard.getModel()
		.getPanelDescriptor(str).getIParametersPanel();
	if (null == ppanel) {
	    return EMPTY;
	} else {
	    return ppanel.getParameters();
	}
    }

    public static void main(String[] args) {
	ParametersGUI sim = new ParametersGUI(Thread.currentThread()
		.getContextClassLoader());
	int code = sim.showDialog();

	System.out.printf("Return code: %d\n", code);
	System.out.printf("General parameters: %s\n",
		Arrays.toString(sim.getSetupParameters()));
	System.out.printf("Scenario parameters: %s\n",
		Arrays.toString(sim.getScenarioParameters()));
	System.out.printf("Trust model parameters: %s\n",
		Arrays.toString(sim.getTrustModelParameters()));
    }
}
