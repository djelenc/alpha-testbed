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
package testbed.trustmodel;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import testbed.common.Tuple;
import testbed.interfaces.OpinionObject;
import testbed.interfaces.ParametersPanel;
import testbed.interfaces.RandomGenerator;
import testbed.interfaces.TrustModel;
import testbed.interfaces.TrustModelTotalOrder;

/**
 * A template class for implementing {@link TrustModel} interfaces. It provides
 * default implementations to a couple of basic methods.
 * 
 * @author David
 * 
 * @param <T>
 *            The data type in which the trust model conveys trust
 */
public abstract class AbstractTrustModel<T extends Comparable<T>, O extends OpinionObject>
	implements TrustModel<T, O>, TrustModelTotalOrder<T> {

    protected RandomGenerator generator;

    @Override
    public void setRandomGenerator(RandomGenerator generator) {
	this.generator = generator;
    }

    @Override
    public String toString() {
	return getClass().getSimpleName();
    }

    @Override
    public ParametersPanel getParametersPanel() {
	return null;
    }

    @Override
    public Set<Tuple<Integer, Integer>> getTrustPartialOrder(int service) {
	final Map<Integer, T> trust = getTrustTotalOrder(service);

	final Set<Tuple<Integer, Integer>> partialTrust = new LinkedHashSet<>();

	for (Entry<Integer, T> t1 : trust.entrySet()) {
	    for (Entry<Integer, T> t2 : trust.entrySet()) {
		final int agent1 = t1.getKey();
		final int agent2 = t2.getKey();

		if (agent1 < agent2) {
		    final T value1 = t1.getValue();
		    final T value2 = t2.getValue();

		    if (value1 != null && value2 != null) {
			if (value1.compareTo(value2) < 0) {
			    partialTrust.add(new Tuple<Integer, Integer>(agent1,
				    agent2));
			} else if (value1.compareTo(value2) > 0) {
			    partialTrust.add(new Tuple<Integer, Integer>(agent2,
				    agent1));
			}
		    }
		}
	    }
	}

	return partialTrust;
    }
}
