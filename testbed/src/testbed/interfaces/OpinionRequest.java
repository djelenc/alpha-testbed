package testbed.interfaces;

/**
 * Represents an opinion request sent by agent Alpha to other agent.
 * 
 * @author David
 * 
 */
public class OpinionRequest {
    /** Agent that will provide opinion */
    public final int agent1;

    /** Agent whom the opinion will be about */
    public final int agent2;

    /** Service type */
    public final int service;

    public OpinionRequest(int agent1, int agent2, int service) {
	this.agent1 = agent1;
	this.agent2 = agent2;
	this.service = service;
    }

    @Override
    public String toString() {
	return String.format("OpinionRequest<%d, %d, %d>", agent1, agent2,
		service);
    }
}