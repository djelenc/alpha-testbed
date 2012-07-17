package testbed.trustmodel;

/**
 * Enumeration class that represents trust degrees and grades of outcomes for
 * trust model of {@link AbdulRahmanHailes Abdul-Rahman and Hailes}.
 * 
 * @author David
 * 
 */
public enum TD {
    VG(0.75), G(0.50), B(0.25), VB(0.00);

    public final double numeric;

    private TD(double numeric) {
	this.numeric = numeric;
    }

    public static TD fromDouble(double d) {
	for (TD td : values())
	    if (d >= td.numeric)
		return td;

	throw new IllegalArgumentException(String.format(
		"Cannot determine TD for value %.2f", d));
    }

    public static TD fromIndex(int i) {
	for (TD td : values())
	    if (td.ordinal() == i)
		return td;

	throw new IllegalArgumentException(String.format(
		"Cannot determine TD for rank %d", i));
    }
}