package testbed.interfaces;

import java.util.Map;

/**
 * Interface for metrics that evaluate the obtained utility during a test run.
 * 
 * @author David
 * 
 */
public interface Utility extends Metric {

    /**
     * Returns the normalized cumulative utility that has been obtained so far.
     * 
     * @param capabilities
     *            A map of capabilities, where agents' ID numbers are used for
     *            keys and their respective capabilities as values
     * 
     * @param agent
     *            Agent with whom Alpha made an interaction.
     * 
     * @return An evaluation score between 0 and 1, inclusively.
     */
    public double evaluate(Map<Integer, Double> capabilities, int agent);
}
