package testbed.repast;

import testbed.AlphaTestbed;
import testbed.MetricSubscriber;
import testbed.common.DefaultRandomGenerator;
import testbed.interfaces.Accuracy;
import testbed.interfaces.Metric;
import testbed.interfaces.Scenario;
import testbed.interfaces.TrustModel;
import testbed.metric.KendallsTauA;
import testbed.scenario.Transitive;
import testbed.trustmodel.YuSinghSycara;

/**
 * A class that demonstrates how an evaluation can be run as a simple Java
 * program.
 * 
 * @author David
 * 
 */
public class ProgramaticRunExample implements MetricSubscriber {

    private final int service;
    private final Metric metric;

    public ProgramaticRunExample(Metric m) {
	service = 0;
	metric = m;
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
	Accuracy accuracy = new KendallsTauA();
	accuracy.initialize();

	// alpha test-bed (utility metric set to null)
	final AlphaTestbed atb = new AlphaTestbed(scenario, model, accuracy,
		null, null, null);

	atb.subscribe(new ProgramaticRunExample(accuracy));

	for (int time = 1; time <= 300; time++)
	    atb.step(time);
    }

    @Override
    public void update(AlphaTestbed instance) {
	System.out.printf("%s (%s): %.2f\n", metric, service,
		instance.getMetric(service, metric));
    }
}