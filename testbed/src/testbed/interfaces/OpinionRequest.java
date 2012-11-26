package testbed.interfaces;

/**
 * Represents an opinion request sent by agent Alpha to other agent.
 * 
 * @author David
 * 
 */
public class OpinionRequest implements Comparable<OpinionRequest> {
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

    @Override
    public int compareTo(OpinionRequest that) {
	final int cmp1 = this.agent1 - that.agent1;
	final int cmp2 = this.agent2 - that.agent2;
	final int cmp3 = this.service - that.service;

	if (cmp1 != 0) {
	    return cmp1;
	} else if (cmp2 != 0) {
	    return cmp2;
	} else {
	    return cmp3;
	}
    }
}