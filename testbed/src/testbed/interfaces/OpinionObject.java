package testbed.interfaces;

public class OpinionObject {

    /** Opinion provider */
    public final int agent1;

    /** The agent who the opinion is about */
    public final int agent2;

    /** The type of service */
    public final int service;

    /** Time when the opinion was given */
    public final int time;

    public OpinionObject(int agent1, int agent2, int service, int time) {
	this.agent1 = agent1;
	this.agent2 = agent2;
	this.service = service;
	this.time = time;
    }

}