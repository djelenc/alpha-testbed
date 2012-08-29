package testbed.metric;

import java.util.Map;

import testbed.common.Utils;
import testbed.interfaces.ICondition;
import testbed.interfaces.IParametersPanel;

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
    public <T extends Comparable<T>> double evaluate(Map<Integer, T> trust,
	    Map<Integer, Double> capabilities) {
	if (trust.size() == 0) {
	    return 0;
	} else if (trust.size() == 1) {
	    return 1;
	}

	int result = 0, cmpCount = 0;

	for (Map.Entry<Integer, T> trust1 : trust.entrySet()) {
	    for (Map.Entry<Integer, T> trust2 : trust.entrySet()) {
		if (!trust1.equals(trust2)) {
		    final T t1 = trust1.getValue();
		    final T t2 = trust2.getValue();
		    final Double c1 = capabilities.get(trust1.getKey());
		    final Double c2 = capabilities.get(trust2.getKey());

		    if (Math.abs(c1 - c2) >= sensitivity) {
			result += evaluatePair(t1, t2, c1, c2);
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

    @Override
    public IParametersPanel getParametersPanel() {
	return new AccuracyWithSensitivityGUI();
    }
}
