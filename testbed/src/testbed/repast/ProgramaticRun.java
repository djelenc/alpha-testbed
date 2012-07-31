package testbed.repast;

import testbed.AlphaTestbed;
import testbed.IMetricSubscriber;
import testbed.common.DefaultRandomGenerator;
import testbed.interfaces.IMetric;
import testbed.interfaces.IRankingMetric;
import testbed.interfaces.IScenario;
import testbed.interfaces.ITrustModel;
import testbed.metric.Accuracy;
import testbed.scenario.Transitive;
import testbed.trustmodel.YuSinghSycara;

/**
 * A class that demonstrates how an evaluation can be run as a simple Java
 * program.
 * 
 * @author David
 * 
 */
public class ProgramaticRun implements IMetricSubscriber {

    private final int service;
    private final IMetric metric;

    public ProgramaticRun() {
	service = 0;
	metric = new Accuracy();
    }

    public static void main(String[] args) {
	// trust model
	ITrustModel model = new YuSinghSycara();
	model.setRandomGenerator(new DefaultRandomGenerator(0));
	model.initialize();

	// scenario
	IScenario scenario = new Transitive();
	scenario.setRandomGenerator(new DefaultRandomGenerator(0));
	scenario.initialize(100, 0.05, 0.1, 1d, 1d);

	// ranking metric
	IRankingMetric rm = new Accuracy();
	rm.initialize();

	// alpha test-bed (utility metric in set to null)
	final AlphaTestbed atb = new AlphaTestbed(scenario, model, rm, null,
		null, null);

	atb.subscribe(new ProgramaticRun());

	for (int time = 1; time <= 300; time++)
	    atb.step(time);
    }

    @Override
    public void update(AlphaTestbed instance) {
	System.out.println(instance.getMetric(service, metric));
    }
}