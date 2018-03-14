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
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    /**
     * Writes evaluation data as JSON to [fileName] in directory [path]. JSON object has the
     * following structure:
     * ```json
     * {
     *   "protocol": {
     *     "scenario": "Scenario name",
     *     "trustModel": "Trust model name"
     *   },
     *   "metrics": ["Kendall's Tau-A"],
     *   "readings": [{
     *     "tick": 1,
     *     "metric": "Kendall's Tau-A",
     *     "service": 0,
     *     "value": 0.65
     *     }, ... remaining readings ...
     *   ],
     *   "seed": 1
     * }
     * ```
     */
    fun toJSON(path: String = System.getProperty("user.dir"), fileName: String = autoName("json")) =
            File(Paths.get(path, fileName).toUri()).printWriter().use {
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

        return "$scenario-$model-$seed-$date.$type"
    }
}