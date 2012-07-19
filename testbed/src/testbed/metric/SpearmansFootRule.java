package testbed.metric;

import java.util.Map;

import testbed.interfaces.IRankingMetric;
import testbed.trustmodel.OnlyExperiences;

/**
 * Spearman's foot rule metric.
 * 
 * <p>
 * TODO: <font color='red'>
 * <ul>
 * <li>Implementation is not finished yet. What happens if rankings and
 * capabilities (the parameters of evaluate method) have different size?
 * <li>How should I handle the ties?
 * </ul>
 * </font>
 * 
 * @author david
 * 
 */
public class SpearmansFootRule extends AbstractMetric implements IRankingMetric {

    private static OnlyExperiences tm;

    @Override
    public void initialize(Object... params) {
	tm = new OnlyExperiences();
    }

    @Override
    public double evaluate(Map<Integer, Integer> rankings,
	    Map<Integer, Double> capabilities) {
	if (rankings.size() == 0) {
	    return 0;
	} else if (rankings.size() == 1) {
	    return 1;
	}

	Map<Integer, Integer> tr = tm
		.constructRankingsFromEstimations(capabilities);

	int result = 0, tmR, cR, id;

	// This formula gives the maximum distance between two rankings
	int k = tr.size();
	double t = Math.ceil((k + 1) / 2d);
	double normalization = 2 * (-t * t + k * t + 2 * t - k - 1);

	for (Map.Entry<Integer, Integer> rank : rankings.entrySet()) {
	    id = rank.getKey();
	    tmR = rank.getValue(); // TM rank
	    cR = tr.get(id); // capability rank

	    result += Math.abs(tmR - cR);
	}

	return 1 - result / normalization;
    }
}
