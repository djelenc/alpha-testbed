package testbed.trustmodel.qad;

import static testbed.trustmodel.qad.Omega.fromNumeric;
import static testbed.trustmodel.qad.Omega.normalizedNumeric;

import java.util.Map;
import java.util.Set;

import testbed.common.Utils;
import testbed.interfaces.Experience;
import testbed.interfaces.ICondition;
import testbed.interfaces.IParametersPanel;
import testbed.interfaces.ITrustModel;
import testbed.interfaces.Opinion;
import testbed.trustmodel.AbstractTrustModel;

/**
 * Qualitative assessment dynamics
 * 
 * @author David
 * 
 */
public class QAD extends AbstractTrustModel implements ITrustModel {
    // matrix for other agents
    public Omega[][] op;

    // Alpha's row
    public Omega[] row;

    // operator
    public Operator operator;

    @Override
    public void initialize(Object... params) {
	op = new Omega[0][0];
	row = new Omega[0];

	final ICondition<Operator> validator = new ICondition<Operator>() {
	    @Override
	    public void eval(Operator var) {

	    }
	};

	operator = Utils.extractParameter(validator, 0, params);
    }

    @Override
    public void calculateTrust(Set<Experience> exps, Set<Opinion> opinions) {
	expandArray(exps, opinions);

	for (Opinion o : opinions)
	    op[o.agent1][o.agent2] = normalizedNumeric(o.internalTrustDegree);

	for (Experience e : exps)
	    row[e.agent] = normalizedNumeric(e.outcome);
    }

    public Map<Integer, Double> compute(int service) {
	Map<Integer, Double> newTrust = operator.compute(row, op);

	// now update Alpha's row
	for (Map.Entry<Integer, Double> e : newTrust.entrySet())
	    row[e.getKey()] = fromNumeric(e.getValue());

	return newTrust;
    }

    @Override
    public Map<Integer, Integer> getRankings(int service) {
	return constructRankingsFromEstimations(compute(service));
    }

    @Override
    public String getName() {
	return "QAD";
    }

    @Override
    public void setCurrentTime(int time) {
    }

    private void expandArray(Set<Experience> experiences, Set<Opinion> opinions) {
	int max = Math.max(op.length - 1, row.length - 1);

	for (Experience e : experiences)
	    if (e.agent > max)
		max = e.agent;

	for (Opinion o : opinions)
	    if (o.agent2 > max || o.agent1 > max)
		max = Math.max(o.agent1, o.agent2);

	// resize arrays
	if (max > op.length - 1 || max > row.length - 1) {
	    Omega[][] newOp = new Omega[max + 1][max + 1];

	    for (int i = 0; i < op.length; i++)
		System.arraycopy(op[i], 0, newOp[i], 0, op.length);

	    op = newOp;

	    Omega[] newExp = new Omega[max + 1];
	    System.arraycopy(row, 0, newExp, 0, row.length);
	    row = newExp;
	}
    }

    @Override
    public IParametersPanel getParametersPanel() {
	return new QADGUI();
    }
}
