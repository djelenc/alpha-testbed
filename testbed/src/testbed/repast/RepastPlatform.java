package testbed.repast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import testbed.common.DefaultRandomGenerator;
import testbed.core.AlphaTestbed;
import testbed.core.EvaluationProtocol;
import testbed.gui.ExceptionWindowDialog;
import testbed.gui.ParametersGUI;
import testbed.interfaces.Accuracy;
import testbed.interfaces.Metric;
import testbed.interfaces.OpinionCost;
import testbed.interfaces.RandomGenerator;
import testbed.interfaces.Scenario;
import testbed.interfaces.TrustModel;
import testbed.interfaces.Utility;

public class RepastPlatform extends DefaultContext<Object> implements
	ContextBuilder<Object> {
    private static ParametersGUI gui;

    private EvaluationProtocol testbed;

    @Override
    public Context<Object> build(Context<Object> context) {
	try {
	    context.setId("testbed");

	    final ClassLoader cl = ContextBuilder.class.getClassLoader();
	    final boolean isBatch = RunEnvironment.getInstance().isBatch();

	    final int guiAnswer;

	    if (null == gui) {
		// first run (either single or batch)
		gui = new ParametersGUI(cl);
		gui.setBatchRun(isBatch);
		guiAnswer = gui.showDialog();
	    } else {
		// subsequent runs
		if (isBatch) {
		    // batch run -- don't show dialog
		    gui.setBatchRun(isBatch);
		    guiAnswer = 0;
		} else {
		    // single run -- show dialog
		    gui.refresh();
		    gui.setBatchRun(isBatch);
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

	    final RandomGenerator scnRnd, tmRnd;
	    scnRnd = new DefaultRandomGenerator(seed);
	    tmRnd = new DefaultRandomGenerator(seed);

	    final Object[] generalParams = gui.getSetupParameters();
	    final Object[] scenarioParams = gui.getScenarioParameters();
	    final Object[] trustModelParams = gui.getTrustModelParameters();
	    final Object[] accParams = gui.getAccuracyParameters();
	    final Object[] utilParams = gui.getUtilityParameters();
	    final Object[] ocParams = gui.getOpinionCostParameters();

	    // set scenario
	    final Scenario scenario = (Scenario) generalParams[0];
	    scenario.setRandomGenerator(scnRnd);
	    scenario.initialize(scenarioParams);

	    // set trust model
	    final TrustModel<?> trustModel = (TrustModel<?>) generalParams[1];
	    trustModel.setRandomGenerator(tmRnd);
	    trustModel.initialize(trustModelParams);

	    // Set ranking metric
	    final Accuracy acc = (Accuracy) generalParams[2];

	    // set utility metric
	    final Utility util = (Utility) generalParams[3];

	    // set opinion cost metric
	    final OpinionCost oc = (OpinionCost) generalParams[4];
	    final Map<Metric, Object[]> metrics = new HashMap<Metric, Object[]>();

	    // Accuracy
	    if (acc != null)
		metrics.put(acc, accParams);

	    // Utility
	    if (util != null)
		metrics.put(util, utilParams);

	    // Opinion cost
	    if (oc != null)
		metrics.put(oc, ocParams);

	    testbed = AlphaTestbed.getProtocol(trustModel, scenario, metrics);

	    // Create metrics for the Metric holder class
	    for (int service : scenario.getServices()) {
		for (Map.Entry<Metric, Object[]> e : metrics.entrySet()) {
		    context.add(new RepastMetricAgent(service, e.getKey(),
			    testbed));
		}
	    }

	    // for batch runs
	    if (isBatch) {
		final int duration = (Integer) generalParams[5];
		RunEnvironment.getInstance().endAt(duration);

		final StringBuffer msg = new StringBuffer();

		msg.append(String.format("Random seed: %d\n", seed));
		msg.append(String.format("Scenario: %s\n", scenario));
		msg.append(String.format("Scenario parameters: %s\n",
			Arrays.toString(scenarioParams)));
		msg.append(String.format("Trust model: %s\n", trustModel));
		msg.append(String.format("Trust model parameters: %s\n",
			Arrays.toString(trustModelParams)));
		msg.append(String.format("Metrics: %s\n", metrics.keySet()));
		msg.append('\n');

		System.out.println(msg.toString());
	    }

	    context.add(testbed);
	} catch (Exception e) {
	    handleException(e);
	    RunEnvironment.getInstance().endAt(0);
	}

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
	    testbed.step(time);
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