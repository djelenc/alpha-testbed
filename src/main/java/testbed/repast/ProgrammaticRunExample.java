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
package testbed.repast;

import testbed.common.DefaultRandomGenerator;
import testbed.core.AlphaTestbed;
import testbed.core.EvaluationProtocol;
import testbed.core.MetricSubscriber;
import testbed.interfaces.Metric;
import testbed.interfaces.Scenario;
import testbed.interfaces.TrustModel;
import testbed.metric.CumulativeNormalizedUtility;
import testbed.metric.DefaultOpinionCost;
import testbed.metric.KendallsTauA;
import testbed.scenario.TransitiveOpinionProviderSelection;
import testbed.trustmodel.SimpleSelectingOpinionProviders;

import java.util.HashMap;
import java.util.Map;

/**
 * A class that demonstrates how an evaluation can be run as a simple Java
 * program.
 *
 * @author David
 */
public class ProgrammaticRunExample implements MetricSubscriber {

    private final int service;

    private final Metric metric;

    public ProgrammaticRunExample(Metric m) {
        service = 0;
        metric = m;
    }

    public static void main(String[] args) {
        // trust model
        TrustModel<?> model = new SimpleSelectingOpinionProviders();
        model.setRandomGenerator(new DefaultRandomGenerator(0));
        model.initialize();

        // scenario
        Scenario scenario = new TransitiveOpinionProviderSelection();
        scenario.setRandomGenerator(new DefaultRandomGenerator(0));
        scenario.initialize(100, 0.05, 0.1, 1d, 1d);

        // metrics
        Metric accuracy = new KendallsTauA();
        Metric utility = new CumulativeNormalizedUtility();
        Metric opinionCost = new DefaultOpinionCost();

        Map<Metric, Object[]> metrics = new HashMap<>();
        metrics.put(accuracy, null);
        metrics.put(utility, null);
        metrics.put(opinionCost, null);

        EvaluationProtocol ep = AlphaTestbed.getProtocol(model, scenario, metrics);
        ep.subscribe(new ProgrammaticRunExample(accuracy));
        ep.subscribe(new ProgrammaticRunExample(utility));
        ep.subscribe(new ProgrammaticRunExample(opinionCost));

        for (int time = 1; time <= 500; time++) {
            ep.step(time);
        }
    }

    @Override
    public void update(EvaluationProtocol instance) {
        System.out.printf("%s (%s): %.2f\n", metric, service,
                instance.getResult(service, metric));
    }
}
