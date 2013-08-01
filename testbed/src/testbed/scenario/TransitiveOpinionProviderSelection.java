/*******************************************************************************
 * Copyright (c) 2013 David Jelenc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     David Jelenc - initial API and implementation
 ******************************************************************************/
package testbed.scenario;

import java.util.ArrayList;
import java.util.List;

import testbed.interfaces.Opinion;
import testbed.interfaces.OpinionProviderSelection;
import testbed.interfaces.OpinionRequest;

/**
 * A transitive scenario that allows an agent to select interaction partners.
 * 
 * @author David
 * 
 */
public class TransitiveOpinionProviderSelection extends
	TransitiveInteractionPartnerSelection implements
	OpinionProviderSelection {

    protected List<OpinionRequest> opinionRequests;

    @Override
    public void initialize(Object... parameters) {
	super.initialize(parameters);
	opinionRequests = null;
    }

    @Override
    public void setOpinionRequests(List<OpinionRequest> opinionRequests) {
	this.opinionRequests = opinionRequests;
    }

    @Override
    public List<Opinion> generateOpinions() {
	final List<Opinion> opinions = new ArrayList<Opinion>();

	for (OpinionRequest or : opinionRequests) {
	    final int agent1 = or.agent1;
	    final int agent2 = or.agent2;

	    // NOTE: because the scenario has only 1 service, the service
	    // component is ignored

	    if (dms[agent1][agent2] != null) {
		final double cap = capabilities.get(agent2);

		double itd = generator.nextDoubleFromUnitTND(cap, sd_o);
		itd = dms[agent1][agent2].calculate(itd);

		final Opinion opinion;
		opinion = new Opinion(agent1, agent2, 0, time, itd, sd_o);

		opinions.add(opinion);
	    }
	}

	return opinions;
    }

    @Override
    public String toString() {
	return "Transitive with opinion selection";
    }

}
