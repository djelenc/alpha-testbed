/*******************************************************************************
 * Copyright (c) 2013 David Jelenc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     David Jelenc - initial API and implementation
 ******************************************************************************/
package testbed.repast;

import testbed.core.EvaluationProtocol;
import testbed.core.MetricSubscriber;
import testbed.interfaces.Metric;

/**
 * An utility class to enable plotting and constructing data sets in Repast.
 * 
 * <p>
 * Instances of this class are Repast agents. They are used to plot data and to
 * write data to the files. Each agent represents a curve in the graph.
 * 
 * <p>
 * Unfortunately, <a href=
 * 'http://sourceforge.net/mailarchive/message.php?msg_id=27474156'>there is no
 * way to manually define the color of each plot</a> -- Repast determines the
 * colors at run-time.
 * 
 * <p>
 * {@link RepastMetricAgent} agents also write data Repast data out-putters.
 * Currently, each line has the following pattern:
 * 
 * <pre>
 * tick, metric value, metric name, scenario name
 * </pre>
 * 
 * @author David
 * 
 */
public class RepastMetricAgent implements MetricSubscriber {

    private final int service;
    private final Metric metric;

    private final String name;
    private final String model;
    private final String scenario;

    private double currentValue = 0;

    public RepastMetricAgent(int service, Metric metric,
	    EvaluationProtocol evaluation) {
	this.service = service;
	this.metric = metric;
	this.name = String.format("%s[%d]", metric.toString(), service);
	this.model = evaluation.getTrustModel().toString();
	this.scenario = evaluation.getScenario().toString();

	// subscribe to notifications
	evaluation.subscribe(this);
    }

    public double getMetric() {
	return currentValue;
    }

    @Override
    public String toString() {
	return this.name;
    }

    public String getModel() {
	return this.model;
    }

    public String getScenario() {
	return this.scenario;
    }

    @Override
    public void update(EvaluationProtocol instance) {
	currentValue = instance.getResult(service, metric);
    }
}
