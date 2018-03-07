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
package atb.app.manual

import atb.common.DefaultRandomGenerator
import atb.core.AlphaTestbed
import atb.interfaces.Metric
import atb.metric.CumulativeNormalizedUtility
import atb.metric.DefaultOpinionCost
import atb.metric.KendallsTauA
import atb.scenario.TransitiveOpinionProviderSelection
import atb.trustmodel.SimpleSelectingOpinionProviders
import java.util.*

/**
 * An example showing how to run an evaluation in a simple Kotlin program.
 *
 * @author David
 */
fun main(args: Array<String>) {
    // trust model
    val model = SimpleSelectingOpinionProviders()
    model.setRandomGenerator(DefaultRandomGenerator(0))
    model.initialize()

    // scenario
    val scenario = TransitiveOpinionProviderSelection()
    scenario.setRandomGenerator(DefaultRandomGenerator(0))
    scenario.initialize(100, 0.05, 0.1, 1.0, 1.0)

    // metrics
    val accuracy = KendallsTauA()
    val utility = CumulativeNormalizedUtility()
    val opinionCost = DefaultOpinionCost()

    val metrics = HashMap<Metric, Array<Any>?>()
    metrics[accuracy] = null
    metrics[utility] = null
    metrics[opinionCost] = null

    // protocol
    val protocol = AlphaTestbed.getProtocol(model, scenario, metrics)

    // subscribe for receiving results from metrics
    protocol.subscribe({
        for (metric in metrics.keys) {
            for (service in it.scenario.services) {
                println("${it.time}: $metric = ${it.getResult(service, metric)}")
            }
        }
    })

    // run the evaluation
    for (time in 1..500) {
        protocol.step(time)
    }
}