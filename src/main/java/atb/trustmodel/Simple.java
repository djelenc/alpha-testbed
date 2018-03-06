/*
 * Copyright (c) 2013 David Jelenc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     David Jelenc - initial API and implementation
 */
package atb.trustmodel;

import atb.interfaces.Experience;
import atb.interfaces.Opinion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A straightforward implementation of a trust model. Used mostly for testing
 * purposes.
 * <p>
 * As trust, the model computes the average value between experiences and
 * opinions. The experiences are weighted with their number, and once the model
 * has at least three experiences, it starts to ignore opinions.
 *
 * @author David
 */
public class Simple extends AbstractTrustModel<Double> {
    // cumulative interaction outcomes
    protected double[] exSum;

    // interaction count
    protected int[] exCnt;

    // received opinions
    protected double[][] op;

    // computed reputation
    protected double[] rep;

    @Override
    public void initialize(Object... params) {
        exSum = new double[0];
        exCnt = new int[0];
        op = new double[0][0];
    }

    @Override
    public void processExperiences(List<Experience> experiences) {
        for (Experience e : experiences) {
            exSum[e.agent] += e.outcome;
            exCnt[e.agent] += 1;
        }
    }

    @Override
    public void processOpinions(List<Opinion> opinions) {
        for (Opinion o : opinions) {
            op[o.agent1][o.agent2] = o.internalTrustDegree;
        }
    }

    @Override
    public void calculateTrust() {
        // pass
    }

    @Override
    public Map<Integer, Double> getTrust(int service) {
        final Map<Integer, Double> trust = new LinkedHashMap<Integer, Double>();

        // compute reputations
        rep = new double[exSum.length];

        for (int target = 0; target < op.length; target++) {
            double sum = 0;
            int count = 0;

            for (int reporter = 0; reporter < op.length; reporter++) {
                if (!Double.isNaN(op[reporter][target])) {
                    sum += op[reporter][target];
                    count += 1;
                }
            }

            if (count > 0)
                rep[target] = sum / count;
            else
                rep[target] = Double.NaN;
        }

        // combine experiences and reputation into trust
        for (int agent = 0; agent < exCnt.length; agent++) {
            double w_e = 0, w_r = 0, t = 0;

            // compute weights
            w_e = Math.min(exCnt[agent], 3) / 3d;
            w_r = (Double.isNaN(rep[agent]) ? 0 : 1 - w_e);

            // aggregate data
            if (w_e > 0 || w_r > 0) {
                if (w_e > 0 && w_r > 0) { // experience & opinions
                    t = w_e * exSum[agent] / exCnt[agent] + w_r * rep[agent];
                } else if (w_r > 0) { // opinions only
                    t = rep[agent];
                } else { // only experiences
                    t = exSum[agent] / exCnt[agent];
                }

                trust.put(agent, t);
            }
        }

        return trust;
    }

    @Override
    public void setCurrentTime(int time) {

    }

    @Override
    public void setAgents(List<Integer> agents) {
        // current size of opinions' data structure
        int max = Math.max(op.length - 1, exSum.length - 1);

        // find the maximum ID
        for (int agent : agents)
            if (agent > max)
                max = agent;

        // resize opinions' array
        if (max > op.length - 1) {
            double[][] newOp = new double[max + 1][max + 1];

            for (int i = 0; i < newOp.length; i++)
                for (int j = 0; j < newOp.length; j++)
                    newOp[i][j] = Double.NaN;

            for (int i = 0; i < op.length; i++)
                System.arraycopy(op[i], 0, newOp[i], 0, op.length);

            op = newOp;
        }

        // resize experiences' array
        if (max > exSum.length - 1) {
            double[] newExSum = new double[max + 1];
            System.arraycopy(exSum, 0, newExSum, 0, exSum.length);
            exSum = newExSum;

            int[] newExCnt = new int[max + 1];
            System.arraycopy(exCnt, 0, newExCnt, 0, exCnt.length);
            exCnt = newExCnt;
        }
    }

    @Override
    public void setServices(List<Integer> services) {
    }

}
