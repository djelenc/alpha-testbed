package atb.infrastructure

import atb.common.DefaultRandomGenerator
import atb.core.AlphaTestbed
import atb.core.EvaluationProtocol
import atb.interfaces.Metric
import atb.interfaces.Scenario
import atb.interfaces.TrustModel
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Supplier


/**
 * Creates an evaluation protocol from given input parameters.
 */
fun createProtocol(model: TrustModel<*>, modelParams: Array<Any>, scenario: Scenario,
                   scenarioParams: Array<Any>, metrics: Map<Metric, Array<Any>>, seed: Int): EvaluationProtocol {
    model.setRandomGenerator(DefaultRandomGenerator(seed))
    model.initialize(*modelParams)

    scenario.randomGenerator = DefaultRandomGenerator(seed)
    scenario.initialize(*scenarioParams)

    return AlphaTestbed.getProtocol(model, scenario, metrics)
}

/** Represents an evaluation task that can be run on a thread pool; [supplier] is the task that is to be executed,
 * and the [interrupter] is a function that can be used to interrupt the task once it starts. As the tasks ends,
 * invoking interrupter is a no-op. */
class EvaluationTask(val supplier: Supplier<EvaluationState>, val interrupter: () -> Unit)

/**
 * Sets up an evaluation run and returns an EvaluationTask.
 * Runs the evaluation setup (consisting of the [protocol], [duration] and [metrics]) asynchronously.
 *
 * @return A callback, which, upon invocation, stops the evaluation run. Invoking the handled on an
 * evaluation run that has already ended, results in a no-op.
 */
fun setupEvaluation(protocol: EvaluationProtocol, duration: Int, metrics: Set<Metric>): EvaluationTask {
    // evaluation data
    val data = EvaluationData(protocol, metrics, ArrayList(), protocol.scenario.randomGenerator.seed)

    // subscribe for updates
    protocol.subscribe {
        for (metric in metrics) {
            for (service in it.scenario.services) {
                val value = it.getResult(service, metric)
                data.readings.add(Reading(it.time, metric, service, value))
            }
        }
    }

    // create interruption function
    val isInterrupted = AtomicBoolean(false)
    val interrupter = { isInterrupted.set(true) }

    // create supplier (actual task)
    val supplier = Supplier supplier@{
        for (tick in 1..duration) {
            protocol.step(tick)

            if (isInterrupted.get()) {
                return@supplier Interrupted(tick, data)
            }
        }
        return@supplier Completed(data)
    }

    return EvaluationTask(supplier, interrupter)
}

/**
 * Runs given evaluation task asynchronously and fires the callback upon competition.
 *
 * @return A reference to the underlying completable future
 * */
fun runAsync(task: EvaluationTask, callback: (EvaluationState) -> Unit): CompletableFuture<Void> =
        CompletableFuture.supplyAsync(task.supplier).exceptionally {
            Faulted(it)
        }.thenAccept {
            callback(it)
        }

