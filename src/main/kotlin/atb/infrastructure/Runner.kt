package atb.infrastructure

import atb.core.EvaluationProtocol
import atb.interfaces.Metric
import atb.interfaces.Scenario
import atb.interfaces.TrustModel
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.registerTypeAdapter
import com.github.salomonbrys.kotson.toJson
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Supplier


/** Contains a single reading in an evaluation run */
data class Reading(val tick: Int, val metric: Metric, val service: Int, val value: Double)

/** Contains all results of an evaluation run */
data class EvaluationData(val protocol: EvaluationProtocol, val metrics: Set<Metric>,
                          val readings: MutableList<Reading>, val seed: Int) {
    /**
     * Writes the contents of a run to a CSV-based file.
     *
     * The format is the same as it was in the Repast version; each file
     * contains the following header which is followed by the data:
     * ```
     * "run", "tick", "Metric", "Name", "TrustModel", "Scenario"
     * ```
     */
    fun toCSV(fileName: String = autoName("csv")) {
        val writer = CSVWriter(FileWriter(fileName))
        writer.writeNext(arrayOf("run", "tick", "Metric", "Name", "TrustModel", "Scenario"))
        readings.forEach {
            writer.writeNext(arrayOf(
                    seed.toString(), it.tick.toString(), it.value.toString(),
                    it.metric.toString(), protocol.trustModel.toString(),
                    protocol.scenario.toString()))
        }
        writer.flushQuietly()
        writer.close()
    }

    fun toJSON(fileName: String = autoName("json")) = File(fileName).printWriter().use {
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
            setPrettyPrinting()
            setExclusionStrategies(ExcludeModelsAndScenarios())
        }.create()

        it.write(converter.toJson(this))
    }

    /**
     * Because of class overriding, do not JSON encode [TrustModel] and [Scenario] instances
     */
    internal inner class ExcludeModelsAndScenarios : ExclusionStrategy {
        override fun shouldSkipClass(clazz: Class<*>): Boolean =
                clazz == TrustModel::class.java || clazz == Scenario::class.java

        override fun shouldSkipField(f: FieldAttributes): Boolean = false
    }

    private fun autoName(type: String): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.HHmmss")
        val date = current.format(formatter)

        fun String.toFileName(): String = split(" ")
                .joinToString("") { it.capitalize() }
                .replace(Regex("\\W+"), "")

        val model = protocol.trustModel.toString().toFileName()
        val scenario = protocol.scenario.toString().toFileName()

        return "$scenario-$model-$date.$type"
    }
}

/** States of an evaluation run */
sealed class EvaluationState

/** Evaluation has completed successfully */
data class Completed(val data: EvaluationData) : EvaluationState()

/** Evaluation has been interrupted */
data class Interrupted(val tick: Int, val data: EvaluationData) : EvaluationState()

/** Evaluation has ended with an exception  */
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