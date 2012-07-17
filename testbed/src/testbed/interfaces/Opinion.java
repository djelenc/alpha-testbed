package testbed.interfaces;

public final class Opinion {
    public final int agent1;
    public final int agent2;
    public final int service;
    public final int time;
    public final double internalTrustDegree;

    public Opinion(int agent1, int agent2, int service, int time, double itd) {
	this.agent1 = agent1;
	this.agent2 = agent2;
	this.service = service;
	this.time = time;
	this.internalTrustDegree = itd;
    }

    @Override
    public String toString() {
	return String.format("Opinion<%d, %d, %d, %d, %.2f>", agent1, agent2,
		service, time, internalTrustDegree);
    }
}
