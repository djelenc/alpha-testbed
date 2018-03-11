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

import atb.infrastructure.createProtocol
import atb.infrastructure.runAsync
import atb.infrastructure.setupEvaluation
import atb.interfaces.Metric
import atb.metric.CumulativeNormalizedUtility
import atb.metric.DefaultOpinionCost
import atb.metric.KendallsTauA
import atb.scenario.TransitiveOpinionProviderSelection
import atb.trustmodel.SimpleSelectingOpinionProviders

/**
 * An example showing how to run an evaluation in a simple Kotlin program.
 *
 * @author David
 */
fun main(args: Array<String>) {
    // random seed
    val seed = 0

    // evaluation duration in ticks
    val duration = 500

    // trust model
    val model = SimpleSelectingOpinionProviders()

    // scenario
    val scenario = TransitiveOpinionProviderSelection()
    val scenarioParams = arrayOf(100, 0.05, 0.1, 1.0, 1.0)

    // metrics
    val metrics = hashMapOf<Metric, Array<Any>>(
            KendallsTauA() to emptyArray(),
            CumulativeNormalizedUtility() to emptyArray(),
            DefaultOpinionCost() to emptyArray()
    )

    // protocol
    val protocol = createProtocol(model, emptyArray(), scenario, scenarioParams, metrics, seed)

    // subscribe to receive results in real-time
    protocol.subscribe({
        for (metric in metrics.keys) {
            for (service in it.scenario.services) {
                println("${it.time}: $metric = ${it.getResult(service, metric)}")
            }
        }
    })

    val evaluationTask = setupEvaluation(protocol, duration, metrics.keys)

    runAsync(evaluationTask.supplier, {
        println("Got ${it.data.readings.size} lines of data!")
    }, {
        println("Got an error: ${it.thrown.message}")
    }, {
        println("Run was interrupted at tick ${it.tick}")
    }).join() // this will block untill evaluation finishes

    println("Finished testing '${protocol.trustModel}' in '${protocol.scenario}'.")
}