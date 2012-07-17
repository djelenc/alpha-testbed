package testbed.repast;

import java.util.Set;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import testbed.Simulator;
import testbed.gui.ParametersGUI;
import testbed.interfaces.IMetric;
import testbed.interfaces.IScenario;
import testbed.interfaces.ITrustModel;

public class RepastPlatform extends DefaultContext<Object> implements
	ContextBuilder<Object> {
    private static ParametersGUI gui;

    private Simulator simulator;

    @Override
    public Context<Object> build(Context<Object> context) {
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

	// Set metrics
	@SuppressWarnings("unchecked")
	Set<IMetric> metrics = (Set<IMetric>) generalSetup[2];

	// simulator
	simulator = new Simulator(model, scenario, metrics);

	MetricHolder mh = null;

	for (IMetric mtrc : metrics) {
	    mtrc.initialize(0.25); // TODO: do it in a more suitable place!

	    for (int srvc = 0; srvc < scenario.getServices().size(); srvc++) {
		mh = new MetricHolder(srvc, mtrc, simulator);
		context.add(mh);
	    }
	}

	context.add(simulator);

	// priority = 2 to ensure stepping has the highest priority,
	// that is -- at the start of every tick
	ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
	ScheduleParameters params = ScheduleParameters.createRepeating(1, 1, 2);
	schedule.schedule(params, this, "step");

	return context;
    }

    public void step() {
	simulator.step((int) RunEnvironment.getInstance().getCurrentSchedule()
		.getTickCount());
    }
}