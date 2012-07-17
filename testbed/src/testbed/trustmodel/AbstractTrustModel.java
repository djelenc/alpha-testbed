package testbed.trustmodel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;

import testbed.common.Tuple;
import testbed.interfaces.IParametersPanel;
import testbed.interfaces.ITrustModel;

public abstract class AbstractTrustModel implements ITrustModel {

    @Override
    public String getName() {
	return getClass().getSimpleName();
    }

    @Override
    public String toString() {
	return getName();
    }

    @Override
    public IParametersPanel getParametersPanel() {
	return null;
    }

    /**
     * Constructs a map of rankings from a given rank of floating poitn
     * estimations. The estimations must lie on [0,1]. The highest estimation
     * gets rank 1.
     * 
     * <p>
     * The algorith assigns the same rank to agents, who have the same
     * estimations. The algorithm uses a <a href=
     * 'http://en.wikipedia.org/w/index.php?title=Ranking&oldid=460628427'>dense
     * ranking strategy</a>, which means that in case of ties, algorithm does
     * not omit ranks. For instance, a 1223 is a valid result, while 1224 is
     * not.
     * 
     * <p>
     * The implementation uses a priority heap, thus it performs in O(n*log(n)).
     * 
     * @param estimations
     *            A map of estimations, where keys represent agents and mapped
     *            values their estimations in floating points.
     * @return A map of rankings, where keys represent agents and mapped values
     *         their rankings.
     */
    public Map<Integer, Integer> constructRankingsFromEstimations(
	    Map<Integer, Double> estimations) {
	PriorityQueue<Tuple<Integer, Double>> pq = new PriorityQueue<Tuple<Integer, Double>>();

	for (Map.Entry<Integer, Double> e : estimations.entrySet()) {
	    pq.add(new Tuple<Integer, Double>(e.getKey(), 1 - e.getValue()));
	}

	Map<Integer, Integer> rankings = new LinkedHashMap<Integer, Integer>();

	double previous = Double.NaN;
	int rank = 0, agent;
	Tuple<Integer, Double> tuple;

	while (!pq.isEmpty()) {
	    tuple = pq.poll();
	    agent = tuple.first;

	    if (tuple.second != previous) {
		rank++;
		previous = tuple.second;
	    }

	    rankings.put(agent, rank);
	}

	return rankings;
    }
}
