package testbed.repast;

import testbed.AlphaTestbed;
import testbed.MetricSubscriber;
import testbed.common.DefaultRandomGenerator;
import testbed.interfaces.Metric;
import testbed.interfaces.RankingMetric;
import testbed.interfaces.Scenario;
import testbed.interfaces.TrustModel;
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
public class ProgramaticRun implements MetricSubscriber {

    private final int service;
    private final Metric metric;

    public ProgramaticRun() {
	service = 0;
	metric = new Accuracy();
    }

    public static void main(String[] args) {
	// trust model
	TrustModel<?> model = new YuSinghSycara();
	model.setRandomGenerator(new DefaultRandomGenerator(0));
	model.initialize();

	// scenario
	Scenario scenario = new Transitive();
	scenario.setRandomGenerator(new DefaultRandomGenerator(0));
	scenario.initialize(100, 0.05, 0.1, 1d, 1d);

	// ranking metric
	RankingMetric rm = new Accuracy();
	rm.initialize();

	// alpha test-bed (utility metric set to null)
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