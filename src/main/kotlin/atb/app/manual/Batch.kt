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

import atb.infrastructure.*
import atb.interfaces.Metric
import atb.metric.CumulativeNormalizedUtility
import atb.metric.DefaultOpinionCost
import atb.metric.KendallsTauA
import atb.scenario.TransitiveOpinionProviderSelection
import atb.trustmodel.SimpleSelectingOpinionProviders
import java.util.concurrent.CompletableFuture

fun main(args: Array<String>) {
    val duration = 500
    val jobs = ArrayList<EvaluationTask>()
    val futures = ArrayList<CompletableFuture<EvaluationState>>()

    val startSeed = 1
    val stopSeed = 30

    for (seed in startSeed..stopSeed) {
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
        val task = setupEvaluation(protocol, duration, metrics.keys)
        futures.add(CompletableFuture
                .supplyAsync(task.supplier)
                .exceptionally {
                    Faulted(it)
                }.thenApply({
                    when (it) {
                        is Completed -> {
                            println("Completed run for ${it.data.seed}")
                            it.data.toJSON()
                        }
                        else -> println("Something went wrong: $it")
                    }
                    it
                }))

        jobs.add(task)
    }

    CompletableFuture.allOf(*futures.toTypedArray()).thenApply {
        println("All done!")
    }.join()
}