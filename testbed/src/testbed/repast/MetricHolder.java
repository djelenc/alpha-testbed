package testbed.repast;

import repast.simphony.engine.schedule.ScheduledMethod;
import testbed.Simulator;
import testbed.interfaces.IMetric;

/**
 * An utility class to enable a simple plotting and constructing data sets in
 * Repast.
 * 
 * @author David
 * 
 */
public class MetricHolder {

    private final int service;
    private final IMetric metric;
    private final Simulator simulator;
    private final String name;
    private final String model;
    private final String scenario;

    private double currentValue = 0;

    public MetricHolder(int service, IMetric metric, Simulator sim) {
	this.service = service;
	this.metric = metric;
	this.simulator = sim;
	this.name = String.format("%s[%d]", metric.getName(), service);
	this.model = sim.getModel().getName();
	this.scenario = sim.getScenario().getName();
    }

    // priority = 1 to ensure the evaluation score is read after the trust
    // model's calculations are made
    @ScheduledMethod(start = 1, interval = 1, priority = 1)
    public void step() {
	currentValue = simulator.getMetric(service, metric);
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
