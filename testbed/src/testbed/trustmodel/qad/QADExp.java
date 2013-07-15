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
