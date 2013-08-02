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
package testbed.trustmodel.qad;

import testbed.interfaces.Experience;

public class QADExp {

    public final int agent;
    public final int service;
    public final int time;
    public final Omega outcome;

    public QADExp(int agent, int service, int time, Omega omega) {
	this.agent = agent;
	this.service = service;
	this.time = time;
	this.outcome = omega;
    }

    public QADExp(Experience e) {
	this.agent = e.agent;
	this.service = e.service;
	this.time = e.time;
	this.outcome = Omega.normalizedNumeric(e.outcome);
    }

    @Override
    public String toString() {
	return String.format("Exp<%d, %d, %d, %s>", agent, service, time,
		outcome);
    }
}
