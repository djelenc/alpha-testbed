package testbed.interfaces;

import java.util.Map;
import java.util.Set;

/**
 * 
 * Interface for defining capabilities, assigning deception models and selecting
 * collaborators for interactions.
 * 
 */
public interface IScenario {

    /**
     * Sets the random number generator. In order to produce repeatable results,
     * all random numbers should be generated with this generator.
     * 
     * @param generator
     *            Generator to be set
     */
    public void setRandomGenerator(IRandomGenerator generator);

    /**
     * Initializes the scenario with an optional array of varargs Objects.
     * 
     * @param parameters
     *            Optional scenario parameters
     */
    public void initialize(Object... parameters);

    /**
     * Notifies the scenario of the current time. The testbed calls this method
     * at the beginning of every every step.
     * 
     * @param time
     *            Current time
     */
    public void setCurrentTime(int time);

    /**
     * Gets the capabilities of agents that concern the given service. The
     * IMetric can then construct a ranking from it.
     * 
     * @param service
     * @return
     */
    public Map<Integer, Double> getCapabilities(int service);

    /**
     * Returns generator's friendly name.
     * 
     * @return
     */
    public String getName();

    /**
     * Generates a set of {@link Opinion} tuples.
     * 
     * @return
     */
    public Set<Opinion> generateOpinions();

    /**
     * Generates a set of {@link Experience} tuples.
     * 
     * @return
     */
    public Set<Experience> generateExperiences();

    /**
     * Returns a set of ID numbers of agents.
     * 
     * @return
     */
    public Set<Integer> getAgents();

    /**
     * Returns a set of service types.
     * 
     * @return
     */
    public Set<Integer> getServices();

    /**
     * Returns an {@link IParametersPanel} instance, which is responsible for
     * generating a graphical user interface for setting scenario parameters.
     * 
     * <p>
     * When a scenario does not need parameters, the method should return null.
     * 
     * @return
     */
    public IParametersPanel getParametersPanel();
}
