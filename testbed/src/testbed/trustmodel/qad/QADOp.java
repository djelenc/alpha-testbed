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
package testbed.trustmodel.qad;

import testbed.interfaces.Opinion;

public class QADOp {

    public final int agent1;
    public final int agent2;
    public final int service;
    public final int time;
    public final Omega itd;

    public QADOp(int agent1, int agent2, int service, int time, Omega omega) {
	this.agent1 = agent1;
	this.agent2 = agent2;
	this.service = service;
	this.time = time;
	this.itd = omega;
    }

    public QADOp(Opinion o) {
	this.agent1 = o.agent1;
	this.agent2 = o.agent2;
	this.service = o.service;
	this.time = o.time;
	this.itd = Omega.normalizedNumeric(o.internalTrustDegree);
    }

    @Override
    public String toString() {
	return String.format("Opinion<%d, %d, %d, %d, %s>", agent1, agent2,
		service, time, itd);
    }
}
