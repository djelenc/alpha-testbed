package testbed.trustmodel.qad;

/**
 * Enumeration class that represents trust degrees and assessments for trust
 * model {@link QAD}.
 * 
 * @author David
 * 
 */
public enum Omega {
    D(-2d), PD(-1d), U(0d), PT(1d), T(2d);

    private static final String MSG_NUM = "Cannot determine Omega for value %.2f";
    private static final String MSG_NORM = "Cannot determine Omega from normalized value %.2f";

    public final double numeric;

    private Omega(double numeric) {
	this.numeric = numeric;
    }

    /**
     * Returns a {@link Omega} instance that is computed from its numeric
     * representation. Valid numeric representations are {-2, -1, 0, 1, 2}.
     * 
     * @param d
     * @return
     */
    public static Omega fromNumeric(double d) {
	if (d < -2d || d > 2d)
	    throw new IllegalArgumentException(String.format(MSG_NUM, d));

	for (Omega td : values())
	    if (Math.abs(td.numeric - d) < 0.00001)
		return td;

	throw new IllegalArgumentException(String.format(MSG_NUM, d));
    }

    /**
     * Returns a {@link Omega} instance that is computed from a given double.
     * The given double must be normalized, that is it must lie within [0, 1].
     * 
     * @param d
     * @return
     */
    public static Omega normalizedNumeric(double d) {
	if (d < 0 || d > 1d) {
	    throw new IllegalArgumentException(String.format(MSG_NORM, d));
	} else if (d < 0.2) {
	    return D;
	} else if (d < 0.4) {
	    return PD;
	} else if (d < 0.6) {
	    return U;
	} else if (d < 0.8) {
	    return PT;
	} else {
	    return T;
	}
    }

}