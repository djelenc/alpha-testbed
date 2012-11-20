package testbed.repast;

import java.util.HashMap;
import java.util.Map;

import testbed.DecisionsModeA;
import testbed.EvaluationProtocol;
import testbed.MetricSubscriber;
import testbed.common.DefaultRandomGenerator;
import testbed.interfaces.Metric;
import testbed.interfaces.Scenario;
import testbed.interfaces.TrustModel;
import testbed.metric.CumulativeNormalizedUtility;
import testbed.metric.KendallsTauA;
import testbed.scenario.TransitiveInteractionPartnerSelection;
import testbed.trustmodel.YuSinghSycaraSelectingInteractionPartners;

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
	TrustModel<?> model = new YuSinghSycaraSelectingInteractionPartners();
	model.setRandomGenerator(new DefaultRandomGenerator(0));
	model.initialize();

	// scenario
	Scenario scenario = new TransitiveInteractionPartnerSelection();
	scenario.setRandomGenerator(new DefaultRandomGenerator(0));
	scenario.initialize(100, 0.05, 0.1, 1d, 1d);

	// metrics
	Metric accuracy = new KendallsTauA();
	Metric utility = new CumulativeNormalizedUtility();

	Map<Metric, Object[]> metrics = new HashMap<Metric, Object[]>();
	metrics.put(accuracy, null);
	metrics.put(utility, null);

	EvaluationProtocol ep = new DecisionsModeA();
	ep.initialize(model, scenario, metrics);

	ep.subscribe(new ProgramaticRunExample(accuracy));
	ep.subscribe(new ProgramaticRunExample(utility));

	for (int time = 1; time <= 100; time++) {
	    ep.step(time);
	}
    }

    @Override
    public void update(EvaluationProtocol instance) {
	System.out.printf("%s (%s): %.2f\n", metric, service,
		instance.getResult(service, metric));
    }
}