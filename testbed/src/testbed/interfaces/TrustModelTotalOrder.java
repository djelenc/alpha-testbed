package testbed.interfaces;

import java.util.Map;

public interface TrustModelTotalOrder<T extends Comparable<T>> {

    /**
     * Returns the calculated trust values for a given service. The trust values
     * have to be packaged in a map, whose keys represent agents and its values
     * represent their computed trust values.
     * 
     * @param service
     *            type of service
     * @return Map where keys represent agents and values computed trust values
     */
    Map<Integer, T> getTrustTotalOrder(int service);

}