package testbed.repast;

import testbed.AlphaTestbed;
import testbed.interfaces.IRankingMetric;
import testbed.interfaces.IScenario;
import testbed.interfaces.ITrustModel;
import testbed.metric.Accuracy;
import testbed.scenario.Transitive;
import testbed.trustmodel.YuSinghSycara;

public class ProgramaticRun {

    public static void main(String[] args) {
	// trust model
	ITrustModel model = new YuSinghSycara();
	model.initialize();

	// scenario
	IScenario scenario = new Transitive();
	scenario.initialize(100, 0.05, 0.1, 1d, 1d);

	// ranking metric
	IRankingMetric rm = new Accuracy();
	rm.initialize();

	// alpha test-bed (utility metric in set to null)
	final AlphaTestbed atb = new AlphaTestbed(scenario, model, rm, null);

	for (int time = 1; time <= 300; time++) {
	    atb.step(time);

	    for (int service : scenario.getServices()) {
		System.out.printf("%d: %s[%d] = %.2f\n", time, rm, service,
			atb.getMetric(0, rm));
	    }
	}
    }
}