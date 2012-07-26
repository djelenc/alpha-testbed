package testbed.repast;

import java.util.Arrays;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import testbed.AlphaTestbed;
import testbed.common.DefaultRandomGenerator;
import testbed.gui.ExceptionWindowDialog;
import testbed.gui.ParametersGUI;
import testbed.interfaces.IRandomGenerator;
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

	    final ClassLoader cl = ContextBuilder.class.getClassLoader();
	    final boolean batchRun = RunEnvironment.getInstance().isBatch();

	    final int guiAnswer;

	    if (null == gui) {
		// first run (either single or batch)
		gui = new ParametersGUI(cl);
		gui.setBatchRun(batchRun);
		guiAnswer = gui.showDialog();
	    } else {
		// subsequent runs
		if (batchRun) {
		    // batch run -- don't show dialog
		    gui.setBatchRun(batchRun); // not sure
		    guiAnswer = 0;
		} else {
		    // single run -- show dialog
		    gui.refresh();
		    gui.setBatchRun(batchRun);
		    guiAnswer = gui.showDialog();
		}
	    }

	    // if user pressed 'Cancel'
	    if (guiAnswer != 0) {
		RunEnvironment.getInstance().endAt(0);
		return context;
	    }

	    // get parameter seed
	    final int seed = (Integer) RunEnvironment.getInstance()
		    .getParameters().getValue("randomSeed");

	    final IRandomGenerator scnRnd, tmRnd;
	    scnRnd = new DefaultRandomGenerator(seed);
	    tmRnd = new DefaultRandomGenerator(seed);

	    final Object[] generalParams = gui.getSetupParameters();
	    final Object[] scenarioParams = gui.getScenarioParameters();
	    final Object[] trustModelParams = gui.getTrustModelParameters();

	    // set scenario
	    final IScenario scenario = (IScenario) generalParams[0];
	    scenario.setRandomGenerator(scnRnd);
	    scenario.initialize(scenarioParams);

	    // set trust model
	    final ITrustModel trustModel = (ITrustModel) generalParams[1];
	    trustModel.setRandomGenerator(tmRnd);
	    trustModel.initialize(trustModelParams);

	    // FIXME: Once I implement GUI parameters for metric this is where I
	    // should pass in their arguments and initialize the metrics

	    // Set ranking metric
	    final IRankingMetric rm = (IRankingMetric) generalParams[2];
	    rm.initialize(0.25); // TODO

	    // set utility metric
	    final IUtilityMetric um = (IUtilityMetric) generalParams[3];
	    um.initialize();

	    // simulator
	    atb = new AlphaTestbed(scenario, trustModel, rm, um);

	    // Create metrics for the Metric holder class
	    for (int service : scenario.getServices()) {
		context.add(new RepastMetricAgent(service, rm, atb));

		if (atb.isUtilityMode())
		    context.add(new RepastMetricAgent(service, um, atb));
	    }

	    // for batch runs
	    if (batchRun) {
		final int duration = (Integer) generalParams[4];
		RunEnvironment.getInstance().endAt(duration);

		final StringBuffer msg = new StringBuffer();

		msg.append(String.format("Random seed: %d\n", seed));
		msg.append(String.format("Scenario: %s\n", scenario));
		msg.append(String.format("Scenario parameters: %s\n",
			Arrays.toString(scenarioParams)));
		msg.append(String.format("Trust model: %s\n", trustModel));
		msg.append(String.format("Trust model parameters: %s\n",
			Arrays.toString(trustModelParams)));
		msg.append(String.format("Ranking metric: %s\n", rm));

		if (atb.isUtilityMode()) {
		    msg.append(String.format("Utility metric: %s\n", um));
		}

		msg.append('\n');

		System.out.println(msg.toString());
	    }

	    context.add(atb);
	} catch (Exception e) {
	    handleException(e);
	    RunEnvironment.getInstance().endAt(0);
	}

	// priority = 2 to ensure stepping has the highest priority
	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
	ScheduleParameters params = ScheduleParameters.createRepeating(1, 1, 1);
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

    public int handleException(Exception e) {
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