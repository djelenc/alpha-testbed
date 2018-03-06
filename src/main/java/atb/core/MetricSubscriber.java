/*
 * Copyright (c) 2013 David Jelenc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     David Jelenc - initial API and implementation
 */
package atb.core;

/**
 * Interface for metric subscribers.
 * <p>
 * <p>
 * The instance of the {@link AlphaTestbed} notifies all instances of classes
 * that implement this interface that the evaluation step is complete and that
 * they can retrieve data from the {@link AlphaTestbed} instance using
 * {@link AlphaTestbed#getResult(int, atb.interfaces.IMetric)} method.
 *
 * @author David
 */
public interface MetricSubscriber {

    /**
     * This method is called at the end of each time step to notify the
     * subscriber that the test-bed is ready to serve new data.
     * <p>
     * <p>
     * The data has to be pulled from the instance (provided in the argument)
     * using
     * {@link EvaluationProtocol#getResult(int, atb.interfaces.Metric)}
     * method.
     *
     * @param instance An {@link EvaluationProtocol} instance that performs the
     *                 evaluation
     */
    public void update(EvaluationProtocol instance);
}
