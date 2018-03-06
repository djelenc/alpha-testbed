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
package atb.metric;

import atb.common.Tuple;
import atb.interfaces.Metric;
import atb.interfaces.ParametersPanel;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;

public abstract class AbstractMetric implements Metric {

    @Override
    public void initialize(Object... params) {

    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public ParametersPanel getParametersPanel() {
        return null;
    }

    /**
     * Constructs a map of dense ranking ("1223" ranking) from a given map of
     * estimations. Estimations can be of any type, but they have to be mutually
     * comparable.
     * <p>
     * In dense ranking, items that compare equal receive the same ranking
     * number, and the next item(s) receive the immediately following ranking
     * number. Equivalently, each item's ranking number is 1 plus the number of
     * items ranked above it that are distinct with respect to the ranking
     * order.
     *
     * @param estimations A map of estimations, where keys represent agents and mapped
     *                    values their estimations in a comparable type.
     * @return A map of rankings, where keys represent agents and mapped values
     * their rankings.
     */
    public <T extends Comparable<T>> Map<Integer, Number> denseRankings(
            Map<Integer, T> estimations) {
        final PriorityQueue<Tuple<Integer, T>> pq = new PriorityQueue<Tuple<Integer, T>>(
                100, Collections.reverseOrder());

        for (Map.Entry<Integer, T> e : estimations.entrySet())
            pq.add(new Tuple<Integer, T>(e.getKey(), e.getValue()));

        final Map<Integer, Number> rankings = new LinkedHashMap<Integer, Number>();

        T previous = null;
        int rank = 0;

        while (!pq.isEmpty()) {
            final Tuple<Integer, T> tuple = pq.poll();
            final int agent = tuple.first;
            final T value = tuple.second;

            if (!value.equals(previous)) {
                rank++;
                previous = value;
            }

            rankings.put(agent, rank);
        }

        return rankings;
    }

    /**
     * Constructs a map of standard competition ranking ("1224" ranking) from a
     * given map of estimations. Estimations can be of any type, but they have
     * to be mutually comparable.
     * <p>
     * In competition ranking, items that compare equal receive the same ranking
     * number, and then a gap is left in the ranking numbers. The number of
     * ranking numbers that are left out in this gap is one less than the number
     * of items that compared equal. Equivalently, each item's ranking number is
     * 1 plus the number of items ranked above it. This ranking strategy is
     * frequently adopted for competitions, as it means that if two (or more)
     * competitors tie for a position in the ranking, the position of all those
     * ranked below them is unaffected (i.e., a competitor only comes second if
     * exactly one person scores better than them, third if exactly two people
     * score better than them, fourth if exactly three people score better than
     * them, etc.).
     *
     * @param estimations A map of estimations, where keys represent agents and mapped
     *                    values their estimations in a comparable type.
     * @return A map of rankings, where keys represent agents and mapped values
     * their rankings.
     */
    public <T extends Comparable<T>> Map<Integer, Number> standardRanking(
            Map<Integer, T> estimations) {
        final PriorityQueue<Tuple<Integer, T>> pq = new PriorityQueue<Tuple<Integer, T>>(
                100, Collections.reverseOrder());

        for (Map.Entry<Integer, T> e : estimations.entrySet())
            pq.add(new Tuple<Integer, T>(e.getKey(), e.getValue()));

        final Map<Integer, Number> rankings = new LinkedHashMap<Integer, Number>();

        T previous = null;
        int count = 0, rank = 1;

        while (!pq.isEmpty()) {
            final Tuple<Integer, T> tuple = pq.poll();
            final int agent = tuple.first;
            final T value = tuple.second;
            count++;

            if (!value.equals(previous)) {
                rank = count;
                previous = value;
            }

            rankings.put(agent, rank);
        }

        return rankings;
    }

    /**
     * Constructs a map of modified competition ranking ("1334" ranking) from a
     * given map of estimations. Estimations can be of any type, but they have
     * to be mutually comparable.
     * <p>
     * Sometimes, competition ranking is done by leaving the gaps in the ranking
     * numbers before the sets of equal-ranking items (rather than after them as
     * in standard competition ranking). The number of ranking numbers that are
     * left out in this gap remains one less than the number of items that
     * compared equal. Equivalently, each item's ranking number is equal to the
     * number of items ranked equal to it or above it. This ranking ensures that
     * a competitor only comes second if they score higher than all but one of
     * their opponents, third if they score higher than all but two of their
     * opponents, etc.
     *
     * @param estimations A map of estimations, where keys represent agents and mapped
     *                    values their estimations in a comparable type.
     * @return A map of rankings, where keys represent agents and mapped values
     * their rankings.
     */
    public <T extends Comparable<T>> Map<Integer, Number> modifiedRanking(
            Map<Integer, T> estimations) {
        final PriorityQueue<Tuple<Integer, T>> pq = new PriorityQueue<Tuple<Integer, T>>();

        for (Map.Entry<Integer, T> e : estimations.entrySet())
            pq.add(new Tuple<Integer, T>(e.getKey(), e.getValue()));

        final Map<Integer, Number> rankings = new LinkedHashMap<Integer, Number>();

        T previous = null;
        int count = pq.size() + 1, rank = pq.size();

        while (!pq.isEmpty()) {
            final Tuple<Integer, T> tuple = pq.poll();
            final int agent = tuple.first;
            final T value = tuple.second;
            count--;

            if (!value.equals(previous)) {
                rank = count;
                previous = value;
            }

            rankings.put(agent, rank);
        }

        return rankings;
    }

    /**
     * Constructs a map of fractional ranking ("1 2.5 2.5 4" ranking) from a
     * given map of estimations. Estimations can be of any type, but they have
     * to be mutually comparable.
     * <p>
     * Items that compare equal receive the same ranking number, which is the
     * mean of what they would have under ordinal rankings. Equivalently, the
     * ranking number of 1 plus the number of items ranked above it plus half
     * the number of items equal to it. This strategy has the property that the
     * sum of the ranking numbers is the same as under ordinal ranking. For this
     * reason, it is used in computing Borda counts and in statistical tests.
     *
     * @param estimations A map of estimations, where keys represent agents and mapped
     *                    values their estimations in a comparable type.
     * @return A map of rankings, where keys represent agents and mapped values
     * their rankings.
     */
    public <T extends Comparable<T>> Map<Integer, Double> fractionalRanking(
            Map<Integer, T> estimations) {

        final Map<Integer, Number> standard = standardRanking(estimations);
        final Map<Integer, Number> modified = modifiedRanking(estimations);

        final Map<Integer, Double> rankings = new LinkedHashMap<Integer, Double>();

        for (Map.Entry<Integer, Number> e : standard.entrySet()) {
            final Integer agent = e.getKey();
            final Double rank = (e.getValue().doubleValue()
                    + modified.get(agent).doubleValue()) / 2d;
            rankings.put(agent, rank);
        }

        return rankings;
    }
}
