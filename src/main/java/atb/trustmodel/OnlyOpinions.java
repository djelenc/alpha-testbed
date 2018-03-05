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
 * Trust model that only uses opinions. The opinions are cached, thus the model
 * considers opinions from previous ticks. If new opinions are obtained, the old
 * ones are discarded.
 *
 * @author David
 */
public class OnlyOpinions extends AbstractTrustModel<Double> {
    // opinions
    private double[][] op;

    @Override
    public void initialize(Object... params) {
        op = new double[0][0];
    }

    @Override
    public void processExperiences(List<Experience> experiences) {

    }

    @Override
    public void processOpinions(List<Opinion> opinions) {
        for (Opinion o : opinions)
            op[o.agent1][o.agent2] = o.internalTrustDegree;
    }

    @Override
    public void calculateTrust() {
        // pass
    }

    @Override
    public Map<Integer, Double> getTrust(int service) {
        Map<Integer, Double> trust = new LinkedHashMap<Integer, Double>();

        for (int agent = 0; agent < op.length; agent++) {
            double sum = 0;
            int count = 0;

            for (int reporter = 0; reporter < op.length; reporter++) {
                if (!Double.isNaN(op[reporter][agent])) {
                    sum += op[reporter][agent];
                    count += 1;
                }
            }

            if (count > 0)
                trust.put(agent, sum / count);
        }

        return trust;
    }

    @Override
    public String toString() {
        return "Opinions only";
    }

    @Override
    public void setCurrentTime(int time) {
    }

    @Override
    public void setAgents(List<Integer> agents) {
        // current size of opinions' data structure
        int max = op.length - 1;

        // find the maximum ID
        for (int agent : agents)
            if (agent > max)
                max = agent;

        // resize arrays
        if (max > op.length - 1) {
            double[][] newOp = new double[max + 1][max + 1];

            for (int i = 0; i < newOp.length; i++)
                for (int j = 0; j < newOp.length; j++)
                    newOp[i][j] = Double.NaN;

            for (int i = 0; i < op.length; i++)
                System.arraycopy(op[i], 0, newOp[i], 0, op.length);

            op = newOp;
        }
    }

    @Override
    public void setServices(List<Integer> services) {
    }
}
