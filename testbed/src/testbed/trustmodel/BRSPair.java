package testbed.trustmodel;

public class BRSPair {
    public double R, S;

    public BRSPair(double r, double s) {
	this.R = r;
	this.S = s;
    }

    public BRSPair() {
	this.R = 0;
	this.S = 0;
    }

    @Override
    public String toString() {
	return String.format("(%.2f, %.2f)", R, S);
    }
}
