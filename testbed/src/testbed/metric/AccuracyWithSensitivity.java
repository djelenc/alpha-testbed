package testbed.metric;

import java.util.Map;

import testbed.common.Utils;
import testbed.interfaces.ICondition;

public class AccuracyWithSensitivity extends Accuracy {
    protected double sensitivity = 0;

    @Override
    public void initialize(Object... params) {
	ICondition<Double> validator = new ICondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var > 1 || var < 0) {
		    throw new IllegalArgumentException(String.format(
			    "Sensitivity must be within [0, 1], but was %.2f.",
			    var));
		}
	    }
	};

	sensitivity = Utils.extractParameter(validator, 0, params);
    }

    @Override
    public double evaluate(Map<Integer, Integer> rankings,
	    Map<Integer, Double> capabilities) {
	if (rankings.size() == 0) {
	    return 0;
	} else if (rankings.size() == 1) {
	    return 1;
	}

	int result = 0, cmpCount = 0, r1, r2;
	double c1, c2;

	for (Map.Entry<Integer, Integer> rank1 : rankings.entrySet()) {
	    for (Map.Entry<Integer, Integer> rank2 : rankings.entrySet()) {
		if (!rank1.equals(rank2)) {
		    r1 = rank1.getValue();
		    r2 = rank2.getValue();
		    c1 = capabilities.get(rank1.getKey());
		    c2 = capabilities.get(rank2.getKey());

		    if (Math.abs(c1 - c2) >= this.sensitivity) {
			result += super.evaluatePair(r1, r2, c1, c2);
			cmpCount += 1;
		    }
		}
	    }
	}

	return ((double) result) / cmpCount;
    }

    @Override
    public String getName() {
	return "Accuracy with sensitivity";
    }
}
