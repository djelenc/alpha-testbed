package testbed.metric;

import java.util.Map;

import testbed.common.Utils;
import testbed.interfaces.ParameterCondition;
import testbed.interfaces.ParametersPanel;

/**
 * 
 * A pair-wise evaluation of the rankings that evaluates only those pairs of
 * agents, in which at least one agent has capability in the given interval.
 * 
 * @author David
 * 
 */
public class KTABounded extends OldAccuracy {
    protected Double lower, upper;

    @Override
    public void initialize(Object... params) {
	ParameterCondition<Double> validator = new ParameterCondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var > 1 || var < 0) {
		    throw new IllegalArgumentException(String.format(
			    "A bound has be within [0, 1], but was %.2f.", var));
		}
	    }
	};

	lower = Utils.extractParameter(validator, 0, params);
	upper = Utils.extractParameter(validator, 1, params);

	if (lower >= upper) {
	    throw new IllegalArgumentException(
		    String.format(
			    "The lower bound must be lower than the upper bound: lower = %.2f, upper = %.2f.",
			    lower, upper));
	}
    }

    @Override
    public <T extends Comparable<T>> double evaluate(Map<Integer, T> trust,
	    Map<Integer, Double> capabilities) {
	int result = 0, cmpCount = 0;

	for (Map.Entry<Integer, T> trust1 : trust.entrySet()) {
	    for (Map.Entry<Integer, T> trust2 : trust.entrySet()) {
		if (!trust1.equals(trust2)) {
		    final T r1 = trust1.getValue();
		    final T r2 = trust2.getValue();
		    final Double c1 = capabilities.get(trust1.getKey());
		    final Double c2 = capabilities.get(trust2.getKey());

		    if ((lower < c1 && c1 < upper)
			    || (lower < c2 && c2 < upper)) {
			result += evaluatePair(r1, r2, c1, c2);
			cmpCount += 1;
		    }
		}
	    }
	}

	return ((double) result) / cmpCount;
    }

    @Override
    public String toString() {
	return "KTA with bounds";
    }

    @Override
    public ParametersPanel getParametersPanel() {
	return new KTABoundedGUI();
    }
}
