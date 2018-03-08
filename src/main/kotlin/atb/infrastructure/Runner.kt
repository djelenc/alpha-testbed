package atb.infrastructure

import atb.core.EvaluationProtocol
import atb.interfaces.Metric
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.opencsv.bean.ColumnPositionMappingStrategy
import com.opencsv.bean.StatefulBeanToCsvBuilder
import java.io.File
import java.io.FileWriter
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Supplier
import kotlin.reflect.full.memberProperties


/** Contains a reading from an evaluation run */
data class Reading(val tick: Int, val metric: Metric, val service: Int, val value: Double)

/** States in which an evaluation run can be in */
sealed class EvaluationState

/** Evaluation is completed */
data class Completed(val protocol: EvaluationProtocol, val metrics: Set<Metric>,
                     val readings: MutableList<Reading>, val seed: Int) : EvaluationState() {
    /**
     * Writes the contents of a run to a CSV-based file.
     */
    fun toCSV(filename: String = "example.data.csv") {
        val fileWriter = FileWriter(filename)

        val mapping = ColumnPositionMappingStrategy<Reading>().apply {
            type = Reading::class.java
            val props = Reading::class.memberProperties.map { it.name }.toTypedArray().sortedArray()
            setColumnMapping(*props)
        }

        val toCsv = StatefulBeanToCsvBuilder<Reading>(fileWriter).apply {
            withMappingStrategy(mapping)
        }.build()

        toCsv.write(readings)
        fileWriter.flush()
        fileWriter.close()
    }

    fun toJSON(filename: String = "example.data.json") = File(filename).printWriter().use {
        val gson = GsonBuilder().let {
            it.registerTypeAdapter(Metric::class.java, MetricAdapter())
            it.create()
        }
        it.write(gson.toJson(readings))
    }

    internal inner class MetricAdapter : JsonSerializer<Metric> {
        override fun serialize(src: Metric, typeOfSrc: Type, context: JsonSerializationContext) =
                JsonPrimitive(src.toString())
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