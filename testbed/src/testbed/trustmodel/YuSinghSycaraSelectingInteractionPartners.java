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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import testbed.common.PartnerSelectionTemplates;
import testbed.interfaces.SelectingInteractionPartners;

/**
 * Trust model on the basis of the {@link YuSinghSycara} that supports selection
 * partners for interactions. The selection is probabilistic.
 * 
 * <p>
 * <b>Note that the original proposal contains no such procedure. This is for
 * experimental purposes only.</b>
 * 
 * @author David
 * 
 */
public class YuSinghSycaraSelectingInteractionPartners extends YuSinghSycara
	implements SelectingInteractionPartners {

    protected int time;

    protected PartnerSelectionTemplates selector;

    @Override
    public void initialize(Object... params) {
	super.initialize(params);
	time = 0;
	selector = new PartnerSelectionTemplates(generator);
    }

    @Override
    public void setCurrentTime(int time) {
	this.time = time;
    }

    @Override
    public Map<Integer, Integer> getInteractionPartners(
	    List<Integer> services) {
	final Map<Integer, Integer> partners = new HashMap<Integer, Integer>();

	for (int service : services) {
	    final Map<Integer, Double> trust = getTrustTotalOrder(service);
	    final Integer best = selector.probabilistic(trust);
	    // final Integer best = selector.maximal(trust);
	    partners.put(service, best);
	}

	return partners;
    }
}
