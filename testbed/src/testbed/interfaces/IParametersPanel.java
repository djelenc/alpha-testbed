package testbed.interfaces;

import java.awt.Component;
import java.util.Observer;

public interface IParametersPanel {

    /**
     * Initializes the parameters panel.
     * 
     * @param o
     *            An observer instance, which can be notified when parameters
     *            are valid by calling
     *            {@link Observer#update(java.util.Observable, Object)}. The
     *            first argument can be null, while the second should be True if
     *            the parameters are valid, or false, when they are invalid.
     * @param params
     *            Optional parameters.
     */
    public void initialize(Observer o, Object... params);

    /**
     * Returns an {@link java.awt.Component} instance that holds the GUI for the
     * parameters.
     * 
     * @return
     */
    public Component getComponent();

    /**
     * Returns an object of parameters for a particular component.
     * 
     * @return
     */
    public Object[] getParameters();
}
