package testbed.repast;

import testbed.AlphaTestbed;
import testbed.MetricSubscriber;
import testbed.interfaces.Metric;

/**
 * An utility class to enable a simple plotting and constructing data sets in
 * Repast.
 * 
 * <p>
 * Instances of this class are Repast agents. They are used to plot data and to
 * write data to the files. Each agent represents a curve in the graph.
 * Unfortunately, <a href=
 * 'http://sourceforge.net/mailarchive/message.php?msg_id=27474156'>there is no
 * way to manually define the color of each plot</a> -- Repast determines the
 * colors at run-time. {@link RepastMetricAgent} agents also write data Repast
 * data out-putters. Currently, each line has the following pattern:
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

    public RepastMetricAgent(int service, Metric metric, AlphaTestbed testbed) {
	this.service = service;
	this.metric = metric;
	this.name = String.format("%s[%d]", metric.toString(), service);
	this.model = testbed.getModel().toString();
	this.scenario = testbed.getScenario().toString();

	// subscribe notifications
	testbed.subscribe(this);
    }

    @Override
    public void update(AlphaTestbed instance) {
	// pulls data from the test-bed
	currentValue = instance.getMetric(service, metric);
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
}
