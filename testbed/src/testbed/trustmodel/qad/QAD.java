package testbed.trustmodel.qad;

import static testbed.trustmodel.qad.Omega.normalizedNumeric;

import java.util.Map;
import java.util.Set;

import testbed.common.Utils;
import testbed.interfaces.Experience;
import testbed.interfaces.ICondition;
import testbed.interfaces.IParametersPanel;
import testbed.interfaces.IRandomGenerator;
import testbed.interfaces.ITrustModel;
import testbed.interfaces.Opinion;

/**
 * Qualitative assessment dynamics
 * 
 * @author David
 * 
 */
public class QAD implements ITrustModel<Omega> {
    // matrix for other agents
    public Omega[][] op;

    // Alpha's row
    public Omega[] row;

    // operator
    public Operator operator;

    // temporary storage for opinions and experiences
    private Set<Opinion> opinions;
    private Set<Experience> experiences;

    protected IRandomGenerator generator;

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

	experiences = null;
	opinions = null;
    }

    @Override
    public void processExperiences(Set<Experience> experiences) {
	this.experiences = experiences;
    }

    @Override
    public void processOpinions(Set<Opinion> opinions) {
	this.opinions = opinions;
    }

    @Override
    public void calculateTrust() {
	expandArray(experiences, opinions);

	for (Opinion o : opinions)
	    op[o.agent1][o.agent2] = normalizedNumeric(o.internalTrustDegree);

	for (Experience e : experiences)
	    row[e.agent] = normalizedNumeric(e.outcome);
    }

    @Override
    public Map<Integer, Omega> getTrust(int service) {
	Map<Integer, Omega> newTrust = operator.compute(row, op);

	// now update Alpha's row
	for (Map.Entry<Integer, Omega> e : newTrust.entrySet())
	    row[e.getKey()] = e.getValue();

	return newTrust;
    }

    @Override
    public String toString() {
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

    @Override
    public void setRandomGenerator(IRandomGenerator generator) {
	this.generator = generator;
    }
}
