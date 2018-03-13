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
import atb.infrastructure.runBatch
import atb.infrastructure.setupEvaluation
import atb.interfaces.Metric
import atb.metric.CumulativeNormalizedUtility
import atb.metric.DefaultOpinionCost
import atb.metric.KendallsTauA
import atb.scenario.TransitiveOpinionProviderSelection
import atb.trustmodel.SimpleSelectingOpinionProviders

fun main(args: Array<String>) {
    val duration = 500
    val start = 1
    val stop = 30

    val tasks = (start..stop).map { seed ->
        // trust model
        val model = SimpleSelectingOpinionProviders()

        // scenario
        val scenario = TransitiveOpinionProviderSelection()
        val scenarioParams = arrayOf(100, 0.05, 0.1, 1.0, 1.0)

        // metrics
        val metrics = hashMapOf<Metric, Array<Any>>(
                KendallsTauA() to emptyArray(),
                CumulativeNormalizedUtility() to emptyArray(),
                DefaultOpinionCost() to emptyArray())

        // protocol
        val protocol = createProtocol(model, emptyArray(), scenario, scenarioParams, metrics, seed)

        setupEvaluation(protocol, duration, metrics.keys)
    }

    val interrupt = runBatch(tasks, { println("All done!") })

    Thread.sleep(3000)
    interrupt()
    Thread.sleep(3000)
}