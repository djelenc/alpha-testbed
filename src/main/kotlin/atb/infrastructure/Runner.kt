package atb.infrastructure

import atb.core.EvaluationProtocol
import atb.interfaces.Metric
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.registerTypeAdapter
import com.github.salomonbrys.kotson.toJson
import com.google.gson.GsonBuilder
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileWriter
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Supplier


/** Contains a reading from an evaluation run */
data class Reading(val tick: Int, val metric: Metric, val service: Int, val value: Double)

/** States in which an evaluation run can be in */
sealed class EvaluationState

/** Evaluation is completed */
data class Completed(val protocol: EvaluationProtocol, val metrics: Set<Metric>,
                     val readings: MutableList<Reading>, val seed: Int) : EvaluationState() {
    /**
     * Writes the contents of a run to a CSV-based file.
     *
     * The format is the same as it was in the Repast version; each file
     * contains the following header which is followed by the data:
     * ```
     * "run", "tick", "Metric", "Name", "TrustModel", "Scenario"
     * ```
     */
    fun toCSV(filename: String = "example.data.csv") {
        val writer = CSVWriter(FileWriter(filename))
        val records = readings.map {
            arrayOf(seed.toString(), it.tick.toString(), it.value.toString(),
                    it.metric.toString(), protocol.trustModel.toString(),
                    protocol.scenario.toString())
        }

        writer.writeNext(arrayOf("run", "tick", "Metric", "Name", "TrustModel", "Scenario"))
        writer.writeAll(records)
        writer.flushQuietly()
        writer.close()
    }

    fun toJSON(filename: String = "example.data.json") = File(filename).printWriter().use {
        val converter = GsonBuilder().apply {
            registerTypeAdapter<Metric> { serialize { it.src.toString().toJson() } }
            registerTypeAdapter<EvaluationProtocol> {
                serialize {
                    jsonObject(
                            "scenario" to it.src.scenario.toString(),
                            "trustModel" to it.src.trustModel.toString()
                    )
                }
            }
        }.create()

        it.write(converter.toJson(this))
    }
}

/** Evaluation has been interrupted */
data class Interrupted(val tick: Int, val completed: Completed) : EvaluationState()

/** Evaluation ended with an exception  */
data class Faulted(val thrown: Throwable) : EvaluationState()

/** Evaluation has not yet started */
object Idle : EvaluationState()

/** Evaluation is running */
object Running : EvaluationState()

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
    val data = Completed(protocol, metrics, ArrayList(), protocol.scenario.randomGenerator.seed)

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
    val task = Supplier stepper@{
        for (tick in 1..duration) {
            protocol.step(tick)

            if (isInterrupted.get()) {
                return@stepper Interrupted(tick, data)
            }
        }
        return@stepper data
    }

    // execute the run
    CompletableFuture.supplyAsync(task).exceptionally {
        Faulted(it)
    }.thenAccept {
        when (it) {
            is Completed -> completed(it)
            is Faulted -> faulted(it)
            is Interrupted -> interrupted(it)
        }
    }

    return interrupter
}