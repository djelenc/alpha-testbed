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
package testbed.interfaces;

/**
 * Represents an Credibility object.
 * 
 * <p>
 * A credibility object is a triple (A, B, C) meaning that agent C stated that A
 * is less credible than B. In this class, A corresponds to agent1, B to agent2
 * and C to agent3.
 * 
 * @author David
 * 
 */
public final class CredibilityObject extends OpinionObject {
    public final int agent3;

    /**
     * Creates a new {@link CredibilityObject}.
     * 
     * @param agent1
     *            Agent that is less credible
     * @param agent2
     *            Agent that is more credible
     * @param agent3
     *            Agent that is providing the statement
     * @param service
     *            The type of service
     * @param time
     *            Time at which the opinion was given
     */
    public CredibilityObject(int agent1, int agent2, int agent3, int service,
	    int time) {
	super(agent1, agent2, service, time);
	this.agent3 = agent3;
    }

    @Override
    public String toString() {
	return String.format("CredibilityObject[%d, %d, %d (%d, %d)]", agent1,
		agent2, agent3, service, time);
    }
}
