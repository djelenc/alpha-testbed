package testbed.interfaces;

import java.util.Map;
import java.util.Set;

public interface ITrustModel {

    /**
     * Initializes the trust model with an optional array of varargs Objects.
     * Called only once, at the very start of the test run.
     * 
     * @param params
     *            Optional parameters
     */
    public void initialize(Object... params);

    /**
     * Notifies the trust model of the current time. Called once at every step
     * of the test run.
     * 
     * @param time
     *            Current time
     */
    public void setCurrentTime(int time);

    /**
     * Updates the trust model with a new experience and newly obtained
     * opinions. The method then calculates trust towards other agents. The
     * method is called once at every step of the test run.
     * 
     * @param experiences
     *            Set of newly obtained experience tuples
     * @param opinions
     *            Set of newly obtained opinion tuples
     */
    public void calculateTrust(Set<Experience> experiences,
	    Set<Opinion> opinions);

    /**
     * Returns the ranking of agents for a given service. The ranking has to be
     * given in a map, where the agents are used as keys and their ranks as
     * values.
     * 
     * <p>
     * The ranking should starts with 1 and need not contain all available
     * agents. Assigning multiple agents to the same rank is also allowed.
     * 
     * <p>
     * For instance, for agents 1, 2, 3, 4, 5 a valid ranking may be:
     * 
     * <p>
     * <blockquote>
     * 
     * <pre>
     * Map&lt;Integer, Integer&gt; ranking = new HashMap&lt;Integer, Integer&gt;();
     * ranking.put(1, 1);
     * ranking.put(2, 1);
     * ranking.put(3, 2);
     * </pre>
     * 
     * </blockquote>
     * 
     * 
     * <p>
     * In this example agents 1 and 2 are the most trustworthy, agent 3 is less,
     * and the agents 4 and 5 are omitted.
     * 
     * 
     * @param service
     *            type of service
     * @return
     */
    public Map<Integer, Integer> getRankings(int service);

    /**
     * Returns an {@link IParametersPanel} instance, which is responsible for
     * generating a graphical user interface for setting trust model's
     * parameters.
     * 
     * <p>
     * When a model does not need parameters, the method should return null.
     * 
     * @return
     */
    public IParametersPanel getParametersPanel();

    /**
     * Returns friendly name of a trust model
     * 
     * @return
     */
    public String getName();

}
