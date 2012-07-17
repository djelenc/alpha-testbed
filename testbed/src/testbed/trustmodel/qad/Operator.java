package testbed.trustmodel.qad;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration that represents operators for {@link QAD Qualitative Assessment
 * Dynamics}.
 * 
 * @author David
 * 
 */
public enum Operator {
    EXTREME_OPTIMIST, EXTREME_PESSIMIST, CENTRALIST, NON_CENTRALIST, MODERATE_OPTIMIST, MODERATE_PESSIMIST, STABLE;

    public Map<Integer, Double> compute(Omega[] exp, Omega[][] op) {
	Map<Integer, Double> trust = new HashMap<Integer, Double>();

	switch (this) {
	case EXTREME_OPTIMIST:
	    extremeOptimist(exp, op, trust);
	    break;
	case EXTREME_PESSIMIST:
	    extremePessimistic(exp, op, trust);
	    break;
	case CENTRALIST:
	    centralist(exp, op, trust);
	    break;
	case NON_CENTRALIST:
	    nonCentralist(exp, op, trust);
	    break;
	case MODERATE_OPTIMIST:
	    moderateOptimist(exp, op, trust);
	    break;
	case MODERATE_PESSIMIST:
	    moderatePessimist(exp, op, trust);
	    break;
	case STABLE:
	    stable(exp, op, trust);
	    break;
	default:
	    throw new IllegalArgumentException("Unknown operator: " + this);
	}

	return trust;
    }

    /**
     * Extreme pessimistic operator
     * 
     * @param exp
     * @param op
     * @param trust
     */
    public void extremePessimistic(Omega[] exp, Omega[][] op,
	    Map<Integer, Double> trust) {
	for (int agent2 = 0; agent2 < op.length; agent2++) {
	    Omega result = null;

	    for (int agent1 = 0; agent1 < op.length; agent1++) {
		final Omega omega = op[agent1][agent2];

		if (null != omega) {
		    if (null == result) {
			result = omega;
		    } else {
			if (omega.numeric < result.numeric) {
			    result = omega;
			}
		    }
		}
	    }

	    if (null != exp[agent2]) {
		if (null == result) {
		    result = exp[agent2];
		} else {
		    if (exp[agent2].numeric < result.numeric) {
			result = exp[agent2];
		    }
		}
	    }

	    if (null != result)
		trust.put(agent2, result.numeric);
	}
    }

    /**
     * Extreme optimistic operator
     * 
     * @param exp
     * @param op
     * @param trust
     */
    public void extremeOptimist(Omega[] exp, Omega[][] op,
	    Map<Integer, Double> trust) {
	for (int agent2 = 0; agent2 < op.length; agent2++) {
	    Omega result = null;

	    for (int agent1 = 0; agent1 < op.length; agent1++) {
		final Omega omega = op[agent1][agent2];

		if (null != omega) {
		    if (null == result) {
			result = omega;
		    } else {
			if (omega.numeric > result.numeric) {
			    result = omega;
			}
		    }
		}
	    }

	    if (null != exp[agent2]) {
		if (null == result) {
		    result = exp[agent2];
		} else {
		    if (exp[agent2].numeric > result.numeric) {
			result = exp[agent2];
		    }
		}
	    }

	    if (null != result)
		trust.put(agent2, result.numeric);
	}
    }

    /**
     * Centralistic operator
     * 
     * @param exp
     * @param op
     * @param trust
     */
    public void centralist(Omega[] exp, Omega[][] op,
	    Map<Integer, Double> trust) {
	for (int agent2 = 0; agent2 < op.length; agent2++) {
	    double sum = 0;
	    int count = 0;

	    for (int agent1 = 0; agent1 < op.length; agent1++) {
		final Omega omega = op[agent1][agent2];

		if (null != omega) {
		    sum += omega.numeric;
		    count += 1;
		}
	    }

	    if (null != exp[agent2]) {
		count += 1;
		sum += exp[agent2].numeric;
	    }

	    if (count > 0) {
		final double avg = sum / count;
		final Omega result;
		if (avg >= 0d) {
		    result = Omega.fromNumeric(Math.floor(avg));
		} else {
		    result = Omega.fromNumeric(Math.ceil(avg));
		}

		trust.put(agent2, result.numeric);
	    }
	}
    }

    /**
     * Non-Centralistic operator
     * 
     * @param exp
     * @param op
     * @param trust
     */
    public void nonCentralist(Omega[] exp, Omega[][] op,
	    Map<Integer, Double> trust) {
	for (int agent2 = 0; agent2 < op.length; agent2++) {
	    double sum = 0;
	    int count = 0;

	    for (int agent1 = 0; agent1 < op.length; agent1++) {
		final Omega omega = op[agent1][agent2];

		if (null != omega) {
		    sum += omega.numeric;
		    count += 1;
		}
	    }

	    if (null != exp[agent2]) {
		count += 1;
		sum += exp[agent2].numeric;
	    }

	    if (count > 0) {
		final double avg = sum / count;
		final Omega result;
		if (avg > 0d) {
		    result = Omega.fromNumeric(Math.ceil(avg));
		} else {
		    result = Omega.fromNumeric(Math.floor(avg));
		}

		trust.put(agent2, result.numeric);
	    }
	}
    }

    /**
     * Moderate optimistic operator
     * 
     * @param exp
     * @param op
     * @param trust
     */
    public void moderateOptimist(Omega[] exp, Omega[][] op,
	    Map<Integer, Double> trust) {
	for (int agent2 = 0; agent2 < op.length; agent2++) {
	    double sum = 0;
	    int count = 0;

	    for (int agent1 = 0; agent1 < op.length; agent1++) {
		final Omega omega = op[agent1][agent2];

		if (null != omega) {
		    sum += omega.numeric;
		    count += 1;
		}
	    }

	    if (null != exp[agent2]) {
		count += 1;
		sum += exp[agent2].numeric;
	    }

	    if (count > 0) {
		final double avg = sum / count;
		final Omega result = Omega.fromNumeric(Math.round(avg));

		final Omega previous;

		if (null == exp[agent2]) {
		    previous = Omega.D;
		} else {
		    previous = exp[agent2];
		}

		if (result.numeric <= previous.numeric) {
		    trust.put(agent2, previous.numeric);
		} else {
		    trust.put(agent2, previous.numeric + 1);
		}
	    }
	}
    }

    /**
     * Moderate pessimistic operator
     * 
     * @param exp
     * @param op
     * @param trust
     */
    public void moderatePessimist(Omega[] exp, Omega[][] op,
	    Map<Integer, Double> trust) {
	for (int agent2 = 0; agent2 < op.length; agent2++) {
	    double sum = 0;
	    int count = 0;

	    for (int agent1 = 0; agent1 < op.length; agent1++) {
		final Omega omega = op[agent1][agent2];

		if (null != omega) {
		    sum += omega.numeric;
		    count += 1;
		}
	    }

	    if (null != exp[agent2]) {
		count += 1;
		sum += exp[agent2].numeric;
	    }

	    if (count > 0) {
		final double avg = sum / count;
		final Omega result = Omega.fromNumeric(Math.round(avg));

		final Omega previous;

		if (null == exp[agent2]) {
		    previous = Omega.T;
		} else {
		    previous = exp[agent2];
		}

		if (result.numeric >= previous.numeric) {
		    trust.put(agent2, previous.numeric);
		} else {
		    trust.put(agent2, previous.numeric - 1);
		}
	    }
	}
    }

    /**
     * Stable operator
     * 
     * @param exp
     * @param op
     * @param trust
     */
    public void stable(Omega[] exp, Omega[][] op,
	    Map<Integer, Double> trust) {
	for (int agent2 = 0; agent2 < op.length; agent2++) {
	    final Omega value = exp[agent2];

	    if (null != value)
		trust.put(agent2, value.numeric);
	}
    }
}