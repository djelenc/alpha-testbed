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
package testbed.metric;

import testbed.interfaces.Accuracy;
import testbed.scenario.TargetedAttack;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Evaluates trust in {@link TargetedAttack} scenario.
 * <p>
 * <p>
 * It derives from Kendall's Tau-A, except that -- when evaluating pairs of
 * comparisons -- it skips pairs when one of the agents is the attacker or when
 * both of agents are neutrals. Therefore this method will throw an
 * {@link IllegalArgumentException} if not used on the {@link TargetedAttack}
 * scenario.
 * <p>
 * <p>
 * The reasoning for skipping those pairs is to narrow the measurement of trust
 * only on the attacked agents; attackers and neutrals are not attacked at all,
 * but comparing attacked agents with neutrals is relevant.
 *
 * @author David
 */
public class KTAOfTargetedAgents extends AbstractMetric implements Accuracy {

    @Override
    public <T extends Comparable<T>> double evaluate(Map<Integer, T> trust,
                                                     Map<Integer, Double> capabilities) {
        int concordant = 0, discordant = 0;

        final List<Integer> attackers = TargetedAttack.getAttackers();
        final List<Integer> neutrals = TargetedAttack.getNeutrals();
        double n = 0;

        for (Entry<Integer, Double> cap1 : capabilities.entrySet()) {
            for (Entry<Integer, Double> cap2 : capabilities.entrySet()) {
                if (cap1.getKey() < cap2.getKey()) {
                    final Double c1 = cap1.getValue();
                    final Double c2 = cap2.getValue();
                    final T r1 = trust.get(cap1.getKey());
                    final T r2 = trust.get(cap2.getKey());
                    final Integer a1 = cap1.getKey();
                    final Integer a2 = cap2.getKey();

                    if (attackers.contains(a1) || attackers.contains(a2)
                            || (neutrals.contains(a1) && neutrals.contains(a2)))
                        continue;

                    n++;

                    if (r1 != null && r2 != null) {
                        final int rankDiff = r1.compareTo(r2);
                        final int capDiff = c1.compareTo(c2);

                        if (rankDiff * capDiff > 0) {
                            concordant++;
                        } else if (rankDiff * capDiff < 0) {
                            discordant++;
                        }
                    }
                }
            }
        }

        final double metric = (concordant - discordant) / n;

        return (metric + 1d) / 2d;
    }

    @Override
    public String toString() {
        return "KTA (Targeted agents)";
    }
}
