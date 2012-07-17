package testbed.interfaces;

public final class Experience {
    public final int agent;
    public final int service;
    public final int time;
    public final double outcome;

    public Experience(int agent, int service, int time, double outcome) {
	this.agent = agent;
	this.service = service;
	this.time = time;
	this.outcome = outcome;
    }

    @Override
    public String toString() {
	return String.format("Exp<%d, %d, %d, %.2f>", agent, service, time,
		outcome);
    }
}
