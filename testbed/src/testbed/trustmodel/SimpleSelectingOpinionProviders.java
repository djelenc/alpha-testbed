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

import java.util.ArrayList;
import java.util.List;

import testbed.interfaces.OpinionRequest;
import testbed.interfaces.SelectingOpinionProviders;

/**
 * Trust model on the basis of the {@link Simple} that supports selection
 * partners for interactions and selection opinion providers.
 * 
 * @author David
 * 
 */
public class SimpleSelectingOpinionProviders
	extends SimpleSelectingInteractionPartners
	implements SelectingOpinionProviders {

    protected List<Integer> agents;

    @Override
    public void initialize(Object... params) {
	super.initialize(params);
    }

    @Override
    public List<OpinionRequest> getOpinionRequests() {
	final List<OpinionRequest> opinionRequests = new ArrayList<OpinionRequest>();

	for (int target : agents) {
	    // ask only if there are less than 3 experiences
	    if (exCnt[target] < 3) {
		for (int source : agents) {
		    if (target != source) {
			opinionRequests
				.add(new OpinionRequest(source, target, 0));
		    }

		}
	    }
	}

	return opinionRequests;
    }

    @Override
    public void setAgents(List<Integer> agents) {
	super.setAgents(agents);

	this.agents = agents;
    }

    @Override
    public String toString() {
	return "Simple with opinion selection";
    }

}
