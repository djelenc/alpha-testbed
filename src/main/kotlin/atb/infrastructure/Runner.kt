package atb.infrastructure

import atb.core.EvaluationProtocol
import atb.interfaces.Metric
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Supplier


/**
 * Runs the evaluation setup (consisting of the [protocol], [duration] and [metrics]) asynchronously. The method
 * fires the following callbacks:
 * * [completed] callback on successfully completing the run;
 * * [faulted] callback if an exception occurs during the run;
 * * [interrupted] callback if the run gets interrupted.
 *
 * @return A callback, which, upon invocation, stops the evaluation run and triggers the [interrupted] callback.
 * If the run has already ended, the invocation does nothing.
 */
fun runAsync(protocol: EvaluationProtocol, duration: Int, metrics: Set<Metric>,
             completed: (Completed) -> Unit, faulted: (Faulted) -> Unit, interrupted: (Interrupted) -> Unit):
        () -> Unit {
    // evaluation data
    val data = EvaluationData(protocol, metrics, ArrayList(), protocol.scenario.randomGenerator.seed)

    // subscribe for updates
    protocol.subscribe({
        for (metric in metrics) {
            for (service in it.scenario.services) {
                val value = it.getResult(service, metric)
                data.readings.add(Reading(it.time, metric, service, value))
            }
        }
    })

    // create interruption handle
    val isInterrupted = AtomicBoolean(false)
    val interrupter = { isInterrupted.set(true) }

    // create supplier (actual task)
    val task = Supplier supplier@{
        for (tick in 1..duration) {
            protocol.step(tick)

            if (isInterrupted.get()) {
                return@supplier Interrupted(tick, data)
            }
        }
        return@supplier Completed(data)
    }

    // execute the run
    CompletableFuture.supplyAsync(task).exceptionally {
        Faulted(it)
    }.thenAccept {
        when (it) {
            is Completed -> completed(it)
            is Faulted -> faulted(it)
            is Interrupted -> interrupted(it)
            is Idle, Running -> throw IllegalStateException("An evaluation cannot end in state: $it")
        }
    }

    return interrupter
}