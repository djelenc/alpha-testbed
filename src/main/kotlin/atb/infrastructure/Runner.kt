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
 * and the [interrupter] is a function that can be used to interrupt the task once it starts. Once the tasks ends,
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
            try {
                if (isInterrupted.get()) {
                    return@supplier Interrupted(tick, data)
                }

                protocol.step(tick)
            } catch (e: Exception) {
                return@supplier Faulted(tick, e)
            }
        }
        return@supplier Completed(data)
    }

    return EvaluationTask(supplier, interrupter)
}

/**
 * Runs given evaluation task asynchronously and fires the callback upon competition.
 *
 * @return A handle to interrupt the task
 * */
fun run(task: EvaluationTask, callback: (EvaluationState) -> Unit): () -> Unit {
    CompletableFuture.supplyAsync(task.supplier).thenAccept { callback(it) }
    return task.interrupter
}

/**
 * Runs given list of evaluation tasks asynchronously and fires the [finished] callback upon
 * competition. The optional parameter, [progress], is invoked upon completition of every
 * task in the list.
 *
 * @return A handle to interrupt the entire run (completed, running and scheduled tasks)
 */
fun runBatch(tasks: List<EvaluationTask>, finished: (List<EvaluationState>) -> Unit,
             progress: (EvaluationState) -> Unit = {}): () -> Unit {
    val interruptAll: () -> Unit = { tasks.forEach { it.interrupter() } }

    val allTasksAsync = tasks.map {
        CompletableFuture.supplyAsync(it.supplier).thenApply {
            progress(it)
            it
        }
    }.toTypedArray()

    // wait for all to complete, then fire callback
    CompletableFuture.allOf(*allTasksAsync).thenApply {
        val completed = allTasksAsync.map { it.join() }
        finished(completed)
    }

    return interruptAll
}