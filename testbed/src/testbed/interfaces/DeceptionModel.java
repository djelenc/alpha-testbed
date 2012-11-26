package testbed.interfaces;

/**
 * Interface for defining deception models; ways of defining how agents report
 * opinions to agent Alpha.
 * 
 * @author David
 * 
 */
public interface DeceptionModel {

    /**
     * Initialization method. Called at the beginning of the evaluation.
     * 
     * @param params
     */
    public void initialize(Object... params);

    /**
     * Transforms a given trust degree to a trust degree that is given to agent
     * Alpha.
     * 
     * @param trustDegree
     *            Given trust degree.
     * @return Transformed trust degree.
     */
    public double calculate(double trustDegree);
}
