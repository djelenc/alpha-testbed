package testbed.common;

import java.util.Comparator;

/**
 * An utility class to enable comparison of objects based on their string
 * representation.
 * 
 * @author David
 * 
 */
public class LexiographicComparator implements Comparator<Object> {

    @Override
    public int compare(Object o1, Object o2) {
	return o1.toString().compareToIgnoreCase(o2.toString());
    }
}
