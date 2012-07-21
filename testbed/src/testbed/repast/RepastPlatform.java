package testbed.repast;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import testbed.AlphaTestbed;
import testbed.gui.ExceptionWindowDialog;
import testbed.gui.ParametersGUI;
import testbed.interfaces.IRankingMetric;
import testbed.interfaces.IScenario;
import testbed.interfaces.ITrustModel;
import testbed.interfaces.IUtilityMetric;

public class RepastPlatform extends DefaultContext<Object> implements
	ContextBuilder<Object> {
    private static ParametersGUI gui;

    private AlphaTestbed atb;

    @Override
    public Context<Object> build(Context<Object> context) {
	try {
	    context.setId("testbed");

	    ClassLoader cl = ContextBuilder.class.getClassLoader();

	    if (null == gui) {
		gui = new ParametersGUI(cl);
	    } else {
		gui.refresh();
	    }

	    // if the user presses 'Cancel'
	    if (gui.showDialog() != 0) {
		RunEnvironment.getInstance().endAt(0);
		return context;
	    }

	    Object[] generalSetup = gui.getSetupParameters();
	    Object[] scenarioSetup = gui.getScenarioParameters();
	    Object[] trustModelSetup = gui.getTrustModelParameters();

	    // set scenario
	    IScenario scenario = (IScenario) generalSetup[1];
	    scenario.initialize(scenarioSetup);

	    // set trust model
	    ITrustModel model = (ITrustModel) generalSetup[0];
	    model.initialize(trustModelSetup);

	    // FIXME: Once I implement GUI parameters for metric this is where I
	    // should pass in their arguments and initialize the metrics

	    // Set ranking metric
	    IRankingMetric rankingMetric = (IRankingMetric) generalSetup[2];
	    rankingMetric.initialize(0.25); // TODO

	    // set utility metric
	    IUtilityMetric utilityMetric = (IUtilityMetric) generalSetup[3];
	    utilityMetric.initialize();

	    // simulator
	    atb = new AlphaTestbed(model, scenario, rankingMetric,
		    utilityMetric);

	    // Create metrics for the Metric holder class
	    for (int service : scenario.getServices()) {
		context.add(new MetricHolder(service, rankingMetric, atb));

		if (atb.isUtilityMode())
		    context.add(new MetricHolder(service, utilityMetric, atb));
	    }
	    context.add(atb);
	} catch (Exception e) {
	    if (0 != handleException(e)) {
		RunEnvironment.getInstance().endAt(0);
	    }
	}

	// priority = 2 to ensure stepping has the highest priority
	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
	ScheduleParameters params = ScheduleParameters.createRepeating(1, 1, 2);
	schedule.schedule(params, this, "step");

	return context;
    }

    public void step() {
	final int time;

	try {
	    time = (int) RunEnvironment.getInstance().getCurrentSchedule()
		    .getTickCount();
	    atb.step(time);
	} catch (Exception e) {
	    if (0 != handleException(e)) {
		RunEnvironment.getInstance().endRun();
	    }
	}
    }

    private int handleException(Exception e) {
	final String title = e.getMessage();
	final StringBuffer sb = new StringBuffer();
	sb.append("== Stack Trace ==\n\n");

	for (StackTraceElement ste : e.getStackTrace())
	    sb.append(ste.toString() + "\n");

	ExceptionWindowDialog wd = new ExceptionWindowDialog(title,
		sb.toString());

	return wd.showNotification();
    }
}